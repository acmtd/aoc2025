package day8

import println
import readAsLines
import kotlin.collections.forEach
import kotlin.math.absoluteValue

fun main() {
    val testInput = readAsLines("day8/test")
    check(part1(testInput, 10) == 40)

    val puzzleInput = readAsLines("day8/problem")
    part1(puzzleInput, 1000).println()

    check(part2(testInput) == 25272L)
    part2(puzzleInput).println()
}

data class Position(val x: Long, val y: Long, val z: Long) {
    fun distanceTo(other: Position): Long {
        val dx = (this.x - other.x).absoluteValue
        val dy = (this.y - other.y).absoluteValue
        val dz = (this.z - other.z).absoluteValue

        return dx * dx + dy * dy + dz * dz
    }
}

fun part1(lines: List<String>, pairsToConnect: Int): Int {
    val boxes = getBoxes(lines)
    val distanceMap = getDistanceMap(boxes)

    val closestBoxes = distanceMap.entries
        .sortedBy { it.value }
        .take(pairsToConnect)
        .map { it.key }

    val circuitList = mutableListOf(mutableSetOf<Position>())
    closestBoxes.forEach { boxes -> rearrangeCircuits(circuitList, boxes) }

    return circuitList.sortedBy { it.size }.reversed()
        .take(3)
        .map { it.size }
        .reduce(Int::times)
}

fun part2(lines: List<String>): Long {
    val boxes = getBoxes(lines)
    val distanceMap = getDistanceMap(boxes)

    val closestBoxes = distanceMap.entries
        .sortedBy { it.value }
        .map { it.key }

    val circuitList = mutableListOf(mutableSetOf<Position>())

    closestBoxes.forEach { boxPair ->
        rearrangeCircuits(circuitList, boxPair)

        if (circuitList.maxBy { it.size }.size == boxes.size) {
            return boxPair.first.x * boxPair.second.x
        }
    }

    error("Did not manage to connect all circuits")
}

private fun rearrangeCircuits(circuitList: MutableList<MutableSet<Position>>, boxes: Pair<Position, Position>) {
    val relatedCircuits = circuitList.filter { boxes.first in it || boxes.second in it }
    val mergedCircuit = relatedCircuits
        .flatMap { it.asSequence() }
        .toMutableSet()
        .apply {
            add(boxes.first)
            add(boxes.second)
        }

    circuitList.removeAll(relatedCircuits)
    circuitList.add(mergedCircuit)
}

private fun getBoxes(lines: List<String>): List<Position> = lines.map { it.split(",") }
    .map { numbers -> Position(numbers[0].toLong(), numbers[1].toLong(), numbers[2].toLong()) }

private fun getDistanceMap(boxes: List<Position>): Map<Pair<Position, Position>, Long> = buildMap {
    boxes.forEachIndexed { index, box ->
        boxes.drop(index + 1).forEach { otherBox ->
            val distance = box.distanceTo(otherBox)
            put(Pair(box, otherBox), distance)
        }
    }
}