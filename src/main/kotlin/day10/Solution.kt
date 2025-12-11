package day10

import println
import readAsLines
import com.microsoft.z3.*

fun main() {
    val testInput = readAsLines("day10/test")
    check(part1(testInput) == 7)

    val puzzleInput = readAsLines("day10/problem")
    part1(puzzleInput).println()

    check(part2_FirstTry(testInput) == 33)
    check(part2_SecondTry(testInput) == 33)
    check(part2_Z3(testInput) == 33)

    part2_Z3(puzzleInput).println()
}

fun String.joltageRequirements() = this.drop(1).dropLast(1).split(",").map { it.toInt() }

fun String.asLight() =
    this.drop(1).dropLast(1).map { if (it == '.') 0 else 1 }.reversed().fold(0) { acc, i -> 2 * acc + i }

fun String.asSchematic() = this.drop(1).dropLast(1).split(",").map { it.toInt() }

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

// So this is what it's come down to: I tried two algorithms that felt like smart approaches
// but both of them ran into exponential complexity and wouldn't solve any of the real puzzle
// input in reasonable time. The r/adventofcode subreddit mentioned the Z3 solver, so I
// gave it a try with a little AI help. Not thrilled with having to do it this way but I'm
// not smart enough to figure out a third way.
fun part2_Z3(input: List<String>): Int {
    val result = input.map { line ->
        val components = line.split(" ")
        val buttons = components.drop(1).dropLast(1).map { it.asSchematic() }
        val joltageRequirements = components.last().joltageRequirements()

        solveWithZ3(buttons, joltageRequirements)
    }

    return result.sum()
}

//Common Z3 methods cheat sheet:
//
//mkIntConst(name) - create an integer variable
//mkInt(value) - create an integer literal (constant value)
//mkAdd(expr1, expr2, ...) - addition: expr1 + expr2 + ...
//mkSub(expr1, expr2) - subtraction: expr1 - expr2
//mkMul(expr1, expr2, ...) - multiplication: expr1 * expr2 * ...
//mkEq(expr1, expr2) - equality: expr1 == expr2
//mkGe(expr1, expr2) - greater-or-equal: expr1 >= expr2
//mkGt(expr1, expr2) - greater-than: expr1 > expr2
//mkLe(expr1, expr2) - less-or-equal: expr1 <= expr2
//mkLt(expr1, expr2) - less-than: expr1 < expr2
//
//The "mk" prefix stands for "make" - you're constructing expressions/constraints that Z3 will reason about.
fun solveWithZ3(buttons: List<List<Int>>, targets: List<Int>): Int {
    // Z3 Context is the main entry point - it manages all Z3 objects
    // We use 'use' to ensure proper cleanup when done
    Context().use { ctx ->

        // An Optimizer is a special Z3 solver that can minimize/maximize objectives
        // (vs a regular Solver which just finds any satisfying solution)
        val optimizer = ctx.mkOptimize()

        // ===== STEP 1: Create decision variables =====
        // We need one integer variable per button representing "how many times do we press it?"
        // mkIntConst creates an integer constant (a variable that Z3 will solve for)
        val buttonPresses = buttons.indices.map { i ->
            ctx.mkIntConst("button_$i")  // The string is just for debugging/display
        }

        // ===== STEP 2: Add non-negativity constraints =====
        // We can't press a button negative times, so each variable must be >= 0
        buttonPresses.forEach { buttonVar ->
            // mkGe = "make greater-than-or-equal-to constraint"
            // mkInt(0) creates the integer literal 0
            // So this reads as: "buttonVar >= 0"
            optimizer.Add(ctx.mkGe(buttonVar, ctx.mkInt(0)))
        }

        // ===== STEP 3: Add equality constraints for each counter =====
        // For each counter/register, the sum of all button contributions must equal the target
        targets.indices.forEach { counterIdx ->

            // Find which buttons affect this counter
            val contributingButtons = buttons.indices.filter { btnIdx ->
                counterIdx in buttons[btnIdx]
            }

            if (contributingButtons.isNotEmpty()) {
                // Create an expression representing the sum of all contributing button presses
                // mkAdd creates an addition expression
                // We use the spread operator * to pass the array as varargs
                val sum = ctx.mkAdd(
                    *contributingButtons.map { buttonPresses[it] }.toTypedArray()
                )

                // mkEq = "make equality constraint"
                // This reads as: "sum == targets[counterIdx]"
                optimizer.Add(ctx.mkEq(sum, ctx.mkInt(targets[counterIdx])))
            }
        }

        // ===== STEP 4: Set the optimization objective =====
        // We want to MINIMIZE the total number of button presses
        val totalPresses = ctx.mkAdd(*buttonPresses.toTypedArray())

        // MkMinimize tells the optimizer what to minimize
        // (there's also MkMaximize for maximization problems)
        optimizer.MkMinimize(totalPresses)

        // ===== STEP 5: Solve and extract the result =====
        // Check() runs the solver and returns the status
        return when (optimizer.Check()) {
            Status.SATISFIABLE -> {
                // SATISFIABLE means Z3 found a solution

                // The model contains the actual values Z3 found for our variables
                val model = optimizer.model

                // For each button variable, ask the model what value it assigned
                // eval() evaluates the variable in the context of the model
                // The 'false' parameter means "don't do model completion" (technical detail)
                buttonPresses.sumOf { buttonVar ->
                    model.eval(buttonVar, false).toString().toInt()
                }
            }

            Status.UNSATISFIABLE -> {
                // UNSATISFIABLE means no solution exists (shouldn't happen for valid input)
                Int.MAX_VALUE
            }

            Status.UNKNOWN -> {
                // UNKNOWN means Z3 couldn't determine satisfiability (timeout, etc.)
                Int.MAX_VALUE
            }

            else -> Int.MAX_VALUE
        }
    }
}

fun part2_FirstTry(input: List<String>): Int {
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

fun part2_SecondTry(input: List<String>): Int {
    data class State(val presses: Int, val levels: IntArray, val remainingButtons: List<List<Int>>)

    val result = input.map { line ->
        val components = line.split(" ")
        val buttons = components.drop(1).dropLast(1).map { it.asSchematic() }
        val joltageRequirements = components.last().joltageRequirements().toIntArray()

        val initialState = State(0, IntArray(joltageRequirements.size), buttons)
        val queue = ArrayDeque<State>().apply { add(initialState) }
        var minPresses = Int.MAX_VALUE

        while (queue.isNotEmpty()) {
            val state = queue.removeFirst()

            if (state.presses >= minPresses) continue

            if (state.levels.contentEquals(joltageRequirements)) {
                minPresses = state.presses
            } else if (state.remainingButtons.isNotEmpty()) {
                val buttonToPress = state.remainingButtons.first()

                val maxPossiblePresses = buttonToPress.minOf { counter ->
                    maxOf(0, joltageRequirements[counter] - state.levels[counter])
                }

                val newStates = (0..maxPossiblePresses).map { presses ->
                    val newLevels = state.levels.clone()
                    buttonToPress.forEach { counter -> newLevels[counter] += presses }
                    State(state.presses + presses, newLevels, state.remainingButtons.drop(1))
                }

                queue.addAll(newStates)
            }
        }

        minPresses
    }

    return result.sum()
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