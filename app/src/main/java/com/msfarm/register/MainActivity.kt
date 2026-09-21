package com.msfarm.register

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.msfarm.register.engine.MicrosoftRegisterEngine
import com.msfarm.register.ui.FloatingWindowService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        
        val btnStart = Button(this).apply { text = "启动产号" }
        val btnFloat = Button(this).apply { text = "开启悬浮窗" }
        
        layout.addView(btnStart)
        layout.addView(btnFloat)
        setContentView(layout)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }

        btnFloat.setOnClickListener {
            startService(Intent(this, FloatingWindowService::class.java))
        }

        btnStart.setOnClickListener {
            MicrosoftRegisterEngine.startFarm(this)
        }
    }
}
