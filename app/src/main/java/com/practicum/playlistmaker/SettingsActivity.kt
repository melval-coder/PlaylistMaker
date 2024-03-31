package com.practicum.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        //обработчик нажатия кнопки "Назад" в меню настройки, для перехода на главный экран.
        val arrow = findViewById<Button>(R.id.arrow)
        arrow.setOnClickListener {
            finish()
        }
    }
}