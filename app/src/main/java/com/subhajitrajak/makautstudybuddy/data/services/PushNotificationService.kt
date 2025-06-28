package com.subhajitrajak.makautstudybuddy.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.presentation.onboarding.OnBoardingActivity
import com.subhajitrajak.makautstudybuddy.utils.Constants.CHANNEL_DESC
import com.subhajitrajak.makautstudybuddy.utils.Constants.CHANNEL_ID
import com.subhajitrajak.makautstudybuddy.utils.Constants.CHANNEL_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PushNotificationService: FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val title = data["title"] ?: "Notification"
        val body = data["body"] ?: ""
        val imageUri = data["image"]

        if (imageUri != null) {
            // Download image in background and show notification
            serviceScope.launch {
                val bitmap = getBitmapFromUrl(imageUri)
                withContext(Dispatchers.Main) {
                    showNotification(title, body, bitmap)
                }
            }
        } else {
            showNotification(title, body, null)
        }
    }

    private fun showNotification(title: String, body: String, image: Bitmap?) {
        val channelId = CHANNEL_ID
        val channelName = CHANNEL_NAME

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(channelId) == null) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = CHANNEL_DESC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, OnBoardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.msblogo_filled)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_small_icon)
            .setLargeIcon(largeIcon)
            .setColor(Color.BLACK)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (image != null) {
            notification.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(image)
                    .bigLargeIcon(null as Bitmap?)
            )
        } else {
            notification.setStyle(
                NotificationCompat.BigTextStyle().bigText(body)
            )
        }

        // unique ID to allow multiple notifications
        notificationManager.notify(System.currentTimeMillis().toInt(), notification.build())
    }

    private suspend fun getBitmapFromUrl(imageUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                Glide.with(applicationContext)
                    .asBitmap()
                    .load(imageUrl)
                    .submit()
                    .get()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}