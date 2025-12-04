package day4

import println
import readAsLines

fun main() {
    val testInput = readAsLines("day4/test")
    val puzzleInput = readAsLines("day4/problem")

    check(part1(testInput) == 13)
    part1(puzzleInput).println()

    check(part2(testInput) == 43)
    part2(puzzleInput).println()
}

fun part1(lines: List<String>): Int {
    val grid = buildGrid(lines)

    return grid.filter { point ->
        point.adjacentPoints().filter { it in grid }.size < 4
    }.size
}

fun part2(lines: List<String>): Int {
    var grid = buildGrid(lines)

    var removed = 0

    while (true) {
        val removable = grid.filter { point ->
            point.adjacentPoints().filter { it in grid }.size < 4
        }

        if (removable.isEmpty()) {
            return removed
        }
        else {
            removed += removable.size
        }

        grid = grid.filter { !removable.contains(it) }
    }
}

private fun buildGrid(lines: List<String>) =
    buildList {
        lines.forEachIndexed { y, line ->
            line.forEachIndexed { x, c ->
                if (c == '@') {
                    add(Point(x, y))
                }
            }
        }
    }

data class Point(val x: Int, val y: Int) {
    fun adjacentPoints(): Set<Point> {
        return setOf(
            Point(x - 1, y - 1),
            Point(x + 1, y - 1),
            Point(x - 1, y + 1),
            Point(x + 1, y + 1),
            Point(x, y - 1),
            Point(x, y + 1),
            Point(x - 1, y),
            Point(x + 1, y)
        )
    }
}
