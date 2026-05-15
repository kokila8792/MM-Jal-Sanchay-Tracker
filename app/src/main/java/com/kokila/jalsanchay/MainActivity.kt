package com.kokila.jalsanchay

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient:
            GoogleSignInClient

    private var isLoggedIn by
    mutableStateOf(false)

    private val signInLauncher =
        registerForActivityResult(
            ActivityResultContracts
                .StartActivityForResult()
        ) { result ->

            val task =
                GoogleSignIn
                    .getSignedInAccountFromIntent(
                        result.data
                    )

            try {

                val account =
                    task.getResult(
                        ApiException::class.java
                    )

                account?.idToken?.let { token ->

                    firebaseAuthWithGoogle(
                        token
                    )
                }

            } catch (e: Exception) {

                e.printStackTrace()

                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            requestPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()

        val gso =
            GoogleSignInOptions.Builder(
                GoogleSignInOptions
                    .DEFAULT_SIGN_IN
            )
                .requestIdToken(
                    getString(
                        R.string.default_web_client_id
                    )
                )
                .requestEmail()
                .build()

        googleSignInClient =
            GoogleSignIn.getClient(
                this,
                gso
            )

        isLoggedIn =
            auth.currentUser != null

        val sharedPreferences =
            getSharedPreferences(
                "app_settings",
                MODE_PRIVATE
            )

        ThemeState.isDarkMode.value =
            sharedPreferences.getBoolean(
                "dark_mode",
                false
            )



        setContent {

            val darkTheme =
                ThemeState
                    .isDarkMode.value

            androidx.compose.material3
                .MaterialTheme(

                    colorScheme =

                        if (darkTheme)

                            androidx.compose.material3
                                .darkColorScheme()

                        else

                            androidx.compose.material3
                                .lightColorScheme()

                ) {

                    var showSplash by remember {

                        mutableStateOf(true)
                    }

                    if (showSplash) {

                        SplashScreen(

                            onSplashFinished = {

                                showSplash = false
                            }
                        )

                    } else {

                        if (isLoggedIn) {

                            MainScreen(

                                onLogout = {

                                    auth.signOut()

                                    googleSignInClient
                                        .signOut()

                                    isLoggedIn = false

                                    Toast.makeText(
                                        this,
                                        "Logged Out",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )

                        } else {

                            LoginScreen {

                                val signInIntent =

                                    googleSignInClient
                                        .signInIntent

                                signInLauncher.launch(
                                    signInIntent
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun firebaseAuthWithGoogle(
        idToken: String
    ) {

        val credential =

            GoogleAuthProvider
                .getCredential(
                    idToken,
                    null
                )

        auth.signInWithCredential(
            credential
        )

            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    isLoggedIn = true

                    Toast.makeText(
                        this,
                        "Login Success",
                        Toast.LENGTH_LONG
                    ).show()

                } else {

                    Toast.makeText(
                        this,
                        task.exception?.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}