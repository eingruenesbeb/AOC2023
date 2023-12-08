import kotlin.time.DurationUnit

fun main() {
    fun part1(input: List<String>): Int {
        val charDigits = '0'..'9'
        return input.fold(0) { current, line ->
            val toAdd = buildString(2) {
                val firstDigit =  runCatching {
                    ValidDigits.fromChar(line.toCharArray().first { it in charDigits })
                }.getOrNull() ?: 0
                val lastDigit = runCatching {
                    ValidDigits.fromChar(line.toCharArray().last { it in charDigits })
                }.getOrNull() ?: 0

                this.append("$firstDigit$lastDigit")
            }

            current + toAdd.toInt()
        }
    }

    fun part2(input: List<String>): Int {
        val validDigitRepresentations = ValidDigits.allRepresentations()
        return input.fold(0) { current, line ->
            val toAdd = buildString(2) {
                val firstDigit = line.findAnyOf(validDigitRepresentations)?.second?.let {
                    ValidDigits.fromStringRepresentation(it)
                } ?: 0
                val lastDigit = line.findLastAnyOf(validDigitRepresentations)?.second?.let {
                    ValidDigits.fromStringRepresentation(it)
                } ?: 0

                this.append("$firstDigit$lastDigit")
            }

            current + toAdd.toInt()
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput1 = readInput("Day01_part1_test")
    val testInput2 = readInput("Day01_part2_test")
    check(part1(testInput1) == 142)
    check(part2(testInput2) == 281)

    val input = readInput("Day01")
    timeAndPrint("Part 1") { part1(input).println() }
    timeAndPrint("Part 2") { part2(input).println() }

    timeTrials("Part 1", DurationUnit.MICROSECONDS, DurationUnit.MILLISECONDS) { part1(input) }
}
