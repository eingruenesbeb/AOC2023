import kotlin.time.DurationUnit
import kotlin.time.measureTime

fun <T> timeAndPrint(label: String = "", unit: DurationUnit = DurationUnit.MILLISECONDS, block: () -> T) {
    val measured = measureTime { block.invoke() }

    when (unit) {
        DurationUnit.NANOSECONDS -> println("\"$label\" took ${measured.inWholeNanoseconds}ns to execute.")
        DurationUnit.MICROSECONDS -> println("\"$label\" took ${measured.inWholeMicroseconds}µs to execute.")
        DurationUnit.MILLISECONDS -> println("\"$label\" took ${measured.inWholeMilliseconds}ms to execute.")
        DurationUnit.SECONDS -> println("\"$label\" took ${measured.inWholeSeconds}s to execute.")
        DurationUnit.MINUTES -> println("\"$label\" took ${measured.inWholeMinutes}m to execute.")
        DurationUnit.HOURS -> println("\"$label\" took ${measured.inWholeHours}h to execute.")
        DurationUnit.DAYS -> println("\"$label\" took ${measured.inWholeDays}d to execute.")
    }
}

fun <T> timeTrials(
    label: String = "",
    unit: DurationUnit = DurationUnit.MILLISECONDS,
    unitTotal: DurationUnit = DurationUnit.SECONDS,
    repetitions: Int = 1000,
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

    "Timings for $label:".println()

    listOf(min, max, mean, median, total).forEachIndexed { index, duration ->
        val convertTo = if (index == 4) unitTotal else unit

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
            4 -> "Total: $stringifiedDuration".println()
            else -> "Wibbly wobbly timey whimey stuff...".println()
        }
    }
}
