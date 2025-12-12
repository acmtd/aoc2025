package day12

import println
import readAsBlocks

fun main() {
    val puzzleInput = readAsBlocks("day12/problem")
    part1(puzzleInput).println()
}

fun part1(input: List<String>): Int {
    val (presents, goals) = parse(input)

    val presentAreas = presents.map { p -> p.count { it == '#' } }

    return goals.count { (area, counts) ->
        val availableArea = area.reduce(Int::times)

        val requiredMinimumArea = counts.mapIndexed { idx, count ->
            count * presentAreas[idx]
        }.sum()

        (requiredMinimumArea <= availableArea)
    }
}

fun parse(blocks: List<String>): Pair<List<String>, List<Pair<List<Int>, List<Int>>>> {
    val presents = blocks.dropLast(1)

    val goals = blocks.last().split("\n").map { line ->
        val area = line.substringBefore(":").split("x").map(String::toInt)
        val counts = line.substringAfter(": ").split(" ").map { num -> num.toInt() }

        area to counts
    }

    return presents to goals
}