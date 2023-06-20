package com.appsrandom.stranger.activities

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import androidx.activity.OnBackPressedCallback
import com.appsrandom.stranger.R
import com.appsrandom.stranger.databinding.ActivityConnectingBinding
import com.bumptech.glide.Glide
import com.appsrandom.stranger.models.UserRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ConnectingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var isOkay = false
    private var isActive = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        database = Firebase.database("https://stranger-6ac43-default-rtdb.asia-southeast1.firebasedatabase.app/")

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val profilePic = intent.getStringExtra("profilePic")
        val url = profilePic?.take(8)
        if (url == "https://") {
            Glide.with(this@ConnectingActivity).load(profilePic).placeholder(R.drawable.demo_user).into(binding.profilePicture)
        } else {
            val bytes = Base64.decode(profilePic, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.profilePicture.setImageBitmap(bitmap)
        }


        val userId = auth.uid.toString()

        CoroutineScope(Dispatchers.IO).launch {
            database.getReference("users")
                .orderByChild("status")
                .equalTo("0").limitToFirst(1).addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.childrenCount > 0) {
                            //Room Available
                            isOkay = true
                            for (childSnap in snapshot.children) {
                                database.getReference("users")
                                    .child(childSnap.key.toString())
                                    .child("incoming")
                                    .setValue(userId)
                                database.getReference("users")
                                    .child(childSnap.key.toString())
                                    .child("status")
                                    .setValue("1")
                                val intent = Intent(this@ConnectingActivity, CallActivity::class.java)
                                intent.putExtra("userId", userId)
                                intent.putExtra("incoming", childSnap.child("incoming").getValue(String::class.java))
                                intent.putExtra("createdBy", childSnap.child("createdBy").getValue(String::class.java))
                                intent.putExtra("isAvailable", childSnap.child("isAvailable").getValue(Boolean::class.java))
                                startActivity(intent)
                                isActive = 1
                                finish()
                            }

                        } else {
                            //Room Not Available

                            val userRoom = UserRoom(userId, userId, true, "0")

                            CoroutineScope(Dispatchers.IO).launch {
                                database.getReference("users")
                                    .child(userId)
                                    .setValue(userRoom).await()

                                database.getReference("users")
                                    .child(userId).addValueEventListener(object: ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.child("status").exists()) {
                                                if (snapshot.child("status").getValue(String::class.java) == "1") {
                                                    if (isOkay) {
                                                        return
                                                    }
                                                    isOkay = true
                                                    val intent = Intent(this@ConnectingActivity, CallActivity::class.java)
                                                    intent.putExtra("userId", userId)
                                                    intent.putExtra("incoming", snapshot.child("incoming").getValue(String::class.java))
                                                    intent.putExtra("createdBy", snapshot.child("createdBy").getValue(String::class.java))
                                                    intent.putExtra("isAvailable", snapshot.child("isAvailable").getValue(Boolean::class.java))
                                                    startActivity(intent)
                                                    isActive = 1
                                                    finish()
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }

                                    } )
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            //showing dialog and then closing the application..
            CoroutineScope(Dispatchers.Main).launch {
                database.getReference("users")
                    .child(auth.uid.toString()).setValue(null).await()
                withContext(Dispatchers.Main) {
                    finish()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isActive == 0) {
            CoroutineScope(Dispatchers.Main).launch {
                database.getReference("users")
                    .child(auth.uid.toString()).setValue(null).await()
                withContext(Dispatchers.Main) {
                    finish()
                }
            }
        }
    }
}