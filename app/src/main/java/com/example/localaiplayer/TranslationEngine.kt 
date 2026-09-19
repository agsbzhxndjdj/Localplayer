package com.example.localaiplayer

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class TranslationEngine(private val context: Context) {

    /**
     * هذه نسخة تجريبية فقط.
     * لاحقًا سيتم استبدالها بمحرك محلي حقيقي مثل:
     * - whisper.cpp
     * - faster-whisper عبر خدمة محلية
     * - NLLB
     * - نموذج لغوي محلي صغير لتحسين الصياغة
     */
    suspend fun generateDemoSubtitles(): List<SubtitleLine> = withContext(Dispatchers.IO) {
        delay(700)

        listOf(
            SubtitleLine(
                id = 1,
                startMs = 0,
                endMs = 3000,
                text = "هذه ترجمة محلية تجريبية.",
                sourceText = "This is a local demo subtitle.",
                language = "en",
                speakerGender = Gender.UNKNOWN,
                editedByUser = false
            ),
            SubtitleLine(
                id = 2,
                startMs = 3000,
                endMs = 6000,
                text = "يمكنك الضغط على زر التعديل الجانبي.",
                sourceText = "You can press the side edit button.",
                language = "en",
                speakerGender = Gender.UNKNOWN,
                editedByUser = false
            ),
            SubtitleLine(
                id = 3,
                startMs = 6000,
                endMs = 10000,
                text = "لاحقًا سيتم ربط التطبيق بنموذج ترجمة محلي حقيقي.",
                sourceText = "Later the app will connect to a real local translation model.",
                language = "en",
                speakerGender = Gender.UNKNOWN,
                editedByUser = false
            )
        )
    }
}
