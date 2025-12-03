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
    return banks.sumOf { it.highestJoltage() }
}

fun part2(banks: List<String>): Long {
    return banks.sumOf { it.highestTwelveBatteryJoltage() }
}

private fun String.highestJoltage(): Int {
    val batteries = this.chunked(1).map { it.toInt() }

    val first = batteries.dropLast(1).max()
    val firstPos = batteries.indexOfFirst { it == first }

    val last = batteries.drop(firstPos + 1).max()

    return first * 10 + last
}

private fun String.highestTwelveBatteryJoltage(): Long {
    var batteries = this.chunked(1).map { it.toInt() }

    val maxDigits = 12
    var digitsRemaining = maxDigits

    val chosenBatteries = mutableListOf<Int>()

    while (digitsRemaining > 0) {
        val highest = batteries.dropLast(digitsRemaining - 1).max()
        val highestPos = batteries.indexOfFirst { it == highest }

        batteries = batteries.drop(highestPos + 1)

        chosenBatteries.add(highest)
        digitsRemaining--
    }

    return chosenBatteries.joinToString("") { it.toString() }.toLong()
}
