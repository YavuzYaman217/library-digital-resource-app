package com.yyaman.libraryapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class OverdueWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val KEY_TITLE = "bookTitle"
        private const val TAG = "OverdueWorker"

        /**
         * Schedule a notification for when a book becomes overdue
         *
         * @param ctx Application context
         * @param reservationId ID of the reservation
         * @param bookTitle Title of the book to show in notification
         * @param dueMillis Due date in milliseconds since epoch
         */
        fun schedule(
            ctx: Context,
            reservationId: Int,
            bookTitle: String,
            dueMillis: Long
        ) {
            val currentTimeMillis = System.currentTimeMillis()
            val delay = dueMillis - currentTimeMillis

            if (delay <= 0) {
                // Already overdue - show notification immediately
                Log.d(TAG, "Book '$bookTitle' is already overdue, showing notification now")
                val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channelId = "overdue_alerts"
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    nm.createNotificationChannel(
                        NotificationChannel(
                            channelId, "Overdue Alerts",
                            NotificationManager.IMPORTANCE_HIGH
                        )
                    )
                }
                val notif = NotificationCompat.Builder(ctx, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("Overdue: $bookTitle")
                    .setContentText("Your reservation is now overdue.")
                    .setAutoCancel(true)
                    .build()
                nm.notify(reservationId, notif)
                return
            }

            // Schedule notification for the future
            Log.d(TAG, "Scheduling notification for '$bookTitle' in $delay ms")
            val work = OneTimeWorkRequestBuilder<OverdueWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(KEY_TITLE to bookTitle)
                )
                .build()

            WorkManager.getInstance(ctx)
                .enqueueUniqueWork(
                    "overdue_$reservationId",
                    ExistingWorkPolicy.REPLACE,
                    work
                )
        }
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val title = inputData.getString(KEY_TITLE) ?: "your book"
                showNotification("Overdue: $title", "Your reservation is now overdue.")
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Error showing notification", e)
                Result.failure()
            }
        }
    }

    private fun showNotification(title: String, text: String) {
        val nm = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "overdue_alerts"
        if (android.os.Build.VERSION.SDK_INT >= 35) {
            nm.createNotificationChannel(
                NotificationChannel(
                    channelId, "Overdue Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
        val notif = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        // Use a stable ID for the notification based on the title
        val notificationId = title.hashCode()
        nm.notify(notificationId, notif)
    }
}