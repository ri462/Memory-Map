package com.rina.memorymap

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var btnSave: FloatingActionButton
    private var selectedLatLng: LatLng? = null
    private lateinit var editSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnCalendar: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI要素の初期化
        setupUI()
        // マップの初期化
        setupMap()
        // 必要な権限をリクエスト
        checkPermissions()
    }

    private fun setupUI() {
        editSearch = findViewById(R.id.editSearch)
        btnSearch = findViewById(R.id.btnSearch)
        btnSearch.setOnClickListener {
            val keyword = editSearch.text.toString()
            if (keyword.isNotEmpty()) searchLocation(keyword)
            else Toast.makeText(this, "検索ワードを入力してください", Toast.LENGTH_SHORT).show()
        }

        btnSave = findViewById(R.id.btn_save)
        btnSave.setOnClickListener {
            selectedLatLng?.let { showMemoInputDialog(it) }
                ?: Toast.makeText(this, "地図をタップして場所を選択してください", Toast.LENGTH_SHORT).show()
        }

        btnCalendar = findViewById(R.id.btn_calendar)
        btnCalendar.setOnClickListener {
            // ★ 変更点: CalendarActivity を開くようにする
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val japanLatLng = LatLng(35.681236, 139.767125)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(japanLatLng, 14f))

        mMap.setOnMapClickListener { latLng ->
            selectedLatLng = latLng
            mMap.clear()
            loadSavedLocations()
            mMap.addMarker(MarkerOptions().position(latLng).title("新しい場所").snippet("この場所にメモを追加します"))
            Toast.makeText(this, "場所を選択しました。「保存」ボタンからメモを作成できます。", Toast.LENGTH_SHORT).show()
        }

        loadSavedLocations()
    }

    private fun searchLocation(keyword: String) {
        try {
            val geocoder = Geocoder(this, Locale.JAPAN)
            val addresses = geocoder.getFromLocationName(keyword, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                selectedLatLng = latLng
                mMap.clear()
                loadSavedLocations()
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                mMap.addMarker(MarkerOptions().position(latLng).title(keyword))
            } else {
                Toast.makeText(this, "場所が見つかりません", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "検索中にエラーが発生しました", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMemoInputDialog(latLng: LatLng) {
        val editText = EditText(this)
        editText.hint = "例: 〇〇さんと打ち合わせ"
        AlertDialog.Builder(this)
            .setTitle("メモを入力")
            .setMessage("この場所のメモと日時を設定します")
            .setView(editText)
            .setPositiveButton("日時選択へ") { _, _ ->
                val memoText = editText.text.toString()
                if (memoText.isNotBlank()) showDateTimePicker(latLng, memoText)
                else Toast.makeText(this, "メモを入力してください", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun showDateTimePicker(latLng: LatLng, memoText: String) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                calendar.set(year, month, day, hour, minute)
                // 各処理を呼び出し
                saveLocationToPrefs(latLng, memoText, calendar)
                addEventToCalendarAutomatically(memoText, calendar) // ★自動保存関数に変更
                mMap.clear()
                loadSavedLocations()
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveLocationToPrefs(latLng: LatLng, memoText: String, calendar: Calendar) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = sdf.format(calendar.time)
        val place = PlaceItem(latLng.latitude, latLng.longitude, memoText, dateStr)
        val prefs = getSharedPreferences("memo", MODE_PRIVATE)
        val saved = prefs.getStringSet("locations", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val gson = Gson()
        saved.add(gson.toJson(place))
        prefs.edit().putStringSet("locations", saved).apply()
        Toast.makeText(this, "アプリに場所を保存しました", Toast.LENGTH_SHORT).show()
    }

    /**
     * ★ 新機能: カレンダーにイベントを【自動で】登録する
     */
    private fun addEventToCalendarAutomatically(memoText: String, calendar: Calendar) {
        val calId = getGoogleCalendarId()
        if (calId == null) {
            Toast.makeText(this, "Googleカレンダーのアカウントが見つかりません", Toast.LENGTH_LONG).show()
            return
        }

        var addressString: String? = null
        selectedLatLng?.let {
            try {
                val geocoder = Geocoder(this, Locale.JAPAN)
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                // ### FIX ###
                // Changed the 'if' to an if-else expression to fix the build error.
                addressString = if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("Geocoder", "住所の取得に失敗しました", e)
            }
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, calendar.timeInMillis)
            put(CalendarContract.Events.DTEND, calendar.timeInMillis + 60 * 60 * 1000) // 1時間
            put(CalendarContract.Events.TITLE, memoText)
            addressString?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }
            put(CalendarContract.Events.CALENDAR_ID, calId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        try {
            contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            Toast.makeText(this, "Googleカレンダーに自動で保存しました", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Log.e("Calendar", "カレンダーへの書き込み権限がありません", e)
            Toast.makeText(this, "カレンダーへのアクセスが許可されていません", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * ★ 新機能: GoogleカレンダーのアカウントIDを取得する
     */
    @SuppressLint("Range")
    private fun getGoogleCalendarId(): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE
        )
        val selection = "${CalendarContract.Calendars.ACCOUNT_TYPE} = ?"
        val selectionArgs = arrayOf("com.google")

        try {
            val cursor = contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )
            // ### FIX ###
            // Switched to a .use block for safer cursor handling, which also resolves the error.
            cursor?.use {
                if (it.moveToFirst()) {
                    return it.getLong(it.getColumnIndex(CalendarContract.Calendars._ID))
                }
            }
        } catch (e: SecurityException) {
            Log.e("Calendar", "カレンダーの読み取り権限がありません", e)
        }
        return null
    }

    private fun loadSavedLocations() {
        val prefs = getSharedPreferences("memo", MODE_PRIVATE)
        val saved = prefs.getStringSet("locations", emptySet())
        val gson = Gson()
        saved?.forEach { json ->
            try {
                val place = gson.fromJson(json, PlaceItem::class.java)
                val latLng = LatLng(place.latitude, place.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(place.memo).snippet(place.date))
            } catch (e: Exception) {
                Log.e("loadError", "JSON読み込み失敗", e)
            }
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.ACCESS_FINE_LOCATION, // マップのために念のため
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permissions, 100)
    }
}