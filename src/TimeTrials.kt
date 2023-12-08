import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit
import kotlin.time.measureTime

/**
 * Executes the provided code block multiple times, measures the execution time for each repetition,
 * and prints various timing statistics, including the minimum,
 * maximum, mean, median, standard deviation, and total time.
 *
 * @param label A string used for labeling the benchmarking output. Default: `""`
 * @param unit The time unit in which individual timings (minimum, maximum, mean, median) will be displayed.
 * Default: [DurationUnit.MICROSECONDS]
 * @param unitTotal The time unit in which the total time will be displayed. Default: [DurationUnit.SECONDS]
 * @param repetitions The number of times the provided code block will be executed for benchmarking. Default: 10000
 * @param block The code block or function that is being benchmarked.
 */
fun <T> timeTrials(
    label: String = "",
    unit: DurationUnit = DurationUnit.MICROSECONDS,
    unitTotal: DurationUnit = DurationUnit.SECONDS,
    repetitions: Int = 10000,
    block: () -> T
) {
    "Gathering timings for $label with $repetitions repetitions...".println()

    val timings = (1..repetitions).map {
        measureTime { block.invoke() }
    }.sorted()

    val min = timings.first()
    val max = timings.last()
    val total = timings.reduce { acc, duration -> acc.plus(duration) }
    val mean = total.div(timings.size)
    val median = timings.subList(0, (timings.size - 1) / 2).last()

    val variance = timings.map { it.inWholeNanoseconds.toDouble() }
        .let { durations ->
            val average = durations.average()
            durations.map { (it - average) * (it - average) }.average()
        }

    val standardDeviation = kotlin.math.sqrt(variance).toLong().nanoseconds


    "Timings for $label:".println()

    listOf(min, max, mean, median, standardDeviation, total).forEachIndexed { index, duration ->
        val convertTo = when (index) {
            5 -> unitTotal
            else -> unit
        }

        val stringifiedDuration = when (convertTo) {
            DurationUnit.NANOSECONDS -> "${duration.inWholeNanoseconds}ns"
            DurationUnit.MICROSECONDS -> "${duration.inWholeMicroseconds}µs"
            DurationUnit.MILLISECONDS -> "${duration.inWholeMilliseconds}ms"
            DurationUnit.SECONDS -> "${duration.inWholeSeconds}s"
            DurationUnit.MINUTES -> "${duration.inWholeMinutes}m"
            DurationUnit.HOURS -> "${duration.inWholeHours}h"
            DurationUnit.DAYS -> "${duration.inWholeDays}d"
        }

        when (index) {
            0 -> "Minimum: $stringifiedDuration".println()
            1 -> "Maximum: $stringifiedDuration".println()
            2 -> "Mean: $stringifiedDuration".println()
            3 -> "Median: $stringifiedDuration".println()
            4 -> "Standard deviation: $stringifiedDuration".println()
            5 -> "Total: $stringifiedDuration".println()
            else -> "Wibbly wobbly timey whimey stuff...".println()
        }
    }
}
