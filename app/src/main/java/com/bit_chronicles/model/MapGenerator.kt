package com.bit_chronicles.model

import kotlin.random.Random

object MapGenerator {
    const val MAP_SIZE = 100

    fun generateMap(): Array<IntArray> {
        val map = Array(MAP_SIZE) { IntArray(MAP_SIZE) { 0 } }

        // Llenar el mapa sin jugador
        for (y in 0 until MAP_SIZE) {
            for (x in 0 until MAP_SIZE) {
                map[y][x] = when (Random.nextInt(3)) {
                    0 -> 0 // Pasto
                    1 -> 2 // Enemigo
                    else -> 3 // Colisionable
                }
            }
        }

        return map
    }
}
