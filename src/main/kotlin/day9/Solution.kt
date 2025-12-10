package day9

import com.madgag.gif.fmsware.AnimatedGifEncoder
import println
import readAsLines
import java.awt.AlphaComposite
import kotlin.math.max
import kotlin.math.min

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.collections.any

fun main() {
    val testInput = readAsLines("day9/test")
    check(part1(testInput) == 50L)

    val puzzleInput = readAsLines("day9/problem")
    part1(puzzleInput).println()

    check(part2(testInput) == 24L)
    part2(puzzleInput).println()
//    part2WithVisualization(puzzleInput).println()
}

fun part1(input: List<String>): Long {
    val redTiles = parse(input)

    return redTiles.flatMapIndexed { idx, tile ->
        redTiles.drop(idx + 1).map { other -> tile.rectangleSize(other) }
    }.max()
}

fun part2(input: List<String>): Long {
    val redTiles = parse(input)
    val polygon = redTiles.boundingPolygon()

    // find all the possible rectangles in order of area, biggest to smallest
    val rectangles =
        redTiles.flatMapIndexed { idx, tile ->
            redTiles.drop(idx + 1).map { other -> Rectangle.fromPoints(tile, other) }
        }.sortedBy { it.area() }.reversed()

    // find the first one that sits inside the polygon
    return rectangles.first { rectangle ->
        rectangle.corners().filterNot { it in redTiles }.all { polygon.inside(it) } && !polygon.intersects(rectangle)
    }.area()
}

data class Line(val from: Point, val to: Point) {
    fun isVertical() = from.x == to.x
    fun isHorizontal() = from.y == to.y

    fun fixed(): Long {
        return if (isVertical()) from.x else from.y
    }

    fun range(): LongRange {
        if (isVertical()) return min(from.y, to.y)..max(from.y, to.y)
        return min(from.x, to.x)..max(from.x, to.x)
    }

    fun rangeWithoutEndpoints(): LongRange {
        return range().let { (it.first + 1)..<it.last }
    }
}

data class Rectangle(val minX: Long, val maxX: Long, val minY: Long, val maxY: Long) {
    fun area() = (maxX - minX + 1) * (maxY - minY + 1)
    fun corners() = setOf(Point(minX, minY), Point(maxX, minY), Point(maxX, maxY), Point(minX, maxY))
    fun edges() = setOf(
        Line(Point(minX, minY), Point(maxX, minY)),
        Line(Point(minX, maxY), Point(maxX, maxY)),
        Line(Point(maxX, minY), Point(maxX, maxY)),
        Line(Point(minX, minY), Point(minX, maxY))
    )

    companion object {
        fun fromPoints(p1: Point, p2: Point): Rectangle {
            return Rectangle(min(p1.x, p2.x), max(p1.x, p2.x), min(p1.y, p2.y), max(p1.y, p2.y))
        }
    }
}

data class Polygon(val edges: Set<Line>) {
    fun inside(p: Point): Boolean {
        if (edges.any { p.isOnEdge(it) }) return true

        val crossings = edges.filter { it.isVertical() }.count { line ->
            val yMin = min(line.from.y, line.to.y)
            val yMax = max(line.from.y, line.to.y)
            line.fixed() > p.x && p.y >= yMin && p.y < yMax
        }

        return crossings % 2 == 1
    }

    fun intersects(rectangle: Rectangle): Boolean {
        // Check if any rectangle edge crosses any polygon edge
        for (rectEdge in rectangle.edges()) {
            for (polyEdge in edges) {
                // Check perpendicular crossings only
                if (rectEdge.isVertical() == polyEdge.isVertical()) continue

                // Crossing at interior point (not endpoints)
                if (rectEdge.fixed() in polyEdge.rangeWithoutEndpoints() && polyEdge.fixed() in rectEdge.rangeWithoutEndpoints()) return true
            }
        }

        return false
    }
}

data class Point(val x: Long, val y: Long) {
    fun rectangleSize(other: Point) = Rectangle.fromPoints(this, other).area()
    fun isOnEdge(line: Line): Boolean = when {
        line.isHorizontal() -> y == line.fixed() && x in line.range()
        else -> x == line.fixed() && y in line.range()
    }
}

fun List<Point>.boundingPolygon(): Polygon {
    return Polygon((this + first()).zipWithNext(::Line).toSet())
}

fun parse(lines: List<String>): List<Point> {
    return lines.map { it.split(",") }.map { Point(it[0].toLong(), it[1].toLong()) }
}


// Visualization code (AI written, self-tweaked)
fun part2WithVisualization(input: List<String>): Long {
    val redTiles = parse(input)
    val polygon = redTiles.boundingPolygon()

    val rectangles = redTiles.flatMapIndexed { idx, tile ->
        redTiles.drop(idx + 1).map { other -> Rectangle.fromPoints(tile, other) }
    }.sortedBy { it.area() }.reversed()

    val baseImage = createPolygonImage(polygon)
    val (minX, maxX, minY, maxY, scale) = getPolygonBounds(polygon)

    val encoder = AnimatedGifEncoder()
    encoder.start("day9_search.gif")
    encoder.setDelay(100)
    encoder.setRepeat(-1)

    val answer = rectangles.first { rectangle ->
        rectangle.corners().filterNot { it in redTiles }.all { polygon.inside(it) }
                && !polygon.intersects(rectangle)
    }

    val rectanglesToShow = rectangles.filter { it.corners().all { polygon.inside(it) } }
        .filter { it.area() >= answer.area() }

    val framesToCreate = 30
    val step = max(1, rectanglesToShow.size / framesToCreate)

    rectanglesToShow.forEachIndexed { idx, rectangle ->
        val isAnswer = rectangle == answer

        if (idx % step == 0 || isAnswer) {
            val frame = copyImage(baseImage)
            val g = frame.createGraphics()
            val color = if (isAnswer) Color.GREEN else Color.RED
            drawRectangle(g, rectangle, color, minX, minY, scale, frame.width, frame.height)
            g.dispose()

            encoder.addFrame(frame)
        }
    }

    encoder.finish()
    return answer.area()
}

fun copyImage(source: BufferedImage): BufferedImage {
    val copy = BufferedImage(source.width, source.height, source.type)
    val g = copy.createGraphics()
    g.drawImage(source, 0, 0, null)
    g.dispose()
    return copy
}

data class PolygonBounds(val minX: Long, val maxX: Long, val minY: Long, val maxY: Long, val scale: Int)

fun getPolygonBounds(polygon: Polygon): PolygonBounds {
    val allX = polygon.edges.flatMap { listOf(it.from.x, it.to.x) }
    val allY = polygon.edges.flatMap { listOf(it.from.y, it.to.y) }
    val scale = maxOf(1, maxOf(allX.max() - allX.min(), allY.max() - allY.min()).toInt() / 2000)
    return PolygonBounds(allX.min(), allX.max(), allY.min(), allY.max(), scale)
}

fun createPolygonImage(polygon: Polygon): BufferedImage {
    // Similar to visualizeToImage but return the image
    // Color the rectangle red if invalid, green if valid
    val allX = polygon.edges.flatMap { listOf(it.from.x, it.to.x) }
    val allY = polygon.edges.flatMap { listOf(it.from.y, it.to.y) }

    val minX = allX.min()
    val maxX = allX.max()
    val minY = allY.min()
    val maxY = allY.max()

    val width = (maxX - minX + 1).toInt()
    val height = (maxY - minY + 1).toInt()

    // Scale down if too large
    val scale = maxOf(1, maxOf(width, height) / 2000)
    val scaledWidth = width / scale
    val scaledHeight = height / scale

    val image = BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB)
    val g = image.createGraphics()

    // White background
    g.color = Color.WHITE
    g.fillRect(0, 0, scaledWidth, scaledHeight)

    // Draw polygon in black
    g.color = Color.BLACK
    polygon.edges.forEach { line ->
        if (line.isHorizontal()) {
            line.range().forEach { x ->
                val px = ((x - minX) / scale).toInt()
                val py = ((line.from.y - minY) / scale).toInt()
                if (px in 0 until scaledWidth && py in 0 until scaledHeight) {
                    image.setRGB(px, py, Color.BLACK.rgb)
                }
            }
        } else {
            line.range().forEach { y ->
                val px = ((line.from.x - minX) / scale).toInt()
                val py = ((y - minY) / scale).toInt()
                if (px in 0 until scaledWidth && py in 0 until scaledHeight) {
                    image.setRGB(px, py, Color.BLACK.rgb)
                }
            }
        }
    }

    g.dispose()
    return image
}

fun drawRectangle(
    g: Graphics2D,
    rect: Rectangle,
    color: Color,
    minX: Long,
    minY: Long,
    scale: Int,
    scaledWidth: Int,
    scaledHeight: Int
) {
    // Draw filled rectangle with transparency
    g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f) // 30% opacity
    g.color = color

    val x = ((rect.minX - minX) / scale).toInt()
    val y = ((rect.minY - minY) / scale).toInt()
    val width = ((rect.maxX - rect.minX + 1) / scale).toInt()
    val height = ((rect.maxY - rect.minY + 1) / scale).toInt()

    g.fillRect(x, y, width, height)

    // Draw solid outline
    g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f) // Full opacity
    g.drawRect(x, y, width, height)
}