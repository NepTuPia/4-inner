package com.example.a4_inner.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.a4_inner.CurrentUser
import com.example.a4_inner.R
import com.example.a4_inner.databinding.ActivityLogInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.Date
import androidx.activity.result.ActivityResult
import com.example.a4_inner.FireBase

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding

    // google login
    private val auth = Firebase.auth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleGoogleSignInResult(result)
        }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.loginButton.setOnClickListener {
            performLocalLogin()
        }

        binding.googleLoginButton.setOnClickListener {
            gSignInFun()
        }
    }
    // local login
    private fun performLocalLogin() {
        if (binding.username.text.toString() == "user" && binding.password.text.toString() == "1234") {
            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(user: FirebaseUser?) { //update ui code here
        if (user != null) {
            userAssign(user)
            FireBase.firebase_online = true
            val intent = Intent(this, NaviActivity::class.java)
            intent.putExtra("fromLogin", "true")
            startActivity(intent)
            finish()
        }
    }

    private fun gSignInFun() {
        googleSignInClient.signOut()
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
                Log.d("ITM", "firebaseAuthWithGoogle: " + account.id)
            } catch (e: ApiException) {
                Log.w("ITM", "Google sign in failed: " + e.message)
            }
        } else {
            offlineLogin()
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("ITM", "signInWithCredential:success")
                    val user = auth.currentUser

                    updateUI(user)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("ITM", "signInWithCredential:failure", task.exception)
                    offlineLogin()
                }
            }
    }
    private fun offlineLogin() {
        AlertDialog.Builder(this)
            .setTitle("Offline Login")
            .setMessage("You are offline. Would you like to try offline login?")
            .setPositiveButton("Yes") { _, _ ->
                // Offline 로그인 수행
                FireBase.firebase_online = false
                val intent = Intent(this, NaviActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun userAssign(user: FirebaseUser) {
        // user object initialization(Singleton pattern) + asynchronization
        CurrentUser.initializeUser(
            user.uid,
            user.displayName,
            Timestamp(Date(user.metadata!!.creationTimestamp)),
            user.email,
            user.photoUrl
        )
        // DB write
        // Access a Cloud Firestore instance
        val db = Firebase.firestore
        // check if there's already document for the user
        val userDocRef = db.collection("users").document(user.uid)
        // Check if the document exists
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                // Document does not exist = First time login
                if (!documentSnapshot.exists()) {
                    // Uploading user data for DB
                    val userData = hashMapOf(
                        "uid" to user.uid,
                        "displayName" to user.displayName,
                        "creationTimestamp" to Timestamp(Date(user.metadata!!.creationTimestamp)),
                        "email" to user.email,
                        "photoUrl" to user.photoUrl.toString()
                    )
                    db.collection("users").document(user.uid).set(userData)
                        .addOnSuccessListener {
                            Log.d("ITM", "DocumentSnapshot added with ID: ${user.uid}")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ITM", "Error adding document", e)
                        }
                } else {
                    // X First time login -> just simple login
                    Log.d("ITM", "User exists")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ITM", "error occurred during loading firestore document in userAssign(): $e")
            }
    }

}