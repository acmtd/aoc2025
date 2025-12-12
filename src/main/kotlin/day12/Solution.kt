package day12

import println
import readAsBlocks

fun main() {
    val puzzleInput = readAsBlocks("day12/problem")
    part1(puzzleInput).println()
}

fun part1(input: List<String>): Int {
    val (presents, goals) = parse(input)

    val presentAreas = presents.map { present -> present.count { it == '#' } }
    val totalAreas = presents.map { present -> present.count { it in "#." } }

    val (definitely, maybe) = goals.partition { (area, counts) ->
        // if it would still fit if the "." were all "#" then it's definitely ok, however
        // need to modify the target area to only consider 3x3 blocks instead of the entire space
        val availableArea = (area.first() / 3) * (area.last() / 3) * 9
        val maxSpaceUsed = counts.indices.sumOf { i -> counts[i] * totalAreas[i] }

        maxSpaceUsed <= availableArea
    }.let { (possible, unknown) ->
        val maybeCount = unknown.count { (area, counts) ->
            // if the available space has enough room for all the "#" then it's at least a maybe
            val availableArea = area.reduce(Int::times)
            val minimumRequired = counts.indices.sumOf { i -> counts[i] * presentAreas[i] }
            minimumRequired <= availableArea
        }

        possible.size to maybeCount
    }

    return when {
        maybe == 0 -> definitely
        else -> error("Need to code box packing algorithm to determine which presents can fit")
    }
}

fun parse(blocks: List<String>): Pair<List<String>, List<Pair<List<Int>, List<Int>>>> {
    val presents = blocks.dropLast(1)

    val goals = blocks.last().split("\n").map { line ->
        val (areaStr, countsStr) = line.split(": ")
        val area = areaStr.split("x").map(String::toInt)
        val counts = countsStr.split(" ").map { num -> num.toInt() }

        area to counts
    }

    return presents to goals
}