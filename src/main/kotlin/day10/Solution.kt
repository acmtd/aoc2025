package day10

import println
import readAsLines

fun main() {
    val testInput = readAsLines("day10/test")
    check(part1(testInput) == 7)

    val puzzleInput = readAsLines("day10/problem")
    part1(puzzleInput).println()

    check(part2(testInput) == 33)
}

fun part1(input: List<String>): Int {
    val pressesPerLight = input.map { line ->
        val components = line.split(" ")
        val lights = components.first().asLight()
        val schematics = components.drop(1).dropLast(1).map { it.asSchematic().sumOf { num -> 1 shl num } }

        (1..Int.MAX_VALUE).first { presses ->
            schematics.combinations(presses).any { combination ->
                (combination.reduce { acc, i -> acc.xor(i) }) == lights
            }
        }
    }

    return pressesPerLight.sum()
}

fun part2(input: List<String>): Int {
    val result = input.map { line ->
        val components = line.split(" ")

        val buttons = components.drop(1).dropLast(1).map { it.asSchematic() }
        val joltageRequirements = components.last().joltageRequirements()

        val allButtonCombos = joltageRequirements.mapIndexed { counter, amount ->
            val possibleButtonIndices = buttons.indices.filter { counter in buttons[it] }
            part2Combinations(possibleButtonIndices, amount)
        }

        allButtonCombos.reduce { acc, next ->
            buildList {
                for (accMap in acc) {
                    for (nextMap in next) {
                        val sharedButtons = accMap.keys.intersect(nextMap.keys)
                        val compatible = sharedButtons.all { button ->
                            accMap[button] == nextMap[button]
                        }

                        if (compatible) {
                            add(accMap + nextMap)
                        }
                    }
                }
            }.distinct()
                .sortedBy { it.values.sum() }
                .toMutableList()
        }
    }

    return result.sumOf { it.first().values.sum() }
}

fun String.joltageRequirements() = this.drop(1).dropLast(1).split(",").map { it.toInt() }

fun String.asLight() =
    this.drop(1).dropLast(1).map { if (it == '.') 0 else 1 }.reversed().fold(0) { acc, i -> 2 * acc + i }

fun String.asSchematic() = this.drop(1).dropLast(1).split(",").map { it.toInt() }

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

fun part2Combinations(buttonIndices: List<Int>, target: Int): MutableList<Map<Int, Int>> {
    val result = mutableListOf<Map<Int, Int>>()
    val counts = MutableList(buttonIndices.size) { 0 } // Mutable list of zeros

    fun backtrack(index: Int, remaining: Int) {
        if (index == buttonIndices.size - 1) {
            counts[index] = remaining
            val combination = buttonIndices.zip(counts).toMap()
            result.add(combination.toMap())
            return
        }

        for (i in 0..remaining) {
            counts[index] = i
            backtrack(index + 1, remaining - i)
        }
    }

    backtrack(0, target)
    return result
}