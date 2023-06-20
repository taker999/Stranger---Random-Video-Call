package com.appsrandom.stranger.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.appsrandom.stranger.R
import com.appsrandom.stranger.databinding.ActivityRewardBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RewardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardBinding
    private var rewardedAd10: RewardedAd? = null
    private var rewardedAd100: RewardedAd? = null
    private var rewardedAd150: RewardedAd? = null
    private var rewardedAd200: RewardedAd? = null
    private var rewardedAd300: RewardedAd? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var currentUid: String
    private var coins = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.database("https://stranger-6ac43-default-rtdb.asia-southeast1.firebasedatabase.app/")
        currentUid = Firebase.auth.uid.toString()

        loadAdd()
        loadAdd100()
        loadAdd150()
        loadAdd200()
        loadAdd300()

        database.getReference("profiles")
            .child(currentUid)
            .child("coins")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    coins = snapshot.getValue(Long::class.java) as Long
                    binding.coins.text = coins.toString()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            } )

        binding.video1.setOnClickListener {
            rewardedAd10?.let { ad ->
                ad.show(this) {
                    // Handle the reward.
                    loadAdd()
                    coins += 10
                    database.getReference("profiles")
                        .child(currentUid)
                        .child("coins")
                        .setValue(coins)

                    binding.video1Icon.setImageResource(R.drawable.check)
//                    val rewardAmount = rewardItem.amount
//                    val rewardType = rewardItem.type
                }
            } ?: run {
                Toast.makeText(this, "Sorry, no ad is available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.video2.setOnClickListener {
            rewardedAd100?.let { ad ->
                ad.show(this) {
                    // Handle the reward.
                    loadAdd100()
                    coins += 100
                    database.getReference("profiles")
                        .child(currentUid)
                        .child("coins")
                        .setValue(coins)

                    binding.video2Icon.setImageResource(R.drawable.check)
//                    val rewardAmount = rewardItem.amount
//                    val rewardType = rewardItem.type
                }
            } ?: run {
                Toast.makeText(this, "Sorry, no ad is available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.video3.setOnClickListener {
            rewardedAd150?.let { ad ->
                ad.show(this) {
                    // Handle the reward.
                    loadAdd150()
                    coins += 150
                    database.getReference("profiles")
                        .child(currentUid)
                        .child("coins")
                        .setValue(coins)

                    binding.video3Icon.setImageResource(R.drawable.check)
//                    val rewardAmount = rewardItem.amount
//                    val rewardType = rewardItem.type
                }
            } ?: run {
                Toast.makeText(this, "Sorry, no ad is available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.video4.setOnClickListener {
            rewardedAd200?.let { ad ->
                ad.show(this) {
                    // Handle the reward.
                    loadAdd200()
                    coins += 200
                    database.getReference("profiles")
                        .child(currentUid)
                        .child("coins")
                        .setValue(coins)

                    binding.video4Icon.setImageResource(R.drawable.check)
//                    val rewardAmount = rewardItem.amount
//                    val rewardType = rewardItem.type
                }
            } ?: run {
                Toast.makeText(this, "Sorry, no ad is available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.video5.setOnClickListener {
            rewardedAd300?.let { ad ->
                ad.show(this) {
                    // Handle the reward.
                    loadAdd300()
                    coins += 300
                    database.getReference("profiles")
                        .child(currentUid)
                        .child("coins")
                        .setValue(coins)

                    binding.video5Icon.setImageResource(R.drawable.check)
//                    val rewardAmount = rewardItem.amount
//                    val rewardType = rewardItem.type
                }
            } ?: run {
                Toast.makeText(this, "Sorry, no ad is available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadAdd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this,"ca-app-pub-6575625115963390/8305678466", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd10 = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd10 = ad
            }
        })
    }

    private fun loadAdd100() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this,"ca-app-pub-6575625115963390/2173595941", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd100 = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd100 = ad
            }
        })
    }

    private fun loadAdd150() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this,"ca-app-pub-6575625115963390/6810026710", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd150 = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd150 = ad
            }
        })
    }

    private fun loadAdd200() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this,"ca-app-pub-6575625115963390/3118193719", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd200 = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd200 = ad
            }
        })
    }

    private fun loadAdd300() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this,"ca-app-pub-6575625115963390/9492030375", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd300 = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd300 = ad
            }
        })
    }
}