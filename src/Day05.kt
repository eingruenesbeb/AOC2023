import java.util.*
import kotlin.time.DurationUnit

/**
 * Indicates an id-type.
 */
sealed interface IDType

data object SeedID : IDType
data object SoilID : IDType
data object FertilizerID : IDType
data object WaterID : IDType
data object LightID : IDType
data object TemperatureID : IDType
data object HumidityID : IDType
data object LocationID : IDType


data class IDSet<@Suppress("unused") T: IDType>(val set: SortedSet<Long>)

data class IDRange<@Suppress("unused") T: IDType>(val range: LongRange)

/**
 * A mapping of sets of an [IDType] [I] to a corresponding set of another [IDType] [O].
 *
 * Mappings can also be concatenated if the input type is the same as the output type.
 *
 * A mapping doesn't need to be an injection, but is always a surjection.
 *
 * @param I The [IDType] of the input-set.
 * @param O The [IDType] of the output-set.
 *
 * @property shiftRanges Range-Int pairs, where any id in the input-set, contained in the range, will get shifted by
 * the amount in the second value of the pair.
 *
 * @see imageSet
 */
class Mapping<I: IDType, O: IDType>(
    private val shiftRanges: List<Pair<LongRange, Long>>
) {
    /**
     * Generates the image [IDSet] of type [O] for an input [IDSet] of type [I].
     *
     * The image has a cardinality lower or equal to its primal.
     *
     * A given amount will shift elements from the input-set that are in the special ranges.
     * Any other element in the input-set will get mapped using its identity.
     *
     * **THE INPUT SET SHOULDN'T BE TOO BIG!**
     *
     * @return The image
     */
    fun imageSet(inputIDSet: IDSet<I>): IDSet<O>  {
        val identityMapped = inputIDSet.set.filter { id -> shiftRanges.all { id !in it.first } }.toSet()
        val shiftedIDs = (inputIDSet.set - identityMapped).map {  shiftedID ->
            val toShiftBy = shiftRanges.first { shiftedID in it.first }.second
            (shiftedID.toLong() + toShiftBy)
        }

        return IDSet(identityMapped.union(shiftedIDs).toSortedSet())
    }

    private fun imageRange(inputRange: IDRange<I>): List<IDRange<O>> {
        /*
         * Each `shiftRange` will "punch a hole" into the inputRange. Like this:
         *
         * Input: `[a, a+3]`
         * shift range: `[a+1, a+2] → [a-1, a]`
         * output: `[a-1,a] and [a+3, a+3]`
         *
         */
        val shiftedHoles = mutableListOf<IDRange<O>>()
        var unshiftedSlices = mutableListOf(IDRange<O>(inputRange.range))
        var newUnshiftedSlices = mutableListOf<IDRange<O>>()

        for (shiftRange in shiftRanges) {
            unshiftedSlices.forEach { unshiftedSlice ->
                val slicedInput = unshiftedSlice.range.slice(shiftRange.first)

                if (!slicedInput.second.isEmpty()) {
                    shiftedHoles.add(IDRange(slicedInput.second.shift(shiftRange.second)))
                }

                newUnshiftedSlices.addAll(
                    listOf(slicedInput.first, slicedInput.third)
                        .filter { !it.isEmpty() }
                        .map { IDRange(it) }
                )
            }
            unshiftedSlices = newUnshiftedSlices
            newUnshiftedSlices = mutableListOf()
        }

        return shiftedHoles.plus(unshiftedSlices)  // Some ranges may overlap, but that's ok.
    }

    fun imageMultiRange(inputRanges: List<IDRange<I>>): List<IDRange<O>> =
        inputRanges.fold(listOf()) { acc, idRange ->
            acc.plus(imageRange(idRange))
        }
}

fun extractSeedSet(seedLine: String): IDSet<SeedID> {
    val seedIDs = seedLine.split(": ").last().split(' ').map {
        it.toLong()
    }.toSortedSet()

    return IDSet(seedIDs)
}

fun extractSeedRanges(seedLine: String): List<IDRange<SeedID>> =
    seedLine.split(": ").last().split(' ').chunked(2).fold(listOf()) { acc, numPair ->
        val rangeStart = numPair[0].toLong()
        val rangeSize = numPair[1].toLong()

        acc.plusElement(IDRange(rangeStart..<(rangeStart + rangeSize)))
    }

/**
 * Extracts a mapping based on the guaranteed input.
 * Advances the [lineIterator] up to the next empty line.
 *
 * @param I The expected input type for the mapping.
 * @param O The expected output type for the mapping.
 * @param lineIterator The current line iterator.
 *
 * @return The [Mapping] represented by the lines up to the next empty line.
 */
