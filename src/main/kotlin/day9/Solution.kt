package day9

import println
import readAsLines
import kotlin.math.absoluteValue

fun main() {
    val testInput = readAsLines("day9/test")
    check(part1(testInput) == 50L)

    val puzzleInput = readAsLines("day9/problem")
    part1(puzzleInput).println()
}

data class Point(val x: Long, val y: Long) {
    fun rectangleSize(other: Point): Long {
        return (1 + (x - other.x).absoluteValue) * (1 + (y - other.y).absoluteValue)
    }
}

fun parse(lines: List<String>): List<Point> {
    return lines.map { it.split(",") }.map { Point(it[0].toLong(), it[1].toLong()) }
}

fun part1(input: List<String>): Long {
    val tiles = parse(input)

    return tiles.flatMapIndexed { idx, tile ->
        tiles.drop(idx + 1).map { other -> tile.rectangleSize(other) }
    }.max()
}