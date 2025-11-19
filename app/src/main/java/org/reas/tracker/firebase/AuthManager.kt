package org.reas.tracker.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import org.reas.tracker.R

private val TAG = "org.reas.tracker"

class AuthManager(private val context: Context) {
    var user by mutableStateOf<FirebaseUser?>(null)
        private set

    private lateinit var credentialManager: CredentialManager
    private val googleIdOption: GetGoogleIdOption
    private val signInWithGoogleOption: GetSignInWithGoogleOption
    private var activity: Activity? = null

    init {
        googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.web_client_id))
            .setFilterByAuthorizedAccounts(true)
            .build()
        signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
            serverClientId = context.getString(R.string.web_client_id)
        ).build()
    }

    fun init(activity: Activity) {
        this.activity = activity
        credentialManager = CredentialManager.create(activity.baseContext)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    user = Firebase.auth.currentUser
                } else {
                    // If sign in fails, display a message to the user
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    user = null
                }
            }
    }

    suspend fun signInOnLaunch() {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            // Launch Credential Manager UI
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            // Extract credential from the result returned by Credential Manager
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                // Create Google ID Token
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                // Sign in to Firebase with using the token
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } else {
                Log.w(TAG, "Credential is not of type Google ID!")
            }
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
        }
    }

    suspend fun signIn() {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        try {
            // Launch Credential Manager UI
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            // Extract credential from the result returned by Credential Manager
            val credential = result.credential

            when (credential) {
                is CustomCredential -> {
                    if (credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            // Use googleIdTokenCredential and extract id to validate and
                            // authenticate on your server.
                            val googleIdTokenCredential = GoogleIdTokenCredential
                                .createFrom(credential.data)
                            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                        } catch (e: GoogleIdTokenParsingException) {
                            Log.e(TAG, "Received an invalid google id token response", e)
                        }
                    } else {
                        // Catch any unrecognized credential type here.
                        Log.e(TAG, "Unexpected type of credential")
                    }
                }

                else -> {
                    // Catch any unrecognized credential type here.
                    Log.e(TAG, "Unexpected type of credential")
                }
            }
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
        }
    }

    suspend fun signOut() {
        // Firebase sign out
        Firebase.auth.signOut()

        // When a user signs out, clear the current user credential state from all credential providers.
        try {
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
            user = null
        } catch (e: ClearCredentialException) {
            Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
        }
    }
}