package com.appsrandom.stranger.activities

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.appsrandom.stranger.R
import com.appsrandom.stranger.databinding.ActivityCallBinding
import com.bumptech.glide.Glide
import com.appsrandom.stranger.interfaces.InterFaceKotlin
import com.appsrandom.stranger.models.User
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
import java.util.UUID

class CallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCallBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var uniqueId: String
    private lateinit var userId: String
    private lateinit var friendUserName: String
    private var isPeerConnected = false
    private lateinit var database: FirebaseDatabase
    private var isAudio = true
    private var isVideo = true
    private lateinit var createdBy: String
    private var pageExit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        database =
            Firebase.database("https://stranger-6ac43-default-rtdb.asia-southeast1.firebasedatabase.app/")

        userId = intent.getStringExtra("userId").toString()
        val incoming = intent.getStringExtra("incoming").toString()
        createdBy = intent.getStringExtra("createdBy").toString()

//        if (incoming.equals(friendUserName, ignoreCase = true)) {
//            friendUserName = incoming
//        }


        database.getReference("profiles").child(auth.uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)!!

                    var coins = user.coins
                    coins -= 5

                    database.getReference("profiles")
                        .child(auth.uid.toString())
                        .child("coins")
                        .setValue(coins)

                }

                override fun onCancelled(error: DatabaseError) {

                }
        }  )


        friendUserName = incoming

        uniqueId = UUID.randomUUID().toString()

        setupWebView()

        binding.micBtn.setOnClickListener {
            isAudio = !isAudio
            callJavaScriptFunction("javascript:toggleAudio(\""+isAudio+"\")")
            if (isAudio) {
                binding.micBtn.setImageResource(R.drawable.btn_unmute_normal)
            } else {
                binding.micBtn.setImageResource(R.drawable.btn_mute_normal)
            }
        }

        binding.videoBtn.setOnClickListener {
            isVideo = !isVideo
            callJavaScriptFunction("javascript:toggleVideo(\""+isVideo+"\")")
            if (isVideo) {
                binding.videoBtn.setImageResource(R.drawable.btn_video_normal)
            } else {
                binding.videoBtn.setImageResource(R.drawable.btn_video_muted)
            }
        }

        binding.endCallBtn.setOnClickListener {
            finish()
        }

        database.getReference("users")
            .child(createdBy).addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            } )
    }

    private fun setupWebView() {
        binding.webView.webChromeClient = object: WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.mediaPlaybackRequiresUserGesture = false
        binding.webView.addJavascriptInterface(InterFaceKotlin(this), "Android")

        loadVideoCall()
    }

    private fun loadVideoCall() {
        val filePath = "file:android_asset/call.html"
        binding.webView.loadUrl(filePath)

        binding.webView.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                initializePeer()
            }
        }
    }

    fun initializePeer() {
        callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")")

        if (createdBy.equals(userId, ignoreCase = true)) {
            if (pageExit) {
                return
            }
            database.getReference("users")
                .child(userId).child("connId").setValue(uniqueId)
            database.getReference("users")
                .child(userId).child("isAvailable").setValue(true)
            binding.loadingGroup.visibility = View.GONE
            binding.controls.visibility = View.VISIBLE

            database.getReference("profiles")
                .child(friendUserName).addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)!!
                        val url = user.profilePic.take(8)
                        if (url == "https://") {
                            Glide.with(this@CallActivity).load(user.profilePic).placeholder(R.drawable.demo_user).into(binding.profilePic)
                        } else {
                            val bytes = Base64.decode(user.profilePic, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            binding.profilePic.setImageBitmap(bitmap)
                        }


                        binding.name.text = user.name
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                } )
        } else {
            Handler().postDelayed({
                friendUserName = createdBy
                database.getReference("profiles")
                    .child(friendUserName).addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue(User::class.java)!!


                            val url = user.profilePic.take(8)
                            if (url == "https://") {
                                Glide.with(this@CallActivity).load(user.profilePic).placeholder(R.drawable.demo_user).into(binding.profilePic)
                            } else {
                                val bytes = Base64.decode(user.profilePic, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                binding.profilePic.setImageBitmap(bitmap)
                            }

                            binding.name.text = user.name
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    } )
                database.getReference("users")
                    .child(friendUserName).child("connId").addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value != null) {
                                //sendCallRequest
                                sendCallRequest()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    } )
            }, 1000)
        }
    }

    fun onPeerConnected() {
        isPeerConnected = true
    }

    private fun sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(this, "You are not connected. Please check your internet connection...", Toast.LENGTH_LONG).show()
            return
        }

//        listenConnId
        listenConnId()
    }

    private fun listenConnId() {
        database.getReference("users")
            .child(friendUserName)
            .child("connId").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        return
                    }
                    binding.loadingGroup.visibility = View.GONE
                    binding.controls.visibility = View.VISIBLE
                    val connId = snapshot.getValue(String::class.java)
                    callJavaScriptFunction("javascript:startCall(\""+connId+"\")")
                }

                override fun onCancelled(error: DatabaseError) {

                }

            } )
    }

    private fun callJavaScriptFunction(function: String) {
        binding.webView.post {
            binding.webView.evaluateJavascript(function, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pageExit = true
        binding.webView.destroy()
        CoroutineScope(Dispatchers.Main).launch {
            database.getReference("users")
                .child(createdBy).setValue(null).await()
            withContext(Dispatchers.Main) {
                finish()
            }
        }
    }
}