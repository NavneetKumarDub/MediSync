package com.example.medisync.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.BuildConfig
import com.example.medisync.networks.PatientRecordDto
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

data class PatientAiChatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false,
    val imageUri: String? = null,
    val fileName: String? = null,
    val fileType: String? = null
)

class PatientAiChatViewModel : ViewModel() {
    private var nextMessageId by mutableLongStateOf(2L)

    val messages = mutableStateListOf(
        PatientAiChatMessage(
            id = 1L,
            text = "Hi, I am MediSync AI. Ask me about symptoms, medicines, reports, or which doctor to book.",
            isUser = false
        )
    )

    var isSending by mutableStateOf(false)
        private set

    private val fallbackModelNames = listOf(
        "gemini-2.5-flash-lite",
        "gemini-2.5-flash",
        "gemini-2.0-flash"
    )
    private val httpClient = OkHttpClient()

    fun sendMessage(text: String) {
        val cleanText = text.trim()
        if (cleanText.isBlank() || isSending) return

        messages.add(
            PatientAiChatMessage(
                id = nextMessageId++,
                text = cleanText,
                isUser = true
            )
        )

        val loadingId = nextMessageId++
        messages.add(
            PatientAiChatMessage(
                id = loadingId,
                text = "Thinking...",
                isUser = false,
                isLoading = true
            )
        )

        viewModelScope.launch {
            isSending = true
            val answer = runCatching {
                if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                    error("Gemini API key is missing")
                }

                generateWithFallback(buildPrompt(cleanText))
            }.getOrElse { error ->
                "I could not connect to MediSync AI right now. ${error.message ?: "Please try again."}"
            }

            val index = messages.indexOfFirst { it.id == loadingId }
            if (index != -1) {
                messages[index] = PatientAiChatMessage(
                    id = loadingId,
                    text = answer,
                    isUser = false
                )
            }
            isSending = false
        }
    }

    fun sendImageMessage(context: Context, uri: Uri, prompt: String) {
        if (isSending) return

        val displayText = prompt.trim()
        val cleanPrompt = displayText.ifBlank {
            "Please analyze this image and explain it in simple language."
        }

        messages.add(
            PatientAiChatMessage(
                id = nextMessageId++,
                text = displayText,
                isUser = true,
                imageUri = uri.toString()
            )
        )

        val loadingId = nextMessageId++
        messages.add(
            PatientAiChatMessage(
                id = loadingId,
                text = "Analyzing image...",
                isUser = false,
                isLoading = true
            )
        )

        viewModelScope.launch {
            isSending = true
            val answer = runCatching {
                if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                    error("Gemini API key is missing")
                }

                val bitmap = loadBitmap(context, uri)
                generateImageWithFallback(
                    prompt = buildImagePrompt(cleanPrompt),
                    bitmap = bitmap
                )
            }.getOrElse { error ->
                "I could not analyze this image right now. ${error.message ?: "Please try again."}"
            }

            val index = messages.indexOfFirst { it.id == loadingId }
            if (index != -1) {
                messages[index] = PatientAiChatMessage(
                    id = loadingId,
                    text = answer,
                    isUser = false
                )
            }
            isSending = false
        }
    }

    fun sendPdfMessage(context: Context, uri: Uri, prompt: String) {
        if (isSending) return

        val fileName = getDisplayName(context, uri) ?: "PDF report"
        val displayText = prompt.trim()
        val cleanPrompt = displayText.ifBlank {
            "Please explain this PDF report in simple language."
        }

        messages.add(
            PatientAiChatMessage(
                id = nextMessageId++,
                text = displayText,
                isUser = true,
                fileName = fileName,
                fileType = "application/pdf"
            )
        )

        val loadingId = nextMessageId++
        messages.add(
            PatientAiChatMessage(
                id = loadingId,
                text = "Reading PDF...",
                isUser = false,
                isLoading = true
            )
        )

        viewModelScope.launch {
            isSending = true
            val answer = runCatching {
                if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                    error("Gemini API key is missing")
                }

                val pdfBytes = loadBytes(context, uri)
                generateBlobWithFallback(
                    prompt = buildDocumentPrompt(cleanPrompt, fileName),
                    mimeType = "application/pdf",
                    bytes = pdfBytes
                )
            }.getOrElse { error ->
                "I could not read this PDF right now. ${error.message ?: "Please try again."}"
            }

            val index = messages.indexOfFirst { it.id == loadingId }
            if (index != -1) {
                messages[index] = PatientAiChatMessage(
                    id = loadingId,
                    text = answer,
                    isUser = false
                )
            }
            isSending = false
        }
    }

    fun sendSavedRecordMessage(record: PatientRecordDto, viewUrl: String, prompt: String) {
        if (isSending) return

        val displayText = prompt.trim()
        val cleanPrompt = displayText.ifBlank {
            "Please explain this saved medical record in simple language."
        }

        messages.add(
            PatientAiChatMessage(
                id = nextMessageId++,
                text = displayText,
                isUser = true,
                imageUri = if (record.fileType?.startsWith("image/") == true) viewUrl else null,
                fileName = if (record.fileType?.startsWith("image/") == true) null else record.fileName,
                fileType = record.fileType
            )
        )

        val loadingId = nextMessageId++
        messages.add(
            PatientAiChatMessage(
                id = loadingId,
                text = "Reading saved record...",
                isUser = false,
                isLoading = true
            )
        )

        viewModelScope.launch {
            isSending = true
            val answer = runCatching {
                if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                    error("Gemini API key is missing")
                }

                val bytes = downloadBytes(viewUrl)
                if (record.fileType?.startsWith("image/") == true) {
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ?: error("Could not read saved image")
                    generateImageWithFallback(
                        prompt = buildImagePrompt(cleanPrompt),
                        bitmap = bitmap
                    )
                } else {
                    generateBlobWithFallback(
                        prompt = buildDocumentPrompt(cleanPrompt, record.fileName),
                        mimeType = record.fileType ?: "application/pdf",
                        bytes = bytes
                    )
                }
            }.getOrElse { error ->
                "I could not read this saved record right now. ${error.message ?: "Please try again."}"
            }

            val index = messages.indexOfFirst { it.id == loadingId }
            if (index != -1) {
                messages[index] = PatientAiChatMessage(
                    id = loadingId,
                    text = answer,
                    isUser = false
                )
            }
            isSending = false
        }
    }

    private suspend fun generateWithFallback(prompt: String): String {
        var lastError: Throwable? = null

        fallbackModelNames.forEach { modelName ->
            val response = runCatching {
                GenerativeModel(
                    modelName = modelName,
                    apiKey = BuildConfig.GEMINI_API_KEY
                ).generateContent(prompt).text
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: error("Empty response from $modelName")
            }

            response.getOrNull()?.let { return it }
            lastError = response.exceptionOrNull()
        }

        error(lastError?.message ?: "All Gemini models are busy. Please try again.")
    }

    private suspend fun generateImageWithFallback(prompt: String, bitmap: Bitmap): String {
        var lastError: Throwable? = null

        fallbackModelNames.forEach { modelName ->
            val response = runCatching {
                GenerativeModel(
                    modelName = modelName,
                    apiKey = BuildConfig.GEMINI_API_KEY
                ).generateContent(
                    content {
                        text(prompt)
                        image(bitmap)
                    }
                ).text
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: error("Empty response from $modelName")
            }

            response.getOrNull()?.let { return it }
            lastError = response.exceptionOrNull()
        }

        error(lastError?.message ?: "All Gemini image models are busy. Please try again.")
    }

    private suspend fun generateBlobWithFallback(
        prompt: String,
        mimeType: String,
        bytes: ByteArray
    ): String {
        var lastError: Throwable? = null

        fallbackModelNames.forEach { modelName ->
            val response = runCatching {
                GenerativeModel(
                    modelName = modelName,
                    apiKey = BuildConfig.GEMINI_API_KEY
                ).generateContent(
                    content {
                        text(prompt)
                        blob(mimeType, bytes)
                    }
                ).text
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: error("Empty response from $modelName")
            }

            response.getOrNull()?.let { return it }
            lastError = response.exceptionOrNull()
        }

        error(lastError?.message ?: "All Gemini file models are busy. Please try again.")
    }

    private suspend fun loadBitmap(context: Context, uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri).use { input ->
            BitmapFactory.decodeStream(input) ?: error("Could not read selected image")
        }
    }

    private suspend fun loadBytes(context: Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri).use { input ->
            input?.readBytes() ?: error("Could not read selected file")
        }
    }

    private suspend fun downloadBytes(url: String): ByteArray = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("Failed to download saved record")
            }
            response.body?.bytes() ?: error("Empty saved record")
        }
    }

    private fun getDisplayName(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
            }
        }.getOrNull()
    }

    private fun buildPrompt(userMessage: String): String {
        return """
            You are MediSync AI, a friendly health assistant inside the MediSync app.

            About MediSync:
            MediSync is a healthcare app with two user roles: Patient and Doctor.

            Patient side features:
            - Patients can register and login.
            - Patients can search for doctors.
            - Patients can view doctor profiles, including doctor name, speciality, sub-speciality, qualification, experience, languages, consultation mode, clinic location, and ratings.
            - Patients can book appointments with doctors.
            - Appointment slots may be Online or Offline.
            - Each slot has its own time, duration, and consultation fee.
            - Patients can view their appointment list.
            - Patients can open appointment details to see doctor info, appointment date/time, consultation type, fee, status, clinic map for offline appointments, directions, chat button, and doctor rating option.
            - Patients can chat with doctors after appointment/chat room creation.
            - Patients can send text, images, and files in chat.
            - Patients can view medical records/reports shared by doctors.
            - Patients can open saved records like images and PDFs.
            - Patients can use AI Health Chat for general health questions, symptoms, medicines, reports, and guidance.
            - Patients can access profile, medical records, AI Health Chat, and logout from the side drawer.

            Doctor side features:
            - Doctors can register and login.
            - Doctors can create and edit their profile.
            - Doctor profile includes personal details, professional details, speciality, sub-speciality, qualification, experience, languages, consultation mode, and profile photo.
            - Doctors can set clinic location using a map picker.
            - Doctors can manage regular appointment slots.
            - Doctors can manage custom appointment slots.
            - Doctors can set slot date, time, duration, consultation type, and consultation fee.
            - Doctors can view booked appointments.
            - Doctors can open appointment details.
            - Doctors can chat with patients.
            - Doctors can send files/images in chat.
            - When sending a file/image, doctors can choose to save it as a patient medical report.
            - Doctors receive notifications when a patient books an appointment.
            - Doctors can access profile, clinic location, schedules, and logout from the side drawer.

            Chat features:
            - Patient and doctor can chat in real time.
            - Chat supports text messages.
            - Chat supports image and file sharing.
            - Images can be previewed in chat.
            - Files can be opened from chat.
            - Chat notifications are sent when the app is not open.
            - If the app is open, chat notifications are suppressed.
            - Tapping a chat notification should open the related chat.

            Appointment features:
            - Patients book from available doctor slots.
            - Appointment details show different information depending on whether patient or doctor is viewing.
            - Chat is available from appointment detail for both patient and doctor.
            - Offline appointments show clinic location and directions.
            - Online appointments do not need clinic location.
            - Local reminders can notify users 10 minutes before and at appointment start time.

            Medical records:
            - Medical records are private.
            - Doctors can save shared files/images as patient reports.
            - Patients can view their saved medical records.
            - Records can be filtered by All, Images, and PDFs.
            - Records can be searched and opened.

            Notifications:
            - Push notifications are used for user-to-user events like chat messages and appointment booking.
            - Local notifications are used for appointment reminders.

            Video call:
            - The app has video call support.
            - Users can mute/unmute microphone.
            - Users can turn camera on/off.
            - Users can switch camera.
            - Users can change audio output such as Bluetooth, speaker, and earpiece.
            - Users can disconnect call.

            Your role:
            - Help patients understand health information in simple language.
            - Help patients decide which doctor speciality may be relevant.
            - Explain symptoms, medicines, and reports in general terms.
            - Guide users to use MediSync features when helpful, such as booking a doctor, checking medical records, opening appointments, or chatting with a doctor.
            - You may also answer normal casual messages politely, such as greetings, "how are you", or simple non-medical small talk.
            - If the user asks something unrelated to health or MediSync, answer briefly and gently guide back to health or app help.

            Medical safety rules:
            - Give general health information only.
            - Do not claim a final diagnosis.
            - Do not prescribe medicine dosage unless the user clearly says it was prescribed by a doctor.
            - Ask relevant follow-up questions when needed, such as duration, age, severity, medical history, pregnancy status, allergies, and current medicines.
            - Ask the user to consult a doctor when symptoms are concerning, unclear, severe, persistent, recurring, or risky.
            - For emergency symptoms such as chest pain, breathing difficulty, fainting, stroke signs, severe allergic reaction, heavy bleeding, or severe pain, advise urgent medical care immediately.
            - Keep answers simple, practical, and not too long.
            - If useful, suggest booking a doctor in MediSync.
            
            Patient message:
            $userMessage
        """.trimIndent()
    }

    private fun buildImagePrompt(userMessage: String): String {
        return buildPrompt(
            """
                The patient uploaded an image.
                User request: $userMessage

                Please analyze the image carefully. If it appears to be a medical report, prescription, medicine label, skin/body photo, or health-related image, explain what can be understood in simple language.
                Do not diagnose with certainty from the image.
                Mention urgent warning signs if relevant.
                Suggest booking a doctor in MediSync if the image suggests the user should get professional review.
            """.trimIndent()
        )
    }

    private fun buildDocumentPrompt(userMessage: String, fileName: String): String {
        return buildPrompt(
            """
                The patient uploaded a medical document or report.
                File name: $fileName
                User request: $userMessage

                Please read the document carefully and explain what it says in simple language.
                If values are present, summarize important abnormal or notable values without exaggerating.
                Do not claim a final diagnosis.
                Mention urgent warning signs if relevant.
                Suggest booking a doctor in MediSync if this document should be reviewed professionally.
            """.trimIndent()
        )
    }
}
