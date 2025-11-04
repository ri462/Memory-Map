package com.rina.memorymap

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// カレンダー画面のアクティビティです
class CalendarActivity : AppCompatActivity() {

    // privateは「このクラスの中だけで使いますよ」という意味です
    private lateinit var calendarView: CalendarView // カレンダーの見た目
    private lateinit var textMemoList: TextView // メモを表示するテキスト
    private lateinit var btnBackToMain: Button // 戻るボタン

    // allPlacesという名前の、PlaceItemを入れるためのリストを作ります
    // このリストに、保存された場所のデータを全部入れておきます
    private var allPlaces = mutableListOf<PlaceItem>()

    // onCreateは、画面が作られるときに一番最初に呼ばれる大事な部分です
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar) // activity_calendar.xmlの見た目を表示する

        // XMLの部品をプログラムで使えるように、IDを使って探してきます
        calendarView = findViewById(R.id.calendarView)
        textMemoList = findViewById(R.id.textMemoList)
        btnBackToMain = findViewById(R.id.btnBackToMain)

        // 戻るボタンが押されたときの処理
        btnBackToMain.setOnClickListener {
            finish() // finish()を呼ぶと、今の画面が閉じて前の画面に戻ります
        }

        // まず、スマホに保存されているデータを全部読み込みます
        loadAllMemos()

        // 最初に画面が開かれたときは、今日の日付のメモを表示します
        val today = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDateString = sdf.format(today.time)
        updateMemoListForDate(todayDateString)

        // カレンダーの日付がユーザーによって選ばれたときの処理をセットします
        calendarView.setOnDateChangeListener(object : CalendarView.OnDateChangeListener {
            override fun onSelectedDayChange(view: CalendarView, year: Int, month: Int, dayOfMonth: Int) {
                // 選ばれた日付でCalendarオブジェクトを作ります
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // 日付を「yyyy-MM-dd」の形の文字列にします
                val sdf_for_listener = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val selectedDateStr = sdf_for_listener.format(selectedCalendar.time)

                // その日付のメモを表示する関数を呼びます
                updateMemoListForDate(selectedDateStr)
            }
        })
    }

    // SharedPreferencesから全てのメモを読み込む関数
    private fun loadAllMemos() {
        // "memo"という名前のファイルからデータを読み込む準備
        val prefs = getSharedPreferences("memo", MODE_PRIVATE)
        // "locations"というキーで保存されている文字列のセットを読み込む
        val jsonStringSet = prefs.getStringSet("locations", null)

        // もしデータが何もなければ、何もしないで関数を終わります
        if (jsonStringSet == null) {
            return
        }

        val gson = Gson()
        // for文を使って、一つ一つのデータを取り出します
        for (jsonString in jsonStringSet) {
            try {
                // JSONという形式の文字列を、PlaceItemというオブジェクトに変換します
                val place = gson.fromJson(jsonString, PlaceItem::class.java)
                // 変換したオブジェクトをリストに追加します
                allPlaces.add(place)
            } catch (e: Exception) {
                // もし変換に失敗したら、エラーログを出します
                Log.e("CalendarActivity", "JSONのパースに失敗しました: " + jsonString)
            }
        }
    }

    // 指定された日付のメモをTextViewに表示する関数
    private fun updateMemoListForDate(dateStr: String) {
        // その日付のメモだけを入れるための、新しい空のリストを作ります
        val memosForDate = mutableListOf<PlaceItem>()

        // for文ですべてのメモをチェックします
        for (place in allPlaces) {
            // もしメモの日付(place.date)が、指定された日付(dateStr)と同じなら
            if (place.date == dateStr) {
                // リストに追加します
                memosForDate.add(place)
            }
        }

        // もし、その日のメモが一つもなかったら
        if (memosForDate.isEmpty()) {
            textMemoList.text = dateStr + " のメモはありません"
        } else {
            // 表示するための文字列を組み立てます
            var resultText = dateStr + " のメモ:\n\n"
            // for文で、その日のメモを一つずつ取り出します
            for (memo in memosForDate) {
                // 文字列を連結していきます
                resultText += "【" + memo.memo + "】\n\n"
            }
            // 出来上がった文字列をTextViewにセットします
            textMemoList.text = resultText
        }
    }
}