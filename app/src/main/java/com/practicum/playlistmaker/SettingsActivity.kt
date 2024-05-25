package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.practicum.playlistmaker.R.layout
import com.practicum.playlistmaker.R.string

class SettingsActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Установка кнопки "назад" в setSupportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish() // Закрываем активити при нажатии на кнопку "назад"
        }

        val themeSwitcher = findViewById<SwitchCompat>(R.id.themeSwitcher)
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE)
        val darkTheme = sharedPreferences.getBoolean(DARK_THEME_ENABLED, false)
        themeSwitcher.isChecked = darkTheme


        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            (applicationContext as App).switchTheme(checked)
        }

        val shareButton = findViewById<Button>(R.id.share)
        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val message = getString(string.course_android_development)
            shareIntent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(Intent.createChooser(shareIntent, "Поделиться приложением"))
        }

        val buttonWriteToSupport = findViewById<Button>(R.id.group)
        buttonWriteToSupport.setOnClickListener {
            val subject = getString(string.subject_letter)
            val message = getString(string.text_letter)
            val shareIntent = Intent(Intent.ACTION_SENDTO)
            shareIntent.data = Uri.parse("mailto:")
            shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(string.my_email)))
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            shareIntent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(Intent.createChooser(shareIntent, "Отправить сообщение"))
        }

        val agreementButton = findViewById<Button>(R.id.agreement)
        agreementButton.setOnClickListener {
            val userAgreement = getString(string.practicum_offer)
            val agreementIntent = Intent(Intent.ACTION_VIEW, Uri.parse(userAgreement))
            startActivity(agreementIntent)
        }
    }
}