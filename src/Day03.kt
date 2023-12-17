sealed interface MachineGridElement: TwoDimGrid.GridElement

/**
 * Represents an empty grid-element.
 */
data object EmptyGridElement : MachineGridElement

/**
 * Represents any grid-element, that is anything but an [EmptyGridElement] or [NumberGridElement].
 */
open class SymbolGridElement : MachineGridElement

/**
 * Represents a gear grid-element.
 */
data class GearGridElement(val x: Int, val y: Int) : SymbolGridElement()

/**
 * Represents an integer grid-element.
 *
 * @property value The value of the integer.
 */
data class NumberGridElement(
    val value: Int
) : MachineGridElement {
    companion object {
        /**
         * Constructs a new [NumberGridElement]. The resulting value is based on what number (in base 10) is represented
         * by the first digits in the given [CharArray].
         *
         * @param rightCharsInclusive The array of the characters including all digits at the beginning. (Left to right)
         *
         * @return The resulting [NumberGridElement]. Its value is 0, when no suitable digits could be found at the
         * beginning of [rightCharsInclusive].
         */
        fun constructRight(rightCharsInclusive: CharArray): NumberGridElement {
            // Will be reversed at first.
            var currentValue = ""
            for (character in rightCharsInclusive) {
                if (ValidDigits.isDigitChar(character)) currentValue += character else break
            }

            return if (currentValue == "") NumberGridElement(0) else NumberGridElement(currentValue.toInt())
        }
    }
}

data class HorizontalNumberRegion(
    val xRange: IntRange,
    val y: Int,
    val localValue: Int
)

/**
 * Represents a two-dimensional grid of finite size containing [TwoDimGrid.GridElement]s at each point.
 *
 * @constructor Can be constructed from a string representation.
 * This string has to be rectangular (the same number of lines as characters per line).
 *
 * A peculiarity is that horizontally adjacent digits in the input string will result in every
 * coordinate of these digits containing the [NumberGridElement] represented by them read from left to right.
 */
class MachineGrid(stringRows: List<String>) : TwoDimGrid<MachineGridElement> {
    override val width: Int
    override val height: Int
    // The indices of the outer list are the y-index, and the indices of the inner list are the x-indices.
    override val elements: List<List<MachineGridElement>>
    // Due to the number-regions being horizontal, the y coordinate can be stored as a single int.
    private val numberRegions: List<HorizontalNumberRegion>
    private val gearElements: List<GearGridElement>

    init {
        val numberRegionsToConstruct = mutableListOf<HorizontalNumberRegion>()
        val gearElementsToConstruct = mutableListOf<GearGridElement>()

        width = stringRows.first().length
        height = stringRows.size

        /*
         * The data will be stored with flipped axis internally. The get operator will invert this again to match the
         * input.
         */
        elements = stringRows.mapIndexed yLoop@ { yIndex, line ->
            if (line.length != width) throw IllegalArgumentException("Input isn't rectangular.")

            var skipUntilIndex = 0
            var lastNumberGridElement = NumberGridElement(0)  // Placeholder value
            line.toCharArray().mapIndexed xLoop@ { xIndex, char ->
                if (xIndex < skipUntilIndex) return@xLoop lastNumberGridElement
                when (char) {
                    '.' -> EmptyGridElement
                    '*' -> GearGridElement(xIndex, yIndex).also {
                        gearElementsToConstruct.add(it)
                    }
                    in '0'..'9' -> {
                        lastNumberGridElement = NumberGridElement.constructRight(line.toCharArray(startIndex = xIndex))
                        skipUntilIndex = xIndex + lastNumberGridElement.value.toString().length

                        numberRegionsToConstruct.add(
                            HorizontalNumberRegion(
                                xIndex..<skipUntilIndex,
                                yIndex,
                                lastNumberGridElement.value
                            )
                        )

                        lastNumberGridElement
                    }
                    else -> SymbolGridElement()
                }
            }
        }

        numberRegions = numberRegionsToConstruct.toList()
        gearElements = gearElementsToConstruct.toList()
    }

    override operator fun get(coordinates: Pair<Int, Int>) = elements[coordinates.second][coordinates.first]

    fun getNumberRegionsNextToSymbol() = numberRegions.filter {
        val xRangeToSearch = (it.xRange.first - 1).coerceIn(0, width - 1)..(it.xRange.last + 1).coerceIn(0, width - 1)
        val yRangeToSearch = (it.y - 1).coerceIn(0, height - 1)..(it.y + 1).coerceIn(0, height - 1)

        return@filter yRangeToSearch.any { y -> xRangeToSearch.any { x -> this[x to y] is SymbolGridElement } }
    }

    fun gearRatios() = gearElements.map { gear ->
        val xRange = (gear.x - 1).coerceIn(0, width - 1)..(gear.x + 1).coerceIn(0, width - 1)
        val yRange = (gear.y - 1).coerceIn(0, height - 1)..(gear.y + 1).coerceIn(0, height - 1)

        val adjacentNumberRegions = numberRegions.filter { it.xRange.any { x -> x in xRange } && it.y in yRange }

        if (adjacentNumberRegions.size < 2) return@map 0

        adjacentNumberRegions.fold(1) { acc, region -> acc * region.localValue }
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        return MachineGrid(input).getNumberRegionsNextToSymbol().fold(0) { acc, region -> acc + region.localValue }
    }

    fun part2(input: List<String>): Int {
        return MachineGrid(input).gearRatios().fold(0) { acc, ratio -> acc + ratio }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 4361)
    check(part2(testInput) == 467835)

    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()

    // Timings
    timeTrials("Part 1") {
        part1(input)
    }

    timeTrials("Part 2", repetitions = 1000) {
        part2(input)
    }
}
