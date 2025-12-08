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
data class SplitterState(val nextSplitters: Set<Point>, val timelines: Long)

fun part1(lines: List<String>): Int {
    val state = parse(lines)
    val queue = ArrayDeque<Point>()
    queue.add(state.start)

    val visited = mutableListOf<Point>()

    while (queue.isNotEmpty()) {
        val point = queue.removeFirst()
        val next = nextSplitter(point, state.splitters) ?: continue

        if (next in visited) continue

        visited += next
        queue += next.adjacentPoints()
    }

    return visited.size
}

fun part2(lines: List<String>): Long {
    val state = parse(lines)
    val states = state.splitters.associateWith { computeSplitterState(it, state.splitters) }.toMutableMap()
    val done = mutableSetOf<Point>()

    while (done.size < state.splitters.size) {
        val solved = states
            .filterKeys { it !in done }
            .filterValues { it.nextSplitters.isEmpty() }

        solved.forEach { (solvedSplitter, solvedState) ->
            states.filterValues { solvedSplitter in it.nextSplitters }
                .forEach { (unsolvedSplitter, unsolvedState) ->
                    states[unsolvedSplitter] =
                        SplitterState(
                            unsolvedState.nextSplitters - solvedSplitter,
                            unsolvedState.timelines + solvedState.timelines
                        )
                }
        }

        done += solved.keys
    }

    return states.minBy { it.key.row }.value.timelines
}

fun computeSplitterState(splitter: Point, splitters: List<Point>): SplitterState {
    val nextSplitters = mutableSetOf<Point>()
    var timelines = 0L

    for (adj in splitter.adjacentPoints()) {
        val next = nextSplitter(adj, splitters)
        if (next == null) {
            timelines++
        } else {
            nextSplitters += next
        }
    }

    return SplitterState(nextSplitters, timelines)
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
