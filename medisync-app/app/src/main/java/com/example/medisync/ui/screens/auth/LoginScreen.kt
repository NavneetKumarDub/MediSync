package com.example.medisync.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.ui.theme.natureGreen
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var phoneNumber  by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }
    val activity      = LocalContext.current as? Activity

    Scaffold(containerColor = Color(0xFFFAFBFC)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Brand mark ────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(natureGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "M",
                    fontSize   = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color      = natureGreen
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text       = "MediSync",
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF0F172A)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text     = "Healthcare, simplified.",
                fontSize = 13.sp,
                color    = Color(0xFF64748B)
            )

            Spacer(Modifier.height(48.dp))

            // ── Heading above input ───────────────────
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text       = "Sign in to continue",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF0F172A)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = "We'll send a code to verify your number",
                    fontSize = 12.sp,
                    color    = Color(0xFF64748B)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Phone input ───────────────────────────
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                        phoneNumber = it
                        errorMessage = ""
                    }
                },
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true,
                shape           = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Row(
                        modifier              = Modifier.padding(start = 12.dp, end = 4.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Phone,
                            contentDescription = null,
                            tint               = Color(0xFF94A3B8),
                            modifier           = Modifier.size(18.dp)
                        )
                        Text(
                            text       = "+91",
                            fontSize   = 15.sp,
                            color      = Color(0xFF0F172A),
                            fontWeight = FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(Color(0xFFE2E8F0))
                        )
                    }
                },
                placeholder = {
                    Text(
                        text     = "Phone number",
                        color    = Color(0xFF94A3B8),
                        fontSize = 15.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = natureGreen,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    cursorColor          = natureGreen,
                    focusedTextColor     = Color(0xFF0F172A),
                    unfocusedTextColor   = Color(0xFF0F172A),
                    focusedContainerColor   = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(Modifier.height(8.dp))

            // ── Error ─────────────────────────────────
            if (errorMessage.isNotEmpty()) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text     = errorMessage,
                        color    = Color(0xFFDC2626),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── CTA ───────────────────────────────────
            val canSubmit = phoneNumber.length == 10 && !isLoading
            Button(
                onClick = {
                    activity?.let {
                        isLoading    = true
                        errorMessage = ""
                        sendOtp(
                            activity = activity,
                            phone    = phoneNumber,
                            onSuccess = { verificationId ->
                                isLoading = false
                                navController.navigate("otp/$verificationId/$phoneNumber")
                            },
                            onError = { error ->
                                isLoading    = false
                                errorMessage = error
                            }
                        )
                    }
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = natureGreen,
                    disabledContainerColor = natureGreen.copy(alpha = 0.4f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        strokeWidth = 2.dp,
                        modifier    = Modifier.size(22.dp)
                    )
                } else {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text       = "Send OTP",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White
                        )
                        Icon(
                            imageVector        = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Fine print ────────────────────────────
            Text(
                text       = "By continuing, you agree to our Terms & Privacy Policy",
                fontSize   = 11.sp,
                color      = Color(0xFF94A3B8),
                modifier   = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

fun sendOtp(
    activity : Activity,
    phone    : String,
    onSuccess: (verificationId: String) -> Unit,
    onError  : (String) -> Unit
) {
    val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
        .setPhoneNumber("+91$phone")
        .setTimeout(2L, TimeUnit.SECONDS)
        .setActivity(activity)
        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onSuccess(verificationId)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError(e.message ?: "Failed to send OTP")
            }

            override fun onVerificationCompleted(credetial: PhoneAuthCredential) { }
        })
        .build()
    PhoneAuthProvider.verifyPhoneNumber(options)
}

@Preview(showBackground = true)
@Composable
fun MinePreview() {
    LoginScreen(navController = rememberNavController())
}