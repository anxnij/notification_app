package com.example.prac31_32

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import kotlin.random.Random
/**
 * NotificationHelper — инкапсулирует создание каналов и все сценарии уведомлений.
 * Закрывает ВСЕ пункты обеих тем (см. чек-лист выше).
 */
class NotificationHelper(private val context: Context) {

    private val nm =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        // 4 канала под разные кейсы (Тема 1, п.4)
        const val CHANNEL_GENERAL = "channel_general"   // базовые уведомления
        const val CHANNEL_OPEN_APP = "channel_open_app" // тап — открыть приложение
        const val CHANNEL_SERVICE = "channel_service"   // команды в сервис
        const val CHANNEL_LOCK = "channel_lock"         // lockscreen-уведомления
    }

    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Канал 1: общий (Тема 1, п.1)
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Basic notifications with icon/title/text"
                enableLights(true)
                lightColor = Color.MAGENTA
                enableVibration(true)
            }

            // Канал 2: открытие приложения (Тема 1, п.2 + Тема 2, п.4 lockscreen)
            val openAppChannel = NotificationChannel(
                CHANNEL_OPEN_APP,
                "Open App notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Tap to open the app"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC // видно на lockscreen
                enableVibration(true)
            }

            // Канал 3: сервисные команды (Тема 1, п.3; Тема 2, п.3 — кастомная вибра)
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                "Service command notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Triggers command in service"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 80, 200) // уникальная вибрация
            }

            // Канал 4: lockscreen (Тема 1, п.5)
            val lockChannel = NotificationChannel(
                CHANNEL_LOCK,
                "Lockscreen notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Visible on lock screen"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC // публично на экране блокировки
                enableVibration(true)
            }

            nm.createNotificationChannel(generalChannel)
            nm.createNotificationChannel(openAppChannel)
            nm.createNotificationChannel(serviceChannel)
            nm.createNotificationChannel(lockChannel)
        }
    }

    /**
     * Тема 1, п.1 — простое уведомление с иконкой, заголовком и текстом.
     */
    fun showSimpleNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_notification)        // иконка
            .setContentTitle("Простое уведомление")          // заголовок
            .setContentText("Это текст уведомления ✨")       // текст
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        nm.notify(Random.nextInt(), notification)
    }

    /**
     * Тема 1, п.2 — уведомление, открывающее приложение.
     * Тема 2, п.4 — открытие из lockscreen (за счёт VISIBILITY_PUBLIC).
     */
    fun showOpenAppNotification() {
        val intent = Intent(context, MainActivity::class.java)

        // Правильный back stack, чтобы system Back работала ожидаемо
        val pendingIntent: PendingIntent =
            TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(intent)
                getPendingIntent(
                    1001,
                    PendingIntent.FLAG_UPDATE_CURRENT or flagMutable()
                )!!
            }

        val notification = NotificationCompat.Builder(context, CHANNEL_OPEN_APP)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Открой меня 🥽")
            .setContentText("Тапни, и я кину тебя прямо в приложение")
            .setContentIntent(pendingIntent)                 // ← ключ к открытию Activity
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // видно на lockscreen
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(Random.nextInt(), notification)
    }

    /**
     * Тема 1, п.3 — уведомление, которое шлёт команду в сервис.
     * Тема 2, п.1 — кнопка действия (addAction).
     */
    fun showServiceCommandNotification() {
        // Broadcast → Receiver → старт сервиса с параметром "task"
        val actionIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.prac31_32.ACTION_DO_WORK"
            putExtra("task", "sync")
        }

        val actionPendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or flagMutable()
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Сервисная команда")
            .setContentText("Нажми кнопку, я дерну сервис выполнить задачу")
            .addAction(                                    // ← кнопка действия (Тема 2, п.1)
                R.drawable.ic_notification,
                "Выполнить",
                actionPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        nm.notify(Random.nextInt(), notification)
    }

    /**
     * Тема 1, п.5 — уведомление на lockscreen.
     * Тема 2, п.3 — уникальная вибрация через vibrateCustom().
     */
    fun showLockscreenNotification() {
        val notificationId = Random.nextInt()

        vibrateCustom() // ручная вибра (дополняет вибру канала)

        val notification = NotificationCompat.Builder(context, CHANNEL_LOCK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Lockscreen alert 🔒")
            .setContentText("Это видно даже на экране блокировки")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // ключ к lockscreen
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(notificationId, notification)
    }

    /**
     * Тема 2, п.2 — поле ввода текста (RemoteInput).
     * Тема 2, п.1 — доп. кнопка «Открыть».
     * Тема 2, п.3 — вибрация.
     * Тема 2, п.4 — открытие из lockscreen (PUBLIC + PendingIntent).
     */
    fun showActionNotification() {
        val notificationId = Random.nextInt()

        // Кнопка "Ответить" с RemoteInput → текст примет BroadcastReceiver
        val replyIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.prac31_32.ACTION_REPLY"
            putExtra("notification_id", notificationId)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context, 3001, replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or flagMutable()
        )
        val remoteInput = RemoteInput.Builder("key_text_reply")
            .setLabel("Напиши ответ…") // подсказка в инлайн-поле
            .build()
        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_notification, "Ответить", replyPendingIntent
        ).addRemoteInput(remoteInput)     // ← поле ввода (Тема 2, п.2)
            .setAllowGeneratedReplies(true)
            .build()

        // Доп. кнопка «Открыть» (Тема 2, п.1)
        val openIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context, 4001, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or flagMutable()
        )
        val openAction = NotificationCompat.Action.Builder(
            R.drawable.ic_notification, "Открыть", openPendingIntent
        ).build()

        vibrateCustom() // уникальная вибра (Тема 2, п.3)

        val notification = NotificationCompat.Builder(context, CHANNEL_OPEN_APP)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Interactive notification 💬")
            .setContentText("Уведомление с reply, кнопкой и вибро")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // lockscreen-friendly
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(replyAction)
            .addAction(openAction)
            .build()

        nm.notify(notificationId, notification)
    }

    // Хелпер: ручная вибра (для старых API и кастомных паттернов)
    private fun vibrateCustom() {
        val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 150, 70, 250)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(pattern, -1)
        }
    }

    // Хелпер: корректные флаги mutability под 12L+
    private fun flagMutable(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0




}
