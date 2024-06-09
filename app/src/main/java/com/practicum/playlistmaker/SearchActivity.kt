package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    private companion object {
        const val INPUT_EDIT_TEXT = "INPUT_EDIT_TEXT"
    }

    private var text: String = ""
    private val itunesBaseUrl = "https://itunes.apple.com"
    private val retrofit =
        Retrofit.Builder()
            .baseUrl(itunesBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    private val searchHistory by lazy {
        SearchHistory(this)
    }
    private val itunesService = retrofit.create(SearchAPI::class.java)
    private lateinit var searchEditText: EditText
    private lateinit var placeholder: LinearLayout
    private lateinit var searchList: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private val tracks = ArrayList<Track>()
    private val adapter = SearchAdapter(tracks) {
        searchHistory.setTrack(it)
        val displayIntent = Intent(this, AudioPlayerActivity::class.java)
        startActivity(displayIntent)
    }

    @SuppressLint("NotifyDataSetChanged", "WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        //телепорт на "Поиск"
        val toolbar = findViewById<Toolbar>(R.id.toolbar2)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE)
        searchEditText = findViewById(R.id.searchEditText)
        searchList = findViewById(R.id.recyclerViewSearch)
        placeholder = findViewById(R.id.placeholder)
        val historyList = findViewById<RecyclerView>(R.id.historySearchList)
        val hintMessage = findViewById<LinearLayout>(R.id.historySearch)
        val clearHistoryButton = findViewById<Button>(R.id.clearHistoryButton)
        val searchList = findViewById<RecyclerView>(R.id.recyclerViewSearch)
        searchList.adapter = adapter
        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        val clearIcon = findViewById<ImageView>(R.id.clearIcon)
        val buttonRepeat = findViewById<Button>(R.id.repeatButton)
        buttonRepeat.setOnClickListener {
            search()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                search()
                return@setOnEditorActionListener true
            }
            false
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearIcon.isVisible = s?.isNotEmpty() == true
                if (searchEditText.hasFocus() && s?.isEmpty() == true) {
                    showMessage(InputStatus.SUCCESS)
                    if (searchHistory.read().isNotEmpty()) hintMessage.visibility =View.VISIBLE
                } else{
                    hintMessage.visibility =View.GONE
                }
                historyList.adapter = SearchAdapter(searchHistory.read()) {
                    searchHistory.setTrack(it)
                    val displayIntent = Intent(this@SearchActivity, AudioPlayerActivity::class.java)
                    startActivity(displayIntent)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        // При изменении фокуса на searchEditText отображается подсказка, если поле пустое и есть история поиска
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            hintMessage.visibility =
                if (hasFocus && searchEditText.text.isEmpty() && searchHistory.read()
                        .isNotEmpty()) View.VISIBLE else View.GONE
            historyList.adapter = SearchAdapter(searchHistory.read()) {
                searchHistory.setTrack(it)
                val displayIntent = Intent(this, AudioPlayerActivity::class.java)
                startActivity(displayIntent)
            }
        }
        // Обработчик нажатия кнопки для очистки истории поиска и скрытия подсказки
        clearHistoryButton.setOnClickListener {
            searchHistory.clear(sharedPreferences)
            hintMessage.visibility = View.GONE
        }

        clearIcon.setOnClickListener {
            if (searchEditText.text.isNotEmpty()) {
                searchEditText.setText("") // Очистить текстовое поле
                clearIcon.isVisible = false // Скрыть кнопку "Очистить поисковый запрос"
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchEditText.windowToken, 0) // Скрыть клавиатуру
                placeholder.visibility = View.GONE // Скрыть placeholder (сообщение)
                tracks.clear() // Очистить список треков
                adapter.notifyDataSetChanged() // Обновить адаптер
            }
        }
    }

    private fun search() {
        itunesService.search(searchEditText.text.toString())
            .enqueue(object : Callback<TrackResponse> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(
                    call: Call<TrackResponse>, response: Response<TrackResponse>
                ) {
                    if (response.code() == 200) {
                        tracks.clear()
                        if (response.body()?.results?.isNotEmpty() == true) {
                            showMessage(InputStatus.SUCCESS)
                            tracks.addAll(response.body()?.results!!)
                            adapter.notifyDataSetChanged()
                        }
                        if (tracks.isEmpty()) {
                            showMessage(InputStatus.EMPTY)
                        }
                    } else {
                        showMessage(
                            InputStatus.ERROR
                        )
                    }
                }

                override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                    showMessage(InputStatus.ERROR)
                }
            })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(INPUT_EDIT_TEXT, text)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        text = savedInstanceState.getString(INPUT_EDIT_TEXT).toString()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showMessage(status: InputStatus) {
        val buttonRepeat = findViewById<Button>(R.id.repeatButton)
        val textMessage = findViewById<TextView>(R.id.placeholderText)
        val imageMessage = findViewById<ImageView>(R.id.placeholderImage)

        buttonRepeat.visibility = View.GONE
        placeholder.visibility = View.VISIBLE
        tracks.clear()
        adapter.notifyDataSetChanged()

        when (status) {
            InputStatus.SUCCESS -> {
                placeholder.visibility = View.GONE
            }

            InputStatus.EMPTY -> {
                textMessage.text = getString(R.string.nothing_found)
                val drawableResId = if (isNightMode(this)) R.drawable.icon_internet_message_dark else R.drawable.icon_internet_message
                imageMessage.setImageResource(drawableResId)
            }

            InputStatus.ERROR -> {
                textMessage.text = getString(R.string.no_internet_connection)
                val errorImageResId = if (isNightMode(this)) R.drawable.icon_error_message_dark else R.drawable.icon_error_message
                imageMessage.setImageResource(errorImageResId)
                buttonRepeat.visibility = View.VISIBLE
            }
        }
    }

    private fun isNightMode(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true // Темная тема
            Configuration.UI_MODE_NIGHT_NO -> false // Светлая тема
            else -> false
        }
    }
}