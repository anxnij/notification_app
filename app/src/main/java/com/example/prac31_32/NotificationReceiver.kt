package com.example.prac31_32

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.RemoteInput
import androidx.core.app.NotificationManagerCompat
/**
 * NotificationReceiver — ловит:
 * 1) ACTION_DO_WORK → стартует MyService с параметром task (Тема 1, п.3; Тема 2, п.1 кнопка).
 * 2) ACTION_REPLY → читает RemoteInput и закрывает уведомление (Тема 2, п.2).
 */
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {

            "com.example.prac31_32.ACTION_DO_WORK" -> {
                val task = intent.getStringExtra("task") ?: "no_task"
                Log.d("NotificationReceiver", "ACTION_DO_WORK, task=$task")

                // Запускаем сервис с командой
                val serviceIntent = Intent(context, MyService::class.java).apply {
                    putExtra("task", task)
                }
                context.startService(serviceIntent)
            }

            "com.example.prac31_32.ACTION_REPLY" -> {
                // Читаем inline-ответ из RemoteInput (Тема 2, п.2)
                val results = RemoteInput.getResultsFromIntent(intent)
                val replyText = results?.getCharSequence("key_text_reply")?.toString() ?: "(пусто)"
                Log.d("NotificationReceiver", "ACTION_REPLY, text='$replyText'")

                // Скрываем уведомление после ответа
                val notifId = intent.getIntExtra("notification_id", -1)
                if (notifId != -1) {
                    NotificationManagerCompat.from(context).cancel(notifId)
                }
            }

            else -> Log.d("NotificationReceiver", "Unknown action: ${intent.action}")
        }
    }
}
