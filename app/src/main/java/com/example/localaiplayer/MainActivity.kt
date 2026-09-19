package com.example.localaiplayer

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var subtitleView: TextView

    private var subtitles = mutableListOf<SubtitleLine>()
    private var currentIndex = -1

    private val handler = Handler(Looper.getMainLooper())

    private val pickVideo =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                videoView.setVideoURI(it)
            }
        }

    private val pickSubtitle =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                loadSubtitle(it)
            }
        }

    private val updateTask = object : Runnable {
        override fun run() {
            updateSubtitle()
            handler.postDelayed(this, 200)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        videoView = findViewById(R.id.videoView)
        subtitleView = findViewById(R.id.subtitleView)

        val btnPickVideo = findViewById<Button>(R.id.btnPickVideo)
        val btnPickSubtitle = findViewById<Button>(R.id.btnPickSubtitle)
        val btnLocalTranslate = findViewById<Button>(R.id.btnLocalTranslate)
        val btnEdit = findViewById<Button>(R.id.btnEdit)

        btnPickVideo.setOnClickListener {
            pickVideo.launch(arrayOf("video/*"))
        }

        btnPickSubtitle.setOnClickListener {
            pickSubtitle.launch(arrayOf("*/*"))
        }

        btnLocalTranslate.setOnClickListener {
            lifecycleScope.launch {
                Toast.makeText(
                    this@MainActivity,
                    "جارٍ توليد ترجمة محلية تجريبية...",
                    Toast.LENGTH_SHORT
                ).show()

                val engine = TranslationEngine(this@MainActivity)
                subtitles = engine.generateDemoSubtitles().toMutableList()
                currentIndex = -1

                Toast.makeText(
                    this@MainActivity,
                    "تم توليد ${subtitles.size} سطر",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnEdit.setOnClickListener {
            openEditDialog()
        }

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = false
            mp.start()
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateTask)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTask)
    }

    private fun loadSubtitle(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                subtitles = SrtParser.parse(input).toMutableList()
                currentIndex = -1

                Toast.makeText(
                    this,
                    "تم تحميل ${subtitles.size} سطر ترجمة",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "فشل تحميل الترجمة: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateSubtitle() {
        if (subtitles.isEmpty()) {
            subtitleView.text = ""
            return
        }

        val position = videoView.currentPosition.toLong()

        val index = subtitles.indexOfFirst {
            position >= it.startMs && position <= it.endMs
        }

        if (index >= 0) {
            if (index != currentIndex) {
                currentIndex = index
                subtitleView.text = subtitles[index].text
            }
        } else {
            currentIndex = -1
            subtitleView.text = ""
        }
    }

    private fun openEditDialog() {
        if (subtitles.isEmpty()) {
            Toast.makeText(this, "لا توجد ترجمة لتعديلها", Toast.LENGTH_SHORT).show()
            return
        }

        val index = if (currentIndex >= 0) currentIndex else 0
        val line = subtitles[index]

        val editText = EditText(this)
        editText.setText(line.text)

        val genderItems = arrayOf("غير معروف", "ذكر", "أنثى")

        var chosenGender = when (line.speakerGender) {
            Gender.MALE -> 1
            Gender.FEMALE -> 2
            else -> 0
        }

        val builder = AlertDialog.Builder(this)
            .setTitle("تعديل السطر ${line.id}")
            .setMessage("النص الأصلي: ${line.sourceText.ifEmpty { "غير متوفر" }}")
            .setView(editText)
            .setSingleChoiceItems(genderItems, chosenGender) { _, which ->
                chosenGender = which
            }
            .setPositiveButton("حفظ") { _, _ ->
                line.text = editText.text.toString()
                line.editedByUser = true

                line.speakerGender = when (chosenGender) {
                    1 -> Gender.MALE
                    2 -> Gender.FEMALE
                    else -> Gender.UNKNOWN
                }

                updateSubtitle()

                Toast.makeText(this, "تم الحفظ", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("إلغاء", null)

        builder.show()
    }
}
