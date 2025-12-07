package day7

import println
import readAsLines

fun main() {
    val testInput = readAsLines("day7/test")
    check(part1(testInput) == 21)

    val puzzleInput = readAsLines("day7/problem")
    part1(puzzleInput).println()
}

data class Point(val row: Int, val col: Int)
data class State(val start: Point, val splitters: List<Point>, val rowCount: Int, val colCount: Int)

fun part1(lines: List<String>): Int {
    val state = parse(lines)

    val queue = ArrayDeque<Point>()
    queue.addLast(state.start)

    val visitedSplitters = mutableListOf<Point>()

    while (queue.isNotEmpty()) {
        val point = queue.removeFirst()

        val nextPoint = nextSplitter(point, state.splitters) ?: continue

        if (visitedSplitters.contains(nextPoint)) continue
        visitedSplitters.add(nextPoint)

        queue.add(Point(nextPoint.row, nextPoint.col - 1))
        queue.add(Point(nextPoint.row, nextPoint.col + 1))
    }

    return visitedSplitters.size
}

fun nextSplitter(point: Point, splitters: List<Point>): Point? {
    return splitters.filter { it.col == point.col && it.row > point.row }.minByOrNull { it.row }
}

fun parse(lines: List<String>): State {
    var start = Point(0, 0)

    val splitters = buildList {
        lines.forEachIndexed { row, line ->
            line.forEachIndexed { col, char ->
                if (char == 'S') {
                    start = Point(row, col)
                } else if (char == '^') {
                    add(Point(row, col))
                }
            }
        }
    }

    return State(start, splitters, lines.size, lines[0].length)
}
