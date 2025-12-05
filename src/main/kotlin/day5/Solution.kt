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

    return mergeAll(ranges).sumOf { it.last - it.first + 1 }
}

fun parse(blocks: List<String>): Pair<List<LongRange>, List<Long>> {
    val ranges = blocks[0].lines().map { it.substringBefore("-").toLong()..it.substringAfter("-").toLong() }
    val ingredients = blocks[1].lines().map { it.toLong() }

    return ranges to ingredients
}

fun mergeAll(ranges: List<LongRange>): List<LongRange> {
    if (ranges.isEmpty()) return emptyList()

    val sorted = ranges.sortedBy { it.first }
    val result = mutableListOf<LongRange>()

    var current = sorted[0]

    for (i in 1 until sorted.size) {
        val next = sorted[i]
        val merged = current.merge(next)

        if (merged != null) {
            current = merged
        } else {
            result.add(current)
            current = next
        }
    }
    result.add(current)
    return result
}

fun LongRange.merge(other: LongRange): LongRange? {
    if (this.first <= other.last + 1 && other.first <= this.last + 1) {
        return minOf(this.first, other.first)..maxOf(this.last, other.last)
    }
    return null
}