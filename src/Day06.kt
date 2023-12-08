import kotlin.math.*
import kotlin.time.DurationUnit

// Technically not acceleration, but it's the rate at which the release-speed increases per millisecond, so I'll call it
// that.
const val ACCELERATION = 1  // In meters per millisecond²

/**
 * Parses the input table.
 *
 * @param lines The input lines
 * @return A list of [Int] [Pair]s. Each pair is a combination of \[maxTime, recordDistance]
 */
fun parseInput(lines: List<String>): List<Pair<Long, Long>> {
    // Line 1 contains the maximum allowed time.
    val times = lines.first().split(" +".toRegex()).drop(1).map { it.toLong() }
    // Line 2 contains the record distance. It has as many distances as there are times.
    val distances = lines.last().split(" +".toRegex()).drop(1).map { it.toLong() }

    return buildList {
        times.forEachIndexed { index, time -> this.add(time to distances[index]) }
    }
}

fun parseInputWithBadKerning(lines: List<String>): Pair<Long, Long> {
    val time = lines.first().split(" +".toRegex()).drop(1).joinToString("").toLong()
    val distance = lines.last().split(" +".toRegex()).drop(1).joinToString("").toLong()

    return time to distance
}

/**
 * The race is won, if the distance traveled (s) is greater than the record distance (s_r) or mathematically:
 * Δs = s - s_r > 0
 *
 * The distance traveled can be derived from the following (`a` is the rate at which the final speed increases while
 * holding the button, `T` is the total amount of time in the race and `t_h` is the time, the button is held):
 *
 * ⇔ `s(t) = v * t`   where: `v = a * t_h`  and  `t = T - t_h`
 *
 * ⇔ `s(t) = a * t_h * (T - t_h)`
 *
 * ⇔ `s(t_h) = -a * t_h^2 * a * T * t_h`
 *
 * ⇒ `Δs = -a * t_h^2 * a * T * t_h - s_r`
 *
 * Therefore, we have to solve the quadratic equation to get the lower and upper bound (in ℝ and exclusive):
 *
 * `0 < -a * t_h^2 * a * T * t_h - s_r`
 *
 *
 * Note: We only want to consider solutions in ℕ.
 *
 * @param maxTimeRecordDistancePair The value pair of `T` and `s_r`. The [ACCELERATION] is given.
 *
 * @return The solutions in ℕ or an empty [LongRange], if none are found.
 */
fun calculateWinningLongRange(maxTimeRecordDistancePair: Pair<Long, Long>): LongRange {
    val maxTime = maxTimeRecordDistancePair.first
    val recordDistance = maxTimeRecordDistancePair.second
    val discriminant = (ACCELERATION * maxTime).toDouble().pow(2.0) - 4 * ACCELERATION * recordDistance

    if (discriminant < 0) return LongRange.EMPTY  // A win would truly be quite imaginary in this case. The competition just has better boats.

    val solution1 = (-ACCELERATION * maxTime + sqrt(discriminant)) / -2 * ACCELERATION
    val solution2 = (-ACCELERATION * maxTime - sqrt(discriminant)) / -2 * ACCELERATION

    // We don't want to just at least match the record, but beat it. Meaning that we want every result in the range to
    // yield a result truly greater than 0.
    // If the result is exactly an integer, we need to manually add 1, because the ceil and floor functions won't push
    // it into the winning range.
    val lowerBound = ceil(min(solution1, solution2)).toLong().takeIf { it.toDouble() != min(solution1, solution2) } ?: (ceil(min(solution1, solution2)).toLong() + 1)
    val upperBound = floor(max(solution1, solution2)).toLong().takeIf { it.toDouble() != max(solution1, solution2) } ?: (floor(max(solution1, solution2)).toLong() - 1)

    return max(lowerBound, 0)..max(upperBound, -1)
}

fun main() {
    val testInput = readInput("Day06_test")
    val input = readInput("Day06")

    fun part1(input: List<String>): Long {
        val parsedInput = parseInput(input)

        return parsedInput.map { calculateWinningLongRange(it) }.fold(1) { acc, intRange ->
            acc * (intRange.last + 1 - intRange.first)
        }
    }

    fun part2(input: List<String>): Long {
        val winningRange = calculateWinningLongRange(parseInputWithBadKerning(input))
        return winningRange.last + 1 - winningRange.first
    }

    // Test input:
    check(part1(testInput) == 288L)
    check(part2(testInput) == 71503L)

    // Real input:
    timeAndPrint("Part 1", DurationUnit.MICROSECONDS) { part1(input).println() }
    timeAndPrint("Part 2", DurationUnit.MICROSECONDS) { part2(input).println() }

    timeTrials("Part 1", DurationUnit.MICROSECONDS, DurationUnit.MILLISECONDS, 10000) {
        part1(input)
    }

    timeTrials("Part 2", DurationUnit.MICROSECONDS, DurationUnit.MILLISECONDS, 10000) {
        part1(input)
    }
}
