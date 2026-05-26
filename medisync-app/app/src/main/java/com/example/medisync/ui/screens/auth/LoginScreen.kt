package com.example.medisync.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

@Composable
fun LoginScreen(navController: NavController) {
    var phoneNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity

    Scaffold(containerColor = AuthBg) { padding ->
        AuthScreenFrame {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(104.dp))

                AuthHeader(
                    title = "MediSync",
                    subtitle = "Healthcare, connected."
                )

                Spacer(Modifier.height(46.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Sign in",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuthTextPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Enter your mobile number to continue",
                        fontSize = 13.sp,
                        color = AuthTextSecondary
                    )
                }

                Spacer(Modifier.height(22.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                            phoneNumber = it
                            errorMessage = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Row(
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = AuthTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "+91",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuthTextPrimary
                            )
                            Spacer(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(20.dp)
                                    .background(AuthBorder)
                            )
                        }
                    },
                    placeholder = {
                        Text("Phone number", color = Color(0xFF94A3B8), fontSize = 15.sp)
                    },
                    colors = authTextFieldColors()
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(22.dp))

                AuthPrimaryButton(
                    text = if (isLoading) "Sending OTP" else "Send OTP",
                    enabled = phoneNumber.length == 10 && !isLoading,
                    loading = isLoading,
                    onClick = {
                        activity?.let {
                            isLoading = true
                            errorMessage = ""
                            sendOtp(
                                activity = it,
                                phone = phoneNumber,
                                onSuccess = { verificationId ->
                                    isLoading = false
                                    navController.navigate("otp/$verificationId/$phoneNumber")
                                },
                                onError = { error ->
                                    isLoading = false
                                    errorMessage = error
                                }
                            )
                        }
                    },
                    trailing = {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "By continuing, you agree to MediSync terms and privacy policy",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 28.dp)
                )
            }
        }
    }
}

fun sendOtp(
    activity: Activity,
    phone: String,
    onSuccess: (verificationId: String) -> Unit,
    onError: (String) -> Unit
) {
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        }

        override fun onVerificationFailed(e: FirebaseException) {
            onError(e.message ?: "OTP failed")
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            onSuccess(verificationId)
        }
    }

    val options = PhoneAuthOptions.newBuilder()
        .setPhoneNumber("+91$phone")
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(activity)
        .setCallbacks(callbacks)
        .build()

    PhoneAuthProvider.verifyPhoneNumber(options)
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}
