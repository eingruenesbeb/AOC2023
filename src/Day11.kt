import kotlin.math.abs

enum class AstronomicalObject(val charRepresentation: Char): TwoDimGrid.GridElement {
    GALAXY('#'),
    VOID('.');

    companion object {
        fun fromCharRepresentation(char: Char) = entries.first { it.charRepresentation == char }
    }
}

fun Pair<Int, Int>.taxicabDistance(toOther: Pair<Int, Int>) =
    abs(first - toOther.first) + abs(second - toOther.second)

class AstronometricData : SparseTwoDimGrid<AstronomicalObject> {
    override val elements: List<TwoDimensionalIndexedValue<AstronomicalObject>>

    constructor(elements: List<TwoDimensionalIndexedValue<AstronomicalObject>>) {
        this.elements = elements
    }

    constructor(lineRepresentation: List<String>, @Suppress("UNUSED_PARAMETER") plsIgnore: Unit = Unit) {
        elements = lineRepresentation.flatMapIndexed { yIndex, line ->
            line.toCharArray().mapIndexed { xIndex, char ->
                TwoDimensionalIndexedValue(xIndex to yIndex, AstronomicalObject.fromCharRepresentation(char)).takeUnless {
                    it.value == AstronomicalObject.VOID
                }
            }.filterNotNull()
        }
    }

    fun sumOfDistanceBetweenGalaxyNeighbours(): Long {
        val indexedGalaxies = elements.toMutableList()
        val doneGalaxies = mutableSetOf<TwoDimensionalIndexedValue<AstronomicalObject>>()
        return indexedGalaxies.associateWith { galaxyWithIndex ->
            doneGalaxies += galaxyWithIndex
            indexedGalaxies - doneGalaxies
        }.map { (measureTo, measureFromSet) ->
            measureFromSet.sumOf {
                it.twoDimIndex.taxicabDistance(measureTo.twoDimIndex).toLong()
            }
        }.sum()
    }

    fun expanded(expansionFactor: Int = 2): AstronometricData {
        var expandedElements = elements
        val emptyColumnsAt = (0..elements.maxOf { it.twoDimIndex.first }).filter { x ->
            elements.none { x == it.twoDimIndex.first }
        }
        val emptyRowsAt = (0..elements.maxOf { it.twoDimIndex.second }).filter { y ->
            elements.none { y == it.twoDimIndex.second }
        }

        expandedElements = expandedElements.map { element ->
            element.copy(element.twoDimIndex.copy(element.twoDimIndex.first + emptyColumnsAt.count { it < element.twoDimIndex.first } * (expansionFactor - 1)))
        }

        expandedElements = expandedElements.map { element ->
            element.copy(element.twoDimIndex.copy(second = element.twoDimIndex.second + emptyRowsAt.count { it < element.twoDimIndex.second } * (expansionFactor - 1)))
        }

        return AstronometricData(expandedElements)
    }

    override operator fun get(coordinates: Pair<Int, Int>): AstronomicalObject =
        runCatching { elements.first { it.twoDimIndex == coordinates }.value }.getOrNull() ?: AstronomicalObject.VOID
}

fun main() {
    fun part1(input: List<String>): Long = AstronometricData(input).expanded().sumOfDistanceBetweenGalaxyNeighbours()

    fun part2(input: List<String>, expansionFactor: Int): Long = AstronometricData(input).expanded(expansionFactor).sumOfDistanceBetweenGalaxyNeighbours()

    val testInput = readInput("Day11_test")
    check(part1(testInput) == 374L)
    check(part2(testInput, 100) == 8410L)

    val input = readInput("Day11")
    part1(input).println()
    part2(input, 1000000).println()
}
