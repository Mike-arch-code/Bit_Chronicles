package com.bit_chronicles.viewmodel

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class IsoMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val tileWidth = 128
    private val tileHeight = 64
    private val numRows = 20
    private val numCols = 20

    private val paintGreen = Paint().apply { color = Color.parseColor("#8BC34A") }
    private val paintGray = Paint().apply { color = Color.parseColor("#B0BEC5") }
    private val paintRed = Paint().apply { color = Color.parseColor("#FF8A65") }

    private val tileMap = Array(numRows) { row ->
        Array(numCols) { col ->
            when {
                (row + col) % 5 == 0 -> paintRed
                (row + col) % 2 == 0 -> paintGreen
                else -> paintGray
            }
        }
    }

    private val selectedTilePaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private var selectedRow = -1
    private var selectedCol = -1

    var listener: TileTouchListener? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val offsetX = width / 2
        val offsetY = -200

        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                val x = (col - row) * tileWidth / 2 + offsetX
                val y = (col + row) * tileHeight / 2 + offsetY
                drawTile(canvas, x, y, tileMap[row][col])

                if (row == selectedRow && col == selectedCol) {
                    drawTileHighlight(canvas, x, y)
                }
            }
        }

        drawCircularFog(canvas)
    }

    private fun drawTile(canvas: Canvas, x: Int, y: Int, paint: Paint) {
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
            lineTo((x + tileWidth / 2).toFloat(), (y + tileHeight / 2).toFloat())
            lineTo(x.toFloat(), (y + tileHeight).toFloat())
            lineTo((x - tileWidth / 2).toFloat(), (y + tileHeight / 2).toFloat())
            close()
        }
        canvas.drawPath(path, paint)
        canvas.drawPath(path, Paint().apply {
            style = Paint.Style.STROKE
            color = Color.DKGRAY
        })
    }

    private fun drawTileHighlight(canvas: Canvas, x: Int, y: Int) {
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
            lineTo((x + tileWidth / 2).toFloat(), (y + tileHeight / 2).toFloat())
            lineTo(x.toFloat(), (y + tileHeight).toFloat())
            lineTo((x - tileWidth / 2).toFloat(), (y + tileHeight / 2).toFloat())
            close()
        }
        canvas.drawPath(path, selectedTilePaint)
    }

    private fun drawCircularFog(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f

        val fogPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.argb(200, 232, 225, 223)
            strokeWidth = 200f
            isAntiAlias = true
            maskFilter = BlurMaskFilter(80f, BlurMaskFilter.Blur.NORMAL)
        }

        val layers = listOf(0.5f, 0.6f, 0.7f)
        layers.forEach { factor ->
            canvas.drawCircle(centerX, centerY, width * factor, fogPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val (row, col) = screenToIso(event.x, event.y)
            if (row in 0 until numRows && col in 0 until numCols) {
                selectedRow = row
                selectedCol = col
                listener?.onTileTouched(row, col)
                invalidate()
            }
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun screenToIso(x: Float, y: Float): Pair<Int, Int> {
        val offsetX = width / 2
        val offsetY = -200

        val tempX = x - offsetX
        val tempY = y - offsetY

        val col = ((tempY / tileHeight) + (tempX / tileWidth)).toInt()
        val row = ((tempY / tileHeight) - (tempX / tileWidth)).toInt()

        return Pair(row, col)
    }

    interface TileTouchListener {
        fun onTileTouched(row: Int, col: Int)
    }
}
