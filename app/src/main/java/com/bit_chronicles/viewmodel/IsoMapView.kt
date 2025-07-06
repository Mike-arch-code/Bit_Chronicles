package com.bit_chronicles.viewmodel

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.bit_chronicles.model.MapGenerator

class IsoMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val tileWidth = 128
    private val tileHeight = 64
    private val visibleSize = 5

    private val paintGrass = Paint().apply { color = Color.parseColor("#8BC34A") }
    private val paintEnemy = Paint().apply { color = Color.parseColor("#FF8A65") }
    private val paintObstacle = Paint().apply { color = Color.parseColor("#B0BEC5") }
    private val paintPlayer = Paint().apply { color = Color.BLUE }

    private val selectedTilePaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private var selectedRow = -1
    private var selectedCol = -1

    private val map = MapGenerator.generateMap()

    // Posición del jugador en el mapa global
    private var playerMapRow = 50
    private var playerMapCol = 50

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val offsetX = width / 2
        val offsetY = 100

        for (row in 0 until visibleSize) {
            for (col in 0 until visibleSize) {
                val mapRow = playerMapRow - 2 + row
                val mapCol = playerMapCol - 2 + col

                val x = (col - row) * tileWidth / 2 + offsetX
                val y = (col + row) * tileHeight / 2 + offsetY

                val isPlayerPosition = row == 2 && col == 2

                val paint = when {
                    isPlayerPosition -> paintPlayer
                    map.getOrNull(mapRow)?.getOrNull(mapCol) == 2 -> paintEnemy
                    map.getOrNull(mapRow)?.getOrNull(mapCol) == 3 -> paintObstacle
                    else -> paintGrass
                }

                drawTile(canvas, x, y, paint)

                if (row == selectedRow && col == selectedCol) {
                    drawTileHighlight(canvas, x, y)
                }
            }
        }
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
        val offsetY = 100  // <- ahora coincide con onDraw()

        val tempX = x - offsetX
        val tempY = y - offsetY

        val col = ((tempY / tileHeight) + (tempX / tileWidth)).toInt()
        val row = ((tempY / tileHeight) - (tempX / tileWidth)).toInt()

        return Pair(row, col)
    }


    fun movePlayerBy(deltaRow: Int, deltaCol: Int) {
        val newRow = playerMapRow + deltaRow
        val newCol = playerMapCol + deltaCol
        if (newRow in 0 until MapGenerator.MAP_SIZE && newCol in 0 until MapGenerator.MAP_SIZE) {
            playerMapRow = newRow
            playerMapCol = newCol
            invalidate()
        }
    }
    fun getSelectedRow(): Int {
        return selectedRow
    }

    fun getSelectedCol(): Int {
        return selectedCol
    }
}
