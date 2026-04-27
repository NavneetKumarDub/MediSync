package com.example.medisync.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.networks.VerifyOtpRequest
import com.example.medisync.networks.WebSocketManager
import com.example.medisync.ui.theme.natureGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    navController : NavController,
    verificationId: String,
    phoneNumber   : String
) {
    var otp          by remember { mutableStateOf("") }
    var timeLeft     by remember { mutableIntStateOf(30) }
    var canResend    by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isVerifying  by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val scope          = rememberCoroutineScope()
    val context        = LocalContext.current

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        canResend = true
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(containerColor = Color(0xFFFAFBFC)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Back ─────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint               = Color(0xFF0F172A)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ── Icon ─────────────────────────
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(natureGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Sms,
                        contentDescription = null,
                        tint               = natureGreen,
                        modifier           = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text       = "Verification code",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF0F172A)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text     = "Enter the 6-digit code sent to",
                    fontSize = 13.sp,
                    color    = Color(0xFF64748B)
                )
                Text(
                    text       = "+91 $phoneNumber",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF0F172A)
                )

                Spacer(Modifier.height(40.dp))

                // ── OTP boxes ─────────────────────
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
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        (0 until 6).forEach { index ->
                            val char       = otp.getOrNull(index)?.toString() ?: ""
                            val isFilled   = char.isNotEmpty()
                            val isFocused  = index == otp.length

                            Box(
                                modifier = Modifier
                                    .size(width = 46.dp, height = 56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(
                                        width = if (isFocused || isFilled) 1.5.dp else 1.dp,
                                        color = when {
                                            errorMessage.isNotEmpty() -> Color(0xFFDC2626)
                                            isFocused || isFilled     -> natureGreen
                                            else                      -> Color(0xFFE2E8F0)
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = char,
                                    fontSize   = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text     = errorMessage,
                        color    = Color(0xFFDC2626),
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(28.dp))

                // ── Verify button ────────────────
                Button(
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
                                            TokenManager.saveUserId(context, response.user.id)
                                            response.user.role?.let { TokenManager.saveRole(context, it) }
                                            WebSocketManager.connect(context)

                                            if (response.isNewUser || response.user.name == null) {
                                                navController.navigate("selectRole/$phoneNumber")
                                            } else {
                                                when (response.user.role) {
                                                    "patient" -> navController.navigate(
                                                        "patientHome/${response.user.name}/${response.user.phone}/${response.user.id}"
                                                    )
                                                    "doctor" -> navController.navigate(
                                                        "doctorHome/${response.user.name}/${response.user.phone}/${response.user.id}"
                                                    )
                                                    {
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
                                otp         = ""
                                isVerifying = false
                                errorMessage = "Invalid OTP, try again"
                            }
                    },
                    enabled = otp.length == 6 && !isVerifying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = natureGreen,
                        disabledContainerColor = natureGreen.copy(alpha = 0.4f)
                    )
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            color       = Color.White,
                            strokeWidth = 2.dp,
                            modifier    = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            text       = "Verify",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Resend ───────────────────────
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text     = "Didn't receive the code?",
                        fontSize = 13.sp,
                        color    = Color(0xFF64748B)
                    )
                    if (canResend) {
                        TextButton(
                            onClick = { /* TODO: resend */ },
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text       = "Resend",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = natureGreen
                            )
                        }
                    } else {
                        Text(
                            text       = "Resend in ${timeLeft}s",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color(0xFF94A3B8)
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
        navController  = rememberNavController(),
        verificationId = "preview-verification-id",
        phoneNumber    = "9122349557"
    )
}