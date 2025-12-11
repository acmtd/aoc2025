package day11

import println
import readAsLines

fun main() {
    val testInput = readAsLines("day11/test")
    check(part1(testInput) == 5)

    val puzzleInput = readAsLines("day11/problem")
    part1(puzzleInput).println()
}

fun part1(input: List<String>): Int {
    val nodeMap = parse(input)

    val queue = ArrayDeque<List<String>>().apply { add(listOf("you")) }

    var outCount = 0

    while (!queue.isEmpty()) {
        val nodes = queue.removeFirst()

        if (nodes.last() == "out") {
            outCount++
        } else {
            val nextNodes = nodeMap.getValue(nodes.last())

            queue.addAll(nextNodes.map { nodes + it })
        }
    }

    return outCount
}

fun parse(input: List<String>): Map<String, List<String>> {
    return buildMap {
        input.forEach { line ->
            val fromNode = line.substringBefore(":")
            val toNodes = line.substringAfter(": ").split(" ")

            put(fromNode, toNodes)
        }
    }
}