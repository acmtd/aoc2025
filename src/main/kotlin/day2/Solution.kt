package day2

import println
import readAsString

fun main() {
    val testInput = readAsString("/day2/test")
    val puzzleInput = readAsString("/day2/problem")

    check(part1(testInput) == 1227775554L)
    part1(puzzleInput).println()

    check(part2(testInput) == 4174379265L)
    part2(puzzleInput).println()
}

fun part1(input: String) = ranges(input).flatMap { it.invalidProductIDs() }.sum()
fun part2(input: String) = ranges(input).flatMap { it.invalidProductIDsPart2() }.sum()

private fun LongRange.invalidProductIDs() =
    this.filter { it.toString().matches(Regex("^(\\d+)\\1$")) }

private fun LongRange.invalidProductIDsPart2() =
    this.filter { it.toString().matches(Regex("^(\\d+)\\1+$")) }

private fun ranges(input: String) =
    input.split(",")
        .map { it.substringBefore("-").toLong()..it.substringAfter("-").toLong() }

