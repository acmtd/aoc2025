package day5

import println
import readAsBlocks

fun main() {
    val testInput = readAsBlocks("day5/test")
    val puzzleInput = readAsBlocks("day5/problem")

    check(part1(testInput) == 3)
    part1(puzzleInput).println()

    check(part2(testInput) == 14L)
    part2(puzzleInput).println()
}

fun part1(blocks: List<String>): Int {
    val (ranges, ingredients) = parse(blocks)

    return ingredients.count { ingredient -> ranges.any { ingredient in it } }
}

fun part2(blocks: List<String>): Long {
    val (ranges, _) = parse(blocks)

    return ranges.merge().sumOf { it.last - it.first + 1 }
}

fun parse(blocks: List<String>): Pair<List<LongRange>, List<Long>> {
    val ranges = blocks[0].lines().map { it.substringBefore("-").toLong()..it.substringAfter("-").toLong() }
    val ingredients = blocks[1].lines().map { it.toLong() }

    return ranges to ingredients
}

fun List<LongRange>.merge(): List<LongRange> {
    val sorted = sortedBy { it.first }

    return sorted.fold(emptyList()) { acc, next ->
        if (acc.isEmpty()) {
            acc.plusElement(next)
        } else {
            if (next.first > acc.last().last) {
                // the next range does not overlap with the current one
                acc.plusElement(next)
            } else if (next.last < acc.last().last) {
                // the next range is fully enclosed by the current one
                acc
            } else {
                // partial overlap
                val newRange = acc.last().first..next.last
                acc.dropLast(1).plusElement(newRange)
            }
        }
    }
}