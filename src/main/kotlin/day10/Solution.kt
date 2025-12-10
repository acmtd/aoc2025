package day10

import println
import readAsLines

fun main() {
    val testInput = readAsLines("day10/test")
    check(part1(testInput) == 7)

    val puzzleInput = readAsLines("day10/problem")
    part1(puzzleInput).println()
}

fun part1(input: List<String>): Int {
    val data = input.map { line ->
        val components = line.split(" ")
        val lights = components.first().asLight()
        val schematics = components.drop(1).dropLast(1).map { it.asSchematic() }

        Pair(lights, schematics)
    }

    val pressesPerLight = data.map { (lights, schematics) ->
        (1..Int.MAX_VALUE).first { presses ->
            schematics.combinations(presses).any { combination ->
                (combination.reduce { acc, i -> acc.xor(i) }) == lights
            }
        }
    }

    return pressesPerLight.sum()
}

fun String.asLight(): Int {
    // example [.##.]
    val binaryDigits = this.drop(1).dropLast(1).map { if (it == '.') 0 else 1 }
    return binaryDigits.reversed().fold(0) { acc, i -> 2 * acc + i }
}

fun String.asSchematic(): Int {
    // example (1,3)
    val digits = this.drop(1).dropLast(1).split(",").map { it.toInt() }
    return digits.sumOf { 1 shl it }
}

fun <T> List<T>.combinations(size: Int): List<List<T>> {
    if (size > this.size) return emptyList()

    val result = mutableListOf<List<T>>()

    fun generate(start: Int, current: MutableList<T>) {
        if (current.size == size) {
            result.add(current.toList())
            return
        }

        for (i in start until this.size) {
            current.add(this[i])
            generate(i + 1, current)
            current.removeAt(current.size - 1)
        }
    }

    generate(0, mutableListOf())
    return result
}