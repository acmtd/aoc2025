package day7

import println
import readAsLines
import kotlin.collections.emptyList
import kotlin.collections.forEach

fun main() {
    val testInput = readAsLines("day7/test")
    check(part1(testInput) == 21)

    val puzzleInput = readAsLines("day7/problem")
    part1(puzzleInput).println()

    check(part2(testInput) == 40L)
    part2(puzzleInput).println() // 923423101 is too low
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

data class SplitterState(val nextSplitters: Set<Point>, val timelines: Long)

fun part2(lines: List<String>): Long {
    val state = parse(lines)

    val splitterStates = state.splitters.associateWith { SplitterState(emptySet(), 0L) }.toMutableMap()

    state.splitters.forEach { splitter ->
        var timelines = 0L
        val nextSplitters = mutableSetOf<Point>()

        splitter.adjacentPoints().forEach { p ->
            val next = nextSplitter(p, state.splitters)

            if (next == null) {
                timelines += 1
            } else {
                nextSplitters.add(next)
            }
        }

        splitterStates[splitter] = SplitterState(nextSplitters, timelines)
    }

    while (true) {
        val solvedSplitters = splitterStates.filter { it.value.nextSplitters.isEmpty() }.keys

        if (solvedSplitters.size == splitterStates.size) {
            return splitterStates.minBy { it.key.row }.value.timelines
        }

        solvedSplitters.forEach { splitter ->
            val unsolvedSplitters = splitterStates.filter { splitter in it.value.nextSplitters }.keys

            unsolvedSplitters.forEach { unsolved ->
                val existingState = splitterStates[unsolved]!!
                val newState = SplitterState(
                    existingState.nextSplitters - splitter,
                    existingState.timelines + splitterStates[splitter]!!.timelines
                )
                splitterStates[unsolved] = newState
            }
        }
    }

}

fun nextSplitter(point: Point, splitters: List<Point>): Point? {
    return splitters.filter { it.col == point.col && it.row > point.row }.minByOrNull { it.row }
}

fun Point.adjacentPoints(): Set<Point> {
    return setOf(
        Point(row, col - 1),
        Point(row, col + 1)
    )
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
