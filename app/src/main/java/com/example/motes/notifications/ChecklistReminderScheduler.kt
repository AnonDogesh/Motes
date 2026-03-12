package com.example.motes.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object ChecklistReminderScheduler {
    private const val EXTRA_CHECKLIST_ID = "extra_checklist_id"
    private const val EXTRA_TITLE = "extra_title"

    fun schedule(context: Context, checklistId: String, title: String, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_CHECKLIST_ID, checklistId)
            putExtra(EXTRA_TITLE, title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            checklistId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel(context: Context, checklistId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            checklistId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    internal fun titleFromIntent(intent: Intent): String = intent.getStringExtra(EXTRA_TITLE).orEmpty()
    internal fun checklistIdFromIntent(intent: Intent): String = intent.getStringExtra(EXTRA_CHECKLIST_ID).orEmpty()
}
