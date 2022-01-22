package com.example.prajnasocketmodeapp

import android.app.Application
import android.system.Os.setenv
import android.util.Log
import com.slack.api.bolt.App
import com.slack.api.bolt.socket_mode.SocketModeApp
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.model.event.ReactionAddedEvent

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("prajna", "here here")

        // initialize Rudder SDK here
    }
}