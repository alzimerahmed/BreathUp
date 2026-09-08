package com.smokingtracker

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ShareCardManager {

    private const val CARD_WIDTH = 1080
    private const val CARD_HEIGHT = 1350

    fun generateCard(
        context: Context,
        smokeFreeText: String,
        cigarettesAvoided: Int,
        moneySaved: String,
        achievementsCount: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.parseColor("#101418"))

        val accent = Color.parseColor("#4CAF93")
        val textPrimary = Color.WHITE
        val textSecondary = Color.parseColor("#9AA3AB")

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textPrimary
            textSize = 96f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val appNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textSecondary
            textSize = 38f
            textAlign = Paint.Align.CENTER
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textPrimary
            textSize = 72f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val taglinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textSecondary
            textSize = 34f
            textAlign = Paint.Align.CENTER
        }

        val cx = CARD_WIDTH / 2f
        canvas.drawText(context.getString(R.string.app_name), cx, 140f, appNamePaint)
        canvas.drawText(smokeFreeText, cx, 420f, titlePaint)
        canvas.drawText(context.getString(R.string.share_smoke_free), cx, 500f, labelPaint)

        val rows = buildList {
            add(context.getString(R.string.share_cigarettes_avoided) to cigarettesAvoided.toString())
            if (moneySaved.isNotBlank()) {
                add(context.getString(R.string.share_money_saved) to moneySaved)
            }
            add(context.getString(R.string.share_achievements) to achievementsCount.toString())
        }

        var y = 640f
        rows.forEach { (label, value) ->
            canvas.drawText(label, cx, y, labelPaint)
            canvas.drawText(value, cx, y + 78f, valuePaint)
            y += 200f
        }

        canvas.drawText(context.getString(R.string.share_tagline), cx, CARD_HEIGHT - 120f, taglinePaint)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            strokeWidth = 8f
        }
        canvas.drawLine(cx - 80f, CARD_HEIGHT - 170f, cx + 80f, CARD_HEIGHT - 170f, linePaint)

        return bitmap
    }

    fun shareCard(context: Context, bitmap: Bitmap) {
        val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(shareDir, "breathup_progress_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    fun formatMoney(amount: Float, currency: String): String {
        return String.format(Locale.getDefault(), "%.0f %s", amount, currency)
    }
}