fun <I: IDType, O: IDType> extractMapping(lineIterator: ListIterator<String>): Mapping<I, O> {
    val specialRanges = buildList {
        while (lineIterator.hasNext()) {
            val currentLine = lineIterator.next()
            if (currentLine == "") {
                lineIterator.previous()
                break
            }
            // Numbers are ordered: [destination range start] [source range start] [range size]
            val numbers = currentLine.split(" ").map { it.toLong() }  // size=3
            val range = numbers[1]..<(numbers[1] + numbers[2])
            val translation = numbers[0] - numbers[1]

            this.add(range to translation)
        }
    }

    return Mapping(specialRanges)
}

fun main() {
    val testInput = readInput("Day05_test")

    fun part1(input: List<String>): Long {
        val linesIterator = input.listIterator()
        var mappedSet: IDSet<*> = IDSet<SeedID>(sortedSetOf())

        var currentBlock = 0
        while (linesIterator.hasNext()) {
            val line = linesIterator.next()
            if (line == "") {
                currentBlock += 1
                continue
            } else {
                @Suppress("UNCHECKED_CAST")
                mappedSet = when (currentBlock) {
                    0 -> extractSeedSet(line)
                    1 -> extractMapping<SeedID, SoilID>(linesIterator).imageSet(mappedSet as IDSet<SeedID>)
                    2 -> extractMapping<SoilID, FertilizerID>(linesIterator).imageSet(mappedSet as IDSet<SoilID>)
                    3 -> extractMapping<FertilizerID, WaterID>(linesIterator).imageSet(mappedSet as IDSet<FertilizerID>)
                    4 -> extractMapping<WaterID, LightID>(linesIterator).imageSet(mappedSet as IDSet<WaterID>)
                    5 -> extractMapping<LightID, TemperatureID>(linesIterator).imageSet(mappedSet as IDSet<LightID>)
                    6 -> extractMapping<TemperatureID, HumidityID>(linesIterator).imageSet(mappedSet as IDSet<TemperatureID>)
                    7 -> extractMapping<HumidityID, LocationID>(linesIterator).imageSet(mappedSet as IDSet<HumidityID>)
                    else -> break
                }
            }
        }

        return mappedSet.set.min()
    }

    fun part2(input: List<String>): Long {
        val linesIterator = input.listIterator()
        var mappedRanges: List<IDRange<*>> = extractSeedRanges(linesIterator.next()).map { IDRange<SeedID>(it.range) }

        var currentBlock = 0
        while (linesIterator.hasNext()) {
            val line = linesIterator.next()
            if (line == "") {
                currentBlock += 1
                continue
            } else {
                @Suppress("UNCHECKED_CAST")
                mappedRanges = when (currentBlock) {
                    1 -> extractMapping<SeedID, SoilID>(linesIterator).imageMultiRange(mappedRanges as List<IDRange<SeedID>>)
                    2 -> extractMapping<SoilID, FertilizerID>(linesIterator).imageMultiRange(mappedRanges as List<IDRange<SoilID>>)
                    3 -> extractMapping<FertilizerID, WaterID>(linesIterator).imageMultiRange(mappedRanges as List<IDRange<FertilizerID>>)
                    4 -> extractMapping<WaterID, LightID>(linesIterator).imageMultiRange(mappedRanges as List<IDRange<WaterID>>)
                    5 -> extractMapping<LightID, TemperatureID>(linesIterator).imageMultiRange(mappedRanges as List<IDRange<LightID>>)
                    6 -> extractMapping<TemperatureID, HumidityID>(linesIterator).imageMultiRange(mappedRanges as List<IDRange<TemperatureID>>)
                    7 -> extractMapping<HumidityID, LocationID>(linesIterator).imageMultiRange(mappedRanges as List<IDRange<HumidityID>>)
                    else -> break
                }
            }
        }

        return mappedRanges.fold(Long.MAX_VALUE) { acc, idRange -> (idRange.range.first.takeIf { it < acc } ?: acc) }
    }

    // test if implementation meets criteria from the description, like:
    check(part1(testInput) == 35L)
    check(part2(testInput) == 46L)

    val input = readInput("Day05")
    part1(input).println()  // 389056265
    part2(input).println()  // 137516820

    // Timings
    timeTrials("Part 1", unitTotal = DurationUnit.MILLISECONDS) {
        part1(input)
    }

    timeTrials("Part 2") {
        part2(input)
    }
}
