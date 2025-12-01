package day1

import println
import readAsLines

fun main() {
    val testInput = readAsLines("day1/test")
    check(part1(testInput) == 3)

    val puzzleInput = readAsLines("day1/problem")
    part1(puzzleInput).println()

    check(part2(testInput) == 6)
    part2(puzzleInput).println()
}

fun rotate(start: Int, rotation: String): IntProgression {
    val amount = rotation.substring(1).toInt()

    return if (rotation.startsWith("L")) start.downTo(start - amount) else start..start + amount
}

fun part1(rotations: List<String>): Int {
    var dial = 50
    var count = 0

    rotations.forEach { r ->
        dial = rotate(dial, r).last.mod(100)

        if (dial == 0) count++
    }

    return count
}

fun part2(rotations: List<String>): Int {
    var dial = 50
    var count = 0

    rotations.forEach { r ->
        rotate(dial, r).let {
            dial = it.last.mod(100)

            count += it.drop(1).filter { pos -> pos.mod(100) == 0 }.size
        }
    }

    return count
}
