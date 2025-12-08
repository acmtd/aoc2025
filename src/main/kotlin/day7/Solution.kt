package day7

import println
import readAsLines
import kotlin.collections.forEach

fun main() {
    val testInput = readAsLines("day7/test")
    check(part1(testInput) == 21)

    val puzzleInput = readAsLines("day7/problem")
    part1(puzzleInput).println()

    check(part2(testInput) == 40L)
    part2(puzzleInput).println()
}

data class Point(val row: Int, val col: Int)
data class State(val start: Point, val splitters: List<Point>)

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

        queue.addAll(nextPoint.adjacentPoints())
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
                timelines++
            } else {
                nextSplitters.add(next)
            }
        }

        splitterStates[splitter] = SplitterState(nextSplitters, timelines)
    }

    val done = mutableSetOf<Point>()

    while (true) {
        if (done.containsAll(state.splitters)) {
            return splitterStates.minBy { it.key.row }.value.timelines
        }

        val solved = splitterStates
            .filterKeys { it !in done }
            .filterValues { it.nextSplitters.isEmpty() }.keys

        solved.forEach { splitter ->
            val unsolved = splitterStates.filterValues { splitter in it.nextSplitters }.keys

            unsolved.forEach { unsolved ->
                val existingState = splitterStates[unsolved]!!
                splitterStates[unsolved] = SplitterState(
                    existingState.nextSplitters - splitter,
                    existingState.timelines + splitterStates[splitter]!!.timelines
                )
            }
        }

        done.addAll(solved)
    }
}

fun nextSplitter(point: Point, splitters: List<Point>): Point? =
    splitters.asSequence()
        .filter { it.col == point.col && it.row > point.row }
        .minByOrNull { it.row }

fun Point.adjacentPoints() = setOf(Point(row, col - 1), Point(row, col + 1))

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

    return State(start, splitters)
}
