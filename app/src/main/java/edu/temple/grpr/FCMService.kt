package edu.temple.grpr

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

class FCMService : FirebaseMessagingService() {

    companion object {
        val UPDATE_ACTION = "grpr_action_update"
        val UPDATE_KEY = "grpr_update_key"
    }

    override fun onNewToken(p0: String) {
        Helper.user.clearToken(this)
        Helper.user.registerTokenFlow(this, p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.d("FCM Message", p0.data["payload"].toString())

        val msg = JSONObject(p0.data["payload"].toString())

        when (msg.getString("action")) {
            "UPDATE" -> {
                sendBroadcast(Intent(UPDATE_ACTION).putExtra(UPDATE_KEY, msg.getJSONArray("data").toString()))
            }
        }


    }

}