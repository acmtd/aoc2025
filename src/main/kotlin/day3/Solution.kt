package day3

import println
import readAsLines

fun main() {
    val testInput = readAsLines("day3/test")
    val puzzleInput = readAsLines("day3/problem")

    check(part1(testInput) == 357)
    part1(puzzleInput).println()

    check(part2(testInput) == 3121910778619L)
    part2(puzzleInput).println()
}

fun part1(banks: List<String>): Int {
    return banks.sumOf { it.highestBatteryJoltage(2) }.toInt()
}

fun part2(banks: List<String>): Long {
    return banks.sumOf { it.highestBatteryJoltage(12) }
}

private fun String.highestBatteryJoltage(batteryCount: Int): Long {
    var batteries = this.map { it.digitToInt() }
    var digitsRemaining = batteryCount

    return buildList {
        while (digitsRemaining > 0) {
            val highest = batteries.dropLast(digitsRemaining - 1).max()
            val highestPos = batteries.indexOfFirst { it == highest }
            batteries = batteries.drop(highestPos + 1)

            add(highest)
            digitsRemaining--
        }
    }.joinToString("") { it.toString() }.toLong()
}