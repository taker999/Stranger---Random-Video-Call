package com.appsrandom.stranger.interfaces

import android.webkit.JavascriptInterface
import com.appsrandom.stranger.activities.CallActivity

class InterFaceKotlin(private val callActivity: CallActivity) {

    @JavascriptInterface
    fun onPeerConnected() {
        callActivity.onPeerConnected()
    }

}