package com.practicum.playlistmaker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button1 = findViewById<Button>(R.id.button1) //кнопка поиск
        val button2 = findViewById<Button>(R.id.button2) //кнопка медиатека
        val button3 = findViewById<Button>(R.id.button3) //кнопка настройки

        button1.setOnClickListener {
            val button1Intent = Intent(this, SearchActivity::class.java)
            startActivity(button1Intent)
        }

        button2.setOnClickListener {
            val button2Intent = Intent(this, MediaLibraryActivity::class.java)
            startActivity(button2Intent)
        }

        button3.setOnClickListener {
            val button3Intent = Intent(this, SettingsActivity::class.java)
            startActivity(button3Intent)
        }
    }
}