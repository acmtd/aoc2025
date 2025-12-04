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

fun part1(lines: List<String>) = removableRolls(buildGrid(lines)).size

fun part2(lines: List<String>): Int {
    return removeRolls(buildGrid(lines).toMutableSet(), 0)
}

tailrec fun removeRolls(grid: MutableSet<Point>, removedCount: Int): Int {
    val removable = removableRolls(grid)
    if (removable.isEmpty()) return removedCount

    grid.removeAll(removable.toSet())
    return removeRolls(grid, removedCount + removable.size)
}

private fun removableRolls(grid: Set<Point>) = grid.filter { point ->
    point.adjacentPoints().filter { it in grid }.size < 4
}

private fun buildGrid(lines: List<String>) =
    buildSet {
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
