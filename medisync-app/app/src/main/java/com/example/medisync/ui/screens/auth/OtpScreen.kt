package com.example.medisync.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.ChatWebSocketManager
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.networks.SaveFcmTokenRequest
import com.example.medisync.networks.VerifyOtpRequest
import com.example.medisync.ui.theme.natureGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.medisync.ui.navigation.safePopBackStack

@Composable
fun OtpScreen(
    navController: NavController,
    verificationId: String,
    phoneNumber: String
) {
    var otp by remember { mutableStateOf("") }
    var timeLeft by remember { mutableIntStateOf(30) }
    var canResend by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        canResend = true
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(containerColor = AuthBg) { padding ->
        AuthScreenFrame(
            showBack = true,
            onBack = { navController.safePopBackStack() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(112.dp))

                AuthHeader(
                    title = "Verify number",
                    subtitle = "Code sent to +91 $phoneNumber",
                    showLogo = true
                )

                Spacer(Modifier.height(42.dp))

                Box {
                    OutlinedTextField(
                        value = otp,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                otp = it
                                errorMessage = ""
                            }
                        },
                        modifier = Modifier
                            .size(1.dp)
                            .focusRequester(focusRequester)
                            .alpha(0f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(6) { index ->
                            val char = otp.getOrNull(index)?.toString().orEmpty()
                            val active = index == otp.length || char.isNotEmpty()
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(AuthFieldBg)
                                    .border(
                                        width = if (active) 1.5.dp else 1.dp,
                                        color = when {
                                            errorMessage.isNotBlank() -> Color(0xFFDC2626)
                                            active -> natureGreen
                                            else -> AuthBorder
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuthTextPrimary
                                )
                            }
                        }
                    }
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(text = errorMessage, color = Color(0xFFDC2626), fontSize = 12.sp)
                }

                Spacer(Modifier.height(28.dp))

                AuthPrimaryButton(
                    text = if (isVerifying) "Verifying" else "Verify",
                    enabled = otp.length == 6 && !isVerifying,
                    loading = isVerifying,
                    onClick = {
                        isVerifying = true
                        errorMessage = ""
                        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnSuccessListener { result ->
                                result.user?.getIdToken(true)?.addOnSuccessListener { tokenResult ->
                                    val firebaseIdToken = tokenResult.token
                                    if (firebaseIdToken == null) {
                                        isVerifying = false
                                        errorMessage = "Failed to get Firebase token"
                                        return@addOnSuccessListener
                                    }

                                    scope.launch {
                                        try {
                                            val response = RetrofitInstance.api.verifyOtp(
                                                VerifyOtpRequest(idToken = firebaseIdToken)
                                            )
                                            Log.d("verifyOtp", response.toString())

                                            TokenManager.saveToken(context, response.token)
                                            FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                                                scope.launch {
                                                    runCatching {
                                                        RetrofitInstance.api.saveFcmToken(
                                                            token = "Bearer ${response.token}",
                                                            body = SaveFcmTokenRequest(token = fcmToken)
                                                        )
                                                    }
                                                }
                                            }
                                            TokenManager.saveUserId(context, response.user.id)
                                            response.user.role?.let { TokenManager.saveRole(context, it) }
                                            response.user.name?.let { TokenManager.saveName(context, it) }
                                            TokenManager.savePhone(context, response.user.phone)
                                            ChatWebSocketManager.connect(context)

                                            isVerifying = false
                                            if (response.isNewUser || response.user.name == null) {
                                                navController.navigate("selectRole/$phoneNumber")
                                            } else {
                                                when (response.user.role) {
                                                    "patient" -> navController.navigate(
                                                        "patientHome/${response.user.name}/${response.user.phone}/${response.user.id}"
                                                    )
                                                    "doctor" -> navController.navigate(
                                                        "doctorHome/${response.user.name}/${response.user.phone}/${response.user.id}"
                                                    ) {
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                    else -> navController.navigate("selectRole/$phoneNumber")
                                                }
                                            }
                                        } catch (e: Exception) {
                                            isVerifying = false
                                            errorMessage = "Login failed: ${e.message}"
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener {
                                otp = ""
                                isVerifying = false
                                errorMessage = "Invalid OTP, try again"
                            }
                    },
                    trailing = {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Didn't receive the code?", fontSize = 13.sp, color = AuthTextSecondary)
                    if (canResend) {
                        TextButton(
                            onClick = {},
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "Resend",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = natureGreen
                            )
                        }
                    } else {
                        Text(
                            text = "Resend in ${timeLeft}s",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OtpScreenPreview() {
    OtpScreen(
        navController = rememberNavController(),
        verificationId = "preview-verification-id",
        phoneNumber = "9122349557"
    )
}
