package com.hornet.movies.util

import androidx.core.graphics.toRectF
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class TextVisionFinder() {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val translator = Translation.getClient(TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.FRENCH)
        .build())

    /**
     * Detect text in an image and returns normalized boxes with coordinates in the range 0 to 1
     *
     *      y = 0.0 (top)
     *       |
     *       v
     *  x=0.0 +-----------------------------+ x=1.0
     *        |                             |
     *        |       +-------------+       |
     *        |       |   Text      |       |
     *        |       +-------------+       |
     *        |                             |
     *        |                             |
     *        +-----------------------------+
     *      y = 1.0 (bottom)
     *
     * Normalized coordinates:
     *   - x, y represent the top-left corner of the bounding box
     *   - width, height are the proportions of the full width/height
     *
     * @param url image url
     */
    fun findText(url: String) {
        loadBitmapFromUrl(url) { bitmap ->
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image).addOnSuccessListener { textVision ->
                // A list of Text to Box pairs <String, RectF>
                val normalizedTextBoxes = textVision.textBlocks.mapNotNull { it.text to it.boundingBox?.toRectF()?.normalize(image.width, image.height) }
                println("====== Text Found ========================")
                normalizedTextBoxes.forEach {
                    val originalText = it.first
                    val textBox = it.second
                    println("====== Text: ${originalText}\n" +
                            "======   Box: (${textBox?.top},${textBox?.left}) to (${textBox?.bottom},${textBox?.right})")
                }
                println("==========================================")
            }
        }
    }

    /**
     * Translates the given english text to French
     *
     * @param text original text
     */
    fun translate(text: String) {
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { result ->

                    }
            }
    }
}