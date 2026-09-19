package com.example.localaiplayer

import java.io.InputStream

object SrtParser {

    fun parse(inputStream: InputStream): List<SubtitleLine> {
        val reader = inputStream.bufferedReader()
        val result = mutableListOf<SubtitleLine>()

        var state = 0 // 0 = index, 1 = time, 2 = text
        var id = 0
        var start = 0L
        var end = 0L
        val textBuilder = StringBuilder()

        fun flush() {
            if (state == 2 && textBuilder.isNotBlank()) {
                val finalText = textBuilder.toString().trim()
                result.add(
                    SubtitleLine(
                        id = id,
                        startMs = start,
                        endMs = end,
                        text = finalText,
                        sourceText = finalText
                    )
                )
            }
            textBuilder.setLength(0)
        }

        var rawLine = reader.readLine()

        while (rawLine != null) {
            val line = rawLine.trim()

            if (line.isEmpty()) {
                flush()
                state = 0
            } else {
                when (state) {
                    0 -> {
                        id = line.toIntOrNull() ?: (result.size + 1)
                        state = 1
                    }

                    1 -> {
                        val parts = line.split("-->")
                        if (parts.size == 2) {
                            start = parseTime(parts[0].trim())
                            end = parseTime(parts[1].trim())
                            state = 2
                        }
                    }

                    2 -> {
                        if (textBuilder.isNotEmpty()) {
                            textBuilder.append('\n')
                        }
                        textBuilder.append(line)
                    }
                }
            }

            rawLine = reader.readLine()
        }

        flush()
        return result
    }

    private fun parseTime(time: String): Long {
        // Formats:
        // 00:00:01,000
        // 00:00:01.000
        val cleaned = time.replace('.', ',')
        val parts = cleaned.split(':')

        if (parts.size != 3) return 0L

        val hours = parts[0].toLongOrNull() ?: 0L
        val minutes = parts[1].toLongOrNull() ?: 0L

        val secondParts = parts[2].split(',')
        val seconds = secondParts.getOrNull(0)?.toLongOrNull() ?: 0L

        val millisText = secondParts.getOrNull(1)
            ?.take(3)
            ?.padEnd(3, '0')

        val millis = millisText?.toLongOrNull() ?: 0L

        return hours * 3_600_000L +
                minutes * 60_000L +
                seconds * 1_000L +
                millis
    }
}
