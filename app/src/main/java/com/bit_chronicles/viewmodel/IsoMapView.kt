package com.bit_chronicles.viewmodel

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.bit_chronicles.R
import com.bit_chronicles.model.MapGenerator
import kotlin.math.abs
import kotlin.math.max

class IsoMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val tileWidth = 128
    private val tileHeight = 64
    private val visibleSize = 7

    private val tileGrass = BitmapFactory.decodeResource(resources, R.drawable.grass)
    private val tileEnemy = BitmapFactory.decodeResource(resources, R.drawable.roca)
    private val tileObstacle = BitmapFactory.decodeResource(resources, R.drawable.water)
    private val tilePlayer = BitmapFactory.decodeResource(resources, R.drawable.grassplayer)

    private val selectedTilePaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private var selectedRow = -1
    private var selectedCol = -1

    private val map = MapGenerator.generateMap()

    var playerMapRow = 50
    var playerMapCol = 50

    private var playerPosRow = playerMapRow.toFloat()
    private var playerPosCol = playerMapCol.toFloat()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val offsetX = width / 2
        val offsetY = 100

        for (row in 0 until visibleSize) {
            for (col in 0 until visibleSize) {
                val mapRow = (playerPosRow - 2 + row).toInt()
                val mapCol = (playerPosCol - 2 + col).toInt()

                val screenX = (col - row) * tileWidth / 2 + offsetX
                val screenY = (col + row) * tileHeight / 2 + offsetY

                val isPlayer = row == 3 && col == 3

                val bitmap = when {
                    isPlayer -> tilePlayer
                    map.getOrNull(mapRow)?.getOrNull(mapCol) == 2 -> tileEnemy
                    map.getOrNull(mapRow)?.getOrNull(mapCol) == 3 -> tileObstacle
                    else -> tileGrass
                }

                drawTileBitmap(canvas, screenX, screenY, bitmap)

                if (row == selectedRow && col == selectedCol) {
                    drawTileHighlight(canvas, screenX, screenY)
                }
            }
        }
    }

    private fun drawTileBitmap(canvas: Canvas, x: Int, y: Int, bitmap: Bitmap) {
        val drawX = x - bitmap.width / 2
        val drawY = y + tileHeight / 2 - bitmap.height
        canvas.drawBitmap(bitmap, drawX.toFloat(), drawY.toFloat(), null)
    }

    private fun drawTileHighlight(canvas: Canvas, x: Int, y: Int) {
        val offsetY = 32
        val path = Path().apply {
            moveTo(x.toFloat(), (y - offsetY).toFloat())
            lineTo((x + tileWidth / 2).toFloat(), (y + tileHeight / 2 - offsetY).toFloat())
            lineTo(x.toFloat(), (y + tileHeight - offsetY).toFloat())
            lineTo((x - tileWidth / 2).toFloat(), (y + tileHeight / 2 - offsetY).toFloat())
            close()
        }

        canvas.drawPath(path, selectedTilePaint)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val (row, col) = screenToIso(event.x, event.y)
            Log.d("IsoMapView", "Touched screen at row=$row col=$col")
            if (row in 0 until visibleSize && col in 0 until visibleSize) {
                selectedRow = row
                selectedCol = col
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
        val offsetY = 100
        val highlightOffsetY = 0

        val tempX = x - offsetX
        val tempY = y - offsetY - highlightOffsetY

        val col = ((tempX / (tileWidth / 2) + tempY / (tileHeight / 2)) / 2).toInt()
        val row = ((tempY / (tileHeight / 2) - tempX / (tileWidth / 2)) / 2).toInt()

        return Pair(row, col)
    }


    /**
     * Desplazamiento suave a velocidad constante, sin importar la distancia.
     */
    fun animatePlayerSmoothlyTo(targetRow: Int, targetCol: Int, speedPerTileMs: Long = 300L) {
        val startRow = playerMapRow
        val startCol = playerMapCol

        val deltaRow = targetRow - startRow
        val deltaCol = targetCol - startCol

        val distance = max(abs(deltaRow), abs(deltaCol))
        if (distance == 0) return

        val duration = speedPerTileMs * distance

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                playerPosRow = startRow + deltaRow * fraction
                playerPosCol = startCol + deltaCol * fraction
                invalidate()
            }
            start()
        }

        playerMapRow = targetRow
        playerMapCol = targetCol
    }

    fun getSelectedRow(): Int = selectedRow
    fun getSelectedCol(): Int = selectedCol
}
