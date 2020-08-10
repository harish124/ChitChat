package com.example.chitchat.harish_activities.notification


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.chitchat.R
import com.example.chitchat.harish_activities.ui.FirstScreen
import com.example.chitchat.harish_activities.ui.message_acts.ChatMessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseIdService : FirebaseMessagingService() {

    private val TAG="MyMessagingService"

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        var deviceToken=""

         FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
             deviceToken=it.token
             updateToken(deviceToken)
         }


    }

    private fun updateToken(deviceToken:String) {
        FirebaseDatabase.
        getInstance().
        getReference("Users/${FirebaseAuth.getInstance().uid}/deviceToken")
            .setValue(deviceToken)
            .addOnSuccessListener {
                println("Device Token Updated")
            }
            .addOnFailureListener{
                println("Couldn't update Token\nError: ${it.message}")
            }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        sendNotifications(remoteMessage)
    }

    private fun sendNotifications(remoteMessage: RemoteMessage) {
        val soundUri =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.coolnotification)
        val mNotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mChannel: NotificationChannel

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel(
                1000.toString(),
                "ChitChat",
                NotificationManager.IMPORTANCE_HIGH
            )
            mChannel.lightColor = Color.GRAY
            mChannel.enableLights(true)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            mChannel.setSound(soundUri, audioAttributes)
            mNotificationManager.createNotificationChannel(mChannel)
        }

        val notificationBuilder=NotificationCompat.Builder(applicationContext)
            .setSmallIcon(R.drawable.androidicon)
            .setContentTitle(remoteMessage.data["title"])
            .setContentText(remoteMessage.data["body"])
            .setAutoCancel(true)
            .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" +applicationContext.packageName+"/"+R.raw.coolnotification))
            .setDefaults(Notification.DEFAULT_SOUND or  Notification.DEFAULT_VIBRATE)


        val intent=Intent(this, ChatMessageActivity::class.java)
        intent.putExtra("UserObj",remoteMessage.data["sender"])

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )


        notificationBuilder.setContentIntent(contentIntent)


        val nm=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1000, notificationBuilder.build())

    }
}