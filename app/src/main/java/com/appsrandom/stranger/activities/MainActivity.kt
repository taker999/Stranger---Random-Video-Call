package com.appsrandom.stranger.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.appsrandom.stranger.R
import com.appsrandom.stranger.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.appsrandom.stranger.models.User
import com.appsrandom.stranger.models.UserRoom
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.github.rupinderjeet.kprogresshud.KProgressHUD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var coins = 0L
    private val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val requestCode = 1
    private lateinit var user: User
    private lateinit var progress: KProgressHUD
    private var STORAGE_PERMISSION_CODE = 2
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private var encodedImage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}

        progress = KProgressHUD.create(this)
        progress.setDimAmount(0.5f)
        progress.show()

        auth = Firebase.auth
        database = Firebase.database("https://stranger-6ac43-default-rtdb.asia-southeast1.firebasedatabase.app/")


        database.getReference("users")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var count = 0
                    for (i in snapshot.children) {
                        val user = i.getValue(UserRoom::class.java)!!
                        if (user.status == "0") {
                            if (user.createdBy != auth.uid.toString()) {
                                count++
                            }
                        }
                    }
                    binding.activeCount.text = count.toString()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        binding.editImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    galleryLauncher.launch("image/*")
                } else {
                    requestStoragePermission()
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    galleryLauncher.launch("image/*")
                } else {
                    requestStoragePermission()
                }
            }

        }

//        pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            if (it.resultCode == RESULT_OK) {
//                if (it.data != null) {
//                    val imageUri = it.data!!.data as Uri
//                    try {
//                        val inputStream = contentResolver.openInputStream(imageUri)
//                        val bitmap = BitmapFactory.decodeStream(inputStream)
//                        binding.profilePic.setImageBitmap(bitmap)
//                        encodedImage = encodedImage(bitmap)
//
//                        CoroutineScope(Dispatchers.IO).launch {
//                            database.getReference("profiles")
//                                .child(auth.uid.toString()).child("profilePic")
//                                .setValue(encodedImage)
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//            }

            galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback {
                if (it != null) {
                    val imageUri = it
                    try {
                        val inputStream = contentResolver.openInputStream(imageUri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.profilePic.setImageBitmap(bitmap)
                        encodedImage = encodedImage(bitmap)

                        CoroutineScope(Dispatchers.IO).launch {
                            database.getReference("profiles")
                                .child(auth.uid.toString()).child("profilePic")
                                .setValue(encodedImage)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })

//        CoroutineScope(Dispatchers.IO).launch {
//            val userSnapshot = database.getReference("profiles").child(auth.uid.toString()).get().await()
//            user = userSnapshot.getValue(User::class.java)!!
//            coins = user.coins
//
//            withContext(Dispatchers.Main) {
//                binding.coins.text = "You have: $coins"
//                Glide.with(this@MainActivity).load(user.profilePic).placeholder(R.drawable.demo_user).into(binding.profilePic)
//                progress.dismiss()
//            }
//        }

        database.getReference("profiles").child(auth.uid.toString()).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    user = snapshot.getValue(User::class.java)!!

                    coins = user.coins

                    binding.coins.text = "You have: $coins"
                    val url = user.profilePic.take(8)
                    if (url == "https://") {
                        Glide.with(this@MainActivity).load(user.profilePic).placeholder(R.drawable.demo_user).into(binding.profilePic)
                    } else {
                        val bytes = Base64.decode(user.profilePic, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        binding.profilePic.setImageBitmap(bitmap)
                    }
                    progress.dismiss()
                } catch (_: Exception) {
                    progress.dismiss()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        } )

        binding.findBtn.setOnClickListener {
            if (isPermissionGranted()) {
                if (coins > 5) {
//                    coins-= 5
//                    database.getReference("profiles")
//                        .child(auth.uid.toString())
//                        .child("coins")
//                        .setValue(coins)
                    val intent = Intent(this, ConnectingActivity::class.java)
                    intent.putExtra("profilePic", user.profilePic)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Insufficient Coins", Toast.LENGTH_SHORT).show()
                }
            } else {
                askPermissions()
            }
        }

        binding.rewardBtn.setOnClickListener {
            startActivity(Intent(this, RewardActivity::class.java))
        }
    }

    private fun encodedImage(bitmap: Bitmap): String {
        val previewWidth = 200
//        val previewHeight = bitmap.height + previewWidth / bitmap.width
        val previewHeight = 200
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), STORAGE_PERMISSION_CODE)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        }
    }

    private fun isPermissionGranted(): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.Main).launch {
            database.getReference("users")
                .child(auth.uid.toString()).setValue(null).await()
            withContext(Dispatchers.Main) {
//                finish()
            }
        }

    }
}