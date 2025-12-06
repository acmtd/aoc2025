package day6

import println
import readAsLines

fun main() {
    val testInput = readAsLines("day6/test")
    check(part1(testInput) == 4277556L)

    val puzzleInput = readAsLines("day6/problem")
    part1(puzzleInput).println()

    check(part2(testInput) == 3263827L)
    part2(puzzleInput).println()
}

fun parse(input: List<String>): Pair<List<String>, List<List<String>>> {
    val operators = input.last().split("\\s+".toRegex())
    val operands = input.dropLast(1).map { it.trim().split("\\s+".toRegex()) }

    return operators to operands
}

fun parsePart2(input: List<String>): Pair<List<String>, List<List<String>>> {
    val operatorRow = input.last()
    val operators = operatorRow.split("\\s+".toRegex())

    val operatorPositions = operatorRow.withIndex()
        .filter { (_, c) -> c == '*' || c == '+' }
        .map { (idx, _) -> idx }
        .toMutableList()

    val operandLines = input.dropLast(1)

    operatorPositions.add(operandLines.maxOf { it.length } + 1)

    val allOperands = buildList {
        operatorPositions.zipWithNext { start, next ->
            var pos = start

            val operands = buildList {
                while (pos < next-1) {
                    val number =
                        operandLines.joinToString("") { if (it.length < (pos+1)) "" else it.substring(pos, pos+1) }.trim()

                    if (number.isNotEmpty()) add(number)
                    pos++
                }
            }

            add(operands)
        }
    }

    return operators to allOperands
}

fun part1(input: List<String>): Long {
    val (operators, operands) = parse(input)

    return compute(operators, operands)
}

private fun compute(
    operators: List<String>,
    operands: List<List<String>>
): Long {
    val computations = operators.mapIndexed { index, operator ->
        val numbers = operands.map { it[index].toLong() }

        when (operator) {
            "+" -> numbers.sum()
            "*" -> numbers.fold(1L) { acc, next -> acc * next }
            else -> throw IllegalArgumentException("Unsupported operator: $operator")
        }
    }

    return computations.sum()
}

private fun computePart2(
    operators: List<String>,
    operands: List<List<String>>
): Long {
    val computations = operators.mapIndexed { index, operator ->
        val numbers = operands[index].map { it.toLong() }

        when (operator) {
            "+" -> numbers.sum()
            "*" -> numbers.fold(1L) { acc, next -> acc * next }
            else -> throw IllegalArgumentException("Unsupported operator: $operator")
        }
    }

    return computations.sum()
}

fun part2(input: List<String>): Long {
    val (operators, operands) = parsePart2(input)
    return computePart2(operators, operands)
}
