package com.practicum.playlistmaker.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.InputStatus

class SearchActivity : AppCompatActivity() {

    private companion object {
        const val INPUT_EDIT_TEXT = "INPUT_EDIT_TEXT"
        private const val SEARCH_DEBOUNCE_DELAY_MILLIS = 2000L
        private const val CLICK_DEBOUNCE_DELAY_MILLIS = 1000L

        fun startActivity(context: Context) {
            val intent = Intent(context, SearchActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var text: String = ""
    private val searchHistory by lazy {
        Creator.getHistoryRepository(this)
    }
    private val searchRunnable = Runnable { search() }
    private val handler = Handler(Looper.getMainLooper())
    private var isClickAllowed = true
    private lateinit var searchEditText: EditText
    private lateinit var placeholder: LinearLayout
    private lateinit var searchList: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val interactor = Creator.provideTrackInteractor()
    private val tracks = ArrayList<Track>()
    private val adapter = SearchAdapter(tracks) {
        if (clickDebounce()) {
            searchHistory.setTrack(it)
            AudioPlayerActivity.startActivity(this)
        }
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

        searchEditText = findViewById(R.id.searchEditText)
        searchList = findViewById(R.id.recyclerViewSearch)
        placeholder = findViewById(R.id.placeholder)
        progressBar = findViewById(R.id.progressBar)
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
                handler.removeCallbacks(searchRunnable)
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
                if (searchEditText.hasFocus() && s.isNullOrEmpty()) {
                    handler.removeCallbacks(searchRunnable)
                    tracks.clear()
                    adapter.notifyDataSetChanged()
                    showMessage(InputStatus.SUCCESS)
                    if (searchHistory.getTrackList().isNotEmpty()) hintMessage.visibility =View.VISIBLE
                } else{
                    hintMessage.visibility =View.GONE
                    searchDebounce()
                }
                historyList.adapter = SearchAdapter(searchHistory.getTrackList()) {
                    searchHistory.setTrack(it)
                    AudioPlayerActivity.startActivity(this@SearchActivity)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        // При изменении фокуса на searchEditText отображается подсказка, если поле пустое и есть история поиска
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            hintMessage.visibility =
                if (hasFocus && searchEditText.text.isEmpty() && searchHistory.getTrackList()
                        .isNotEmpty()) View.VISIBLE else View.GONE
            historyList.adapter = SearchAdapter(searchHistory.getTrackList()) {
                searchHistory.setTrack(it)
                AudioPlayerActivity.startActivity(this)
            }
        }
        // Обработчик нажатия кнопки для очистки истории поиска и скрытия подсказки
        clearHistoryButton.setOnClickListener {
            searchHistory.clear()
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
        progressBar.visibility = View.VISIBLE
        searchList.visibility = View.GONE
        placeholder.visibility = View.GONE
        interactor.searchTracks(searchEditText.text.toString(), {
            handler.post {
                tracks.clear()
                progressBar.visibility = View.GONE
                if (it.isNotEmpty()) {
                    searchList.visibility = View.VISIBLE
                    tracks.addAll(it)
                    adapter.notifyDataSetChanged()
                    showMessage(InputStatus.SUCCESS)
                } else {
                    showMessage(InputStatus.EMPTY)
                }
            }
        }, {
            handler.post {
                progressBar.visibility = View.GONE
                showMessage(InputStatus.ERROR)
            }
        })
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY_MILLIS)
        }
        return current
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

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY_MILLIS)
    }
}