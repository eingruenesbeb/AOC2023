enum class MapDirection(val directionVector: Pair<Int, Int>) {
    NORTH(0 to 1),
    EAST(1 to 0),
    SOUTH(0 to -1),
    WEST(-1 to 0);

    companion object {
        fun fromDirectionVector(vector: Pair<Int, Int>) = entries.first { it.directionVector == vector }
    }
}

operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
    this.first + other.first to this.second + other.second

operator fun Pair<Int, Int>.unaryMinus(): Pair<Int, Int> = -first to -second

fun <T> List<List<T>>.transposed(): List<List<T>> {
    assert(this.map { it.size }.all { rowSize -> rowSize == this.maxOf { it.size } }) { "List must be rectangular!" }
    return buildList {
        this@transposed.first().indices.forEach {  rowIndex ->
            this.add(this@transposed.map { it[rowIndex] })
        }
    }
}

enum class Pipe(
    val charRepresentation: Char,
    val endPoints: List<MapDirection>
) : TwoDimGrid.GridElement {
    VERTICAL('|', listOf(MapDirection.NORTH, MapDirection.SOUTH)),
    HORIZONTAL('-', listOf(MapDirection.WEST, MapDirection.EAST)),
    BEND_NORTH_EAST('L', listOf(MapDirection.NORTH, MapDirection.EAST)),
    BEND_NORTH_WEST('J', listOf(MapDirection.NORTH, MapDirection.WEST)),
    BEND_SOUTH_EAST('F', listOf(MapDirection.SOUTH, MapDirection.EAST)),
    BEND_SOUTH_WEST('7', listOf(MapDirection.SOUTH, MapDirection.WEST)),
    START('S', listOf(MapDirection.NORTH, MapDirection.EAST, MapDirection.SOUTH, MapDirection.WEST)),
    GROUND('.', listOf()),
    VOID('x', listOf());

    fun canConnectTo(other: Pipe, relativePosition: Pair<Int, Int>) =
        relativePosition in endPoints.map { it.directionVector } && -relativePosition in other.endPoints.map { it.directionVector }

    companion object {
        fun fromChar(char: Char) = entries.first { it.charRepresentation == char }
    }
}

class PipeGrid : TwoDimGrid<Pipe> {
    private val startPipePosition: Pair<Int, Int>

    override val height: Int
    override val width: Int
    override val elements: List<List<Pipe>>
    val loop: List<TwoDimensionalIndexedValue<Pipe>>

    constructor(lineRepresentation: List<String>) {
        height = lineRepresentation.size
        width = lineRepresentation.maxOf { it.length }
        var toStartPipePosition: Pair<Int, Int>? = null
        elements = lineRepresentation.reversed().mapIndexed { yIndex, line ->
            line.padEnd(width - line.length, Pipe.GROUND.charRepresentation).toCharArray().mapIndexed { xIndex, char ->
                Pipe.fromChar(char).also {
                    if (it == Pipe.START) toStartPipePosition = xIndex to yIndex
                }
            }
        }
        startPipePosition = toStartPipePosition ?: throw IllegalArgumentException("No starting pipe found!")
        loop = initLoop()
    }

    constructor(elements: List<List<Pipe>>, @Suppress("UNUSED_PARAMETER") ignoreThis: Unit = Unit) {
        height = elements.size
        width = elements.first().size
        this.elements = elements

        var toStartPipePosition = -1 to -1
        elements.forEachIndexed { yIndex, row ->
            if (row.contains(Pipe.START)) toStartPipePosition = row.indexOf(Pipe.START) to yIndex
        }
        if (toStartPipePosition.first < 0 || toStartPipePosition.second < 0) throw IllegalArgumentException("Pipe-loop has no start.")
        startPipePosition = toStartPipePosition

        loop = initLoop()
    }

    fun groundElementsEnclosedByLoop(): Int {
        val expandedOnlyLoopAndGroundGrid = loopAndGroundOnly().expanded()
        val expandedGroundOrVoidRegions = GroundOrVoidRegion.fromElements(expandedOnlyLoopAndGroundGrid.elements)

        return expandedGroundOrVoidRegions.filter { region ->
            region.elements.all {
                it.twoDimIndex.first in 1..<(expandedOnlyLoopAndGroundGrid.width - 1) && it.twoDimIndex.second in 1..<(expandedOnlyLoopAndGroundGrid.height - 1)
            }
        }.flatMap { region -> region.elements.filter { it.value == Pipe.GROUND } }.count()
    }

    // Any non-ground type pipes that are not connected to the loop are treated as ground.
    private fun loopAndGroundOnly(): PipeGrid {
        val loopCoordinates = loop.map { it.twoDimIndex }
        return PipeGrid(elements.mapIndexed { yIndex, row ->
            row.mapIndexed { xIndex, pipe ->
                if (xIndex to yIndex in loopCoordinates) pipe else Pipe.GROUND
            }
        })
    }
    
    private fun expanded(): PipeGrid {
        val horizontalExpansionStep = expandHorizontal(elements) { pipe: Pipe, followingPipe: Pipe ->
            if (pipe.canConnectTo(followingPipe, MapDirection.EAST.directionVector)) Pipe.HORIZONTAL else Pipe.VOID
        }
        
        return PipeGrid(expandHorizontal(horizontalExpansionStep.transposed()) { pipe: Pipe, followingPipe: Pipe ->
            if (pipe.canConnectTo(followingPipe, MapDirection.NORTH.directionVector)) Pipe.VERTICAL else Pipe.VOID
        }.transposed())
    }
    
    private fun expandHorizontal(gridElements: List<List<Pipe>>, expandWith: (Pipe, Pipe) -> Pipe): List<List<Pipe>> = gridElements.map {
        buildList {
            val iterator = it.listIterator()
            while (iterator.hasNext()) {
                val pipe = iterator.next()
                this.add(pipe)

                if (iterator.hasNext()) {
                    val nextPipe = iterator.next()
                    val intermediary = expandWith(pipe, nextPipe)
                    this.add(intermediary)
                    iterator.previous()
                }
            }
        }
    }

    private fun initLoop(): List<TwoDimensionalIndexedValue<Pipe>> {
        val indexedStartPipe = TwoDimensionalIndexedValue(startPipePosition, this[startPipePosition])
        val loop = mutableListOf(indexedStartPipe)
        var currentEdges = listOf<Pair<TwoDimensionalIndexedValue<Pipe>, MapDirection?>>(
            indexedStartPipe to null
        )

        while (currentEdges.map { it.first }.distinct().size == currentEdges.size) {
            val newEdges = currentEdges.flatMap { branchOut(it.first.twoDimIndex, it.second) }

            loop.addAll(newEdges.distinct().map { it.first })
            currentEdges = newEdges
        }

        return loop.toList()
    }

    private fun branchOut(
        isAt: Pair<Int, Int>,
        comesFrom: MapDirection?
    ): List<Pair<TwoDimensionalIndexedValue<Pipe>, MapDirection>> {
        val pipe = this[isAt]
        val relativePositionsToCheck = pipe.endPoints.filter { it != comesFrom }.map { it.directionVector }

        return buildList {
            relativePositionsToCheck.forEach { relativePosition ->
                val absolutePosition = (relativePosition + isAt)
                val otherPipe = kotlin.runCatching { this@PipeGrid[absolutePosition] }.getOrElse { return@forEach }
                if (pipe.canConnectTo(otherPipe, relativePosition)) {
                    this.add(
                        TwoDimensionalIndexedValue(
                            absolutePosition,
                            otherPipe
                        ) to MapDirection.fromDirectionVector(-relativePosition)
                    )
                }
            }
        }
    }

    override operator fun get(coordinates: Pair<Int, Int>): Pipe = elements[coordinates.second][coordinates.first]
    
    private data class GroundOrVoidRegion(
        val elements: Set<TwoDimensionalIndexedValue<Pipe>>,
        val edgeCoordinates: Set<Pair<Int, Int>>
    ) {
        companion object {
            fun fromElements(elements: List<List<Pipe>>): List<GroundOrVoidRegion> {
                var remainingElements = elements.flatMapIndexed { yIndex, row ->
                    row.mapIndexed { xIndex, pipe ->
                        TwoDimensionalIndexedValue(xIndex to yIndex, pipe)
                    }
                }.filter { it.value == Pipe.GROUND || it.value == Pipe.VOID }.toSet()

                val regions = mutableListOf<GroundOrVoidRegion>()

                while (remainingElements.isNotEmpty()) {
                    val currentRegionCoordinates = mutableSetOf(remainingElements.first().twoDimIndex)
                    var currentEdgeCoordinates = currentRegionCoordinates.toSet()
                    var lastEdgeCoordinates = setOf<Pair<Int, Int>>()

                    while (currentEdgeCoordinates != lastEdgeCoordinates) {
                        lastEdgeCoordinates = currentEdgeCoordinates
                        currentEdgeCoordinates = expandEdge(elements, currentEdgeCoordinates, currentRegionCoordinates)
                        currentRegionCoordinates += currentEdgeCoordinates
                    }

                    val regionElements = currentRegionCoordinates.map {
                        TwoDimensionalIndexedValue(it, elements[it.second][it.first])
                    }.toSet()

                    remainingElements = remainingElements.filter { it.twoDimIndex !in currentRegionCoordinates }.toSet()

                    // Edges used for searching, might be incomplete.
                    val trueEdges = currentRegionCoordinates.filter { elementCoordinate ->
                        MapDirection.entries.map {
                            it.directionVector + elementCoordinate
                        }.any { it !in currentRegionCoordinates }
                    }.toSet()

                    regions.add(GroundOrVoidRegion(regionElements, trueEdges))
                }

                return regions
            }

            fun expandEdge(
                elements: List<List<Pipe>>,
                edgeCoordinates: Set<Pair<Int, Int>>,
                wasAt: Set<Pair<Int, Int>>
            ): Set<Pair<Int, Int>> = edgeCoordinates.flatMap { edgeCoordinate ->
                buildSet {
                    MapDirection.entries.forEach {
                        val lookAt = edgeCoordinate + it.directionVector
                        val pipe = runCatching { elements[lookAt.second][lookAt.first] }.getOrNull()

                        if (lookAt !in wasAt && (pipe == Pipe.VOID || pipe == Pipe.GROUND)) this.add(lookAt)
                    }
                }
            }.toSet()
        }
    }
}

fun main() {
    fun part1(input: List<String>): Int = (PipeGrid(input).loop.size / 2).let { it + it % 2 }
    fun part2(input: List<String>): Int = PipeGrid(input).groundElementsEnclosedByLoop()

    // Test input:
    val testInput = readInput("Day10_test")
    check(part1(testInput) == 8)
    val testInput2 = readInput("Day10_test2")
    check(part2(testInput2) == 8)

    val input = readInput("Day10")
    part1(input).println()
    part2(input).println()
}