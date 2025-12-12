package day11

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Font
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.Factory.mutGraph
import guru.nidi.graphviz.model.Factory.mutNode
import println
import readAsLines
import java.io.File

fun main() {
    val testInput = readAsLines("day11/test")
    check(part1(testInput) == 5)

    val puzzleInput = readAsLines("day11/problem")
    part1(puzzleInput).println()
    part2(puzzleInput).println()
}

fun part1(input: List<String>): Int {
    val nodeMap = parse(input)
    return pathsFrom(nodeMap, "you", listOf("out")).getValue("out").toInt()
}

fun part2(input: List<String>): Long {
    val nodeMap = parse(input)

    // use drawGraph to find the bottlenecks - then split into chunks to keep the calculations manageable
    drawGraph(nodeMap, listOf("svr", "fft", "dac", "out"))

    val layers = listOf(
        listOf("svr") to null,
        listOf("xed", "kxy", "nju") to null,
        listOf("oyz", "yhv", "qlj", "uyb") to "fft",
        listOf("mfc", "bjj", "xyw", "cin") to null,
        listOf("cub", "jcy", "ooa", "bid") to null,
        listOf("you", "cjp", "tgk", "ykg") to "dac",
        listOf("out") to null
    )

    var pathCountsPerTarget = mapOf("svr" to 1L)

    layers.zipWithNext { from, to ->
        val (fromNodes, _) = from
        val (toNodes, via) = to

        val pathCounts = fromNodes.map { fromNode ->
            pathsFrom(nodeMap, fromNode, toNodes, via, pathCountsPerTarget.getValue(fromNode))
        }

        pathCountsPerTarget = pathCounts.flatMap { it.entries }
            .groupingBy { it.key }
            .fold(0L) { acc, entry -> acc + entry.value }
    }

    return pathCountsPerTarget.values.sum()
}

private fun pathsFrom(
    nodeMap: Map<String, List<String>>,
    from: String,
    to: List<String>,
    via: String? = null,
    multiplier: Long = 1L
): Map<String, Long> {
    val queue = ArrayDeque<Pair<String, Boolean>>().apply { add(from to false) }
    val countsPerEndpoint = mutableMapOf<String, Long>()

    while (queue.isNotEmpty()) {
        val (node, routeOk) = queue.removeFirst()

        if (node in to) {
            if (routeOk || via == null) {
                countsPerEndpoint.merge(node, 1L, Long::plus)
            }
        } else {
            nodeMap[node]?.forEach { nextNode ->
                queue.add(nextNode to (routeOk || node == via))
            }
        }
    }

    return countsPerEndpoint.mapValues { it.value * multiplier }
}

fun parse(input: List<String>): Map<String, List<String>> {
    return input.associate { line ->
        val fromNode = line.substringBefore(":")
        val toNodes = line.substringAfter(": ").split(" ")
        fromNode to toNodes
    }
}

fun drawGraph(nodeMap: Map<String, List<String>>, specialNodes: List<String>) {
    val g = mutGraph("Day11").setDirected(true)
        .nodeAttrs().add(Color.CYAN, Font.name("Helvetica"))
        .linkAttrs().add(Color.GRAY)

    nodeMap.forEach { (from, toList) ->
        val fromNode = mutNode(from).apply {
            if (from in specialNodes) add(Color.RED.fill(), Style.FILLED)
        }

        toList.forEach { to ->
            val toNode = mutNode(to).apply {
                if (to in specialNodes) add(Color.RED.fill(), Style.FILLED)
            }
            fromNode.addLink(toNode)
        }
        g.add(fromNode)
    }

    Graphviz.fromGraph(g).width(3000).render(Format.PNG).toFile(File("day11_graph.png"))
}