package com.appsrandom.stranger.activities

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.appsrandom.stranger.R
import com.appsrandom.stranger.databinding.ActivityLogInBinding
import com.appsrandom.stranger.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LogInActivity : AppCompatActivity() {

    private val requestCode = 3
    private lateinit var binding: ActivityLogInBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val permissionsNew = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askPermissions()

        auth = Firebase.auth
        database = Firebase.database("https://stranger-6ac43-default-rtdb.asia-southeast1.firebasedatabase.app/")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    binding.progressBar.visibility = View.GONE
                    Log.d(ContentValues.TAG, "signInResult:failed code=" + e.statusCode)
                }
            }
            binding.progressBar.visibility = View.GONE
            binding.logInBtn.isEnabled = true

        }

        binding.logInBtn.setOnClickListener {
            val intent = mGoogleSignInClient.signInIntent
            binding.logInBtn.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            resultLauncher.launch(intent)
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
        }

        binding.pAndP.setOnClickListener {
            startActivity(Intent(this, PAndPActivity::class.java))
        }
    }

    private fun askPermissions() {
        ActivityCompat.requestPermissions(this, permissionsNew, requestCode)

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        binding.progressBar.visibility = View.VISIBLE
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user
            withContext(Dispatchers.Main) {
                updateUI(firebaseUser)
            }
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        binding.progressBar.visibility = View.VISIBLE
        binding.logInBtn.isEnabled = false
        if (firebaseUser != null) {
            val user = User(firebaseUser.uid, firebaseUser.displayName.toString(), firebaseUser.photoUrl.toString(),250)
            CoroutineScope(Dispatchers.IO).launch {

                val isUser = database.getReference("profiles").child(firebaseUser.uid).get().await()

                if (!isUser.exists()) {
                    database.getReference("profiles")
                        .child(firebaseUser.uid)
                        .setValue(user)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LogInActivity, "Account created Successfully...", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LogInActivity, "Welcome back...", Toast.LENGTH_SHORT).show()
                    }
                }

            }
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        } else {
            binding.logInBtn.isEnabled = true
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Account creation Failed!", Toast.LENGTH_SHORT).show()
        }
    }
}