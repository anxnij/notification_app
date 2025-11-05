package com.example.prac31_32

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.prac31_32.databinding.ActivityMainBinding
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var helper: NotificationHelper

    // Android 13+: запрос runtime-разрешения на показ уведомлений
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* просто даём юзеру прожать кнопки снова */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        helper = NotificationHelper(this)

        helper.createChannels() // создать каналы (Тема 1, п.4)
        ensureNotificationPermission() // запрос POST_NOTIFICATIONS на 13+

        // Кнопки, каждая запускает нужный сценарий из задания:
        binding.btnSimple.setOnClickListener { helper.showSimpleNotification() }          // Тема 1, п.1
        binding.btnOpenApp.setOnClickListener { helper.showOpenAppNotification() }       // Тема 1, п.2; Тема 2, п.4
        binding.btnServiceCmd.setOnClickListener { helper.showServiceCommandNotification() } // Тема 1, п.3; Тема 2, п.1
        binding.btnLockscreen.setOnClickListener { helper.showLockscreenNotification() } // Тема 1, п.5; Тема 2, п.3
        binding.btnAction.setOnClickListener { helper.showActionNotification() }         // Тема 2: п.1, п.2, п.3, п.4

        binding.btnClearAll.setOnClickListener {
            // Быстрая очистка всех уведомлений
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancelAll()
        }
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
