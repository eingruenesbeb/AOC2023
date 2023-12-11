data class OASISSingleValueHistory(
    val historicalValues: List<Int>
) {
    fun extrapolateNext(): OASISSingleValueHistory {
        val helperLines: List<List<Int>> = getExtrapolationLines()

        var differenceToNext = 0
        val helperLinesWithExtrapolated = helperLines.reversed().mapIndexed { index, helperLine ->
            if (index != 0) {
                differenceToNext = nextExtrapolated(helperLine.last(), differenceToNext)
                helperLine.plus(differenceToNext)
            } else helperLine.plus(0)
        }

        return OASISSingleValueHistory(helperLinesWithExtrapolated.last())
    }

    fun extrapolatePrevious(): OASISSingleValueHistory {
        val helperLines = getExtrapolationLines()

        var differenceFromNext = 0
        val helperLinesWithExtrapolated = helperLines.reversed().mapIndexed { index, helperLine ->
            if (index != 0) {
                differenceFromNext = previousExtrapolated(helperLine.first(), differenceFromNext)
                listOf(differenceFromNext).plus(helperLine)
            } else listOf(0).plus(helperLine)
        }

        return OASISSingleValueHistory(helperLinesWithExtrapolated.last())
    }

    private fun getExtrapolationLines() = buildList<List<Int>> {
        if (this.isEmpty()) this.add(historicalValues)

        while (this.last().any { it != 0 }) this.add(nextHelperLine(this.last()))
    }

    private fun nextHelperLine(lastHelperLine: List<Int>) = buildList {
        val iterator = lastHelperLine.iterator()
        var last = iterator.next()
        while (iterator.hasNext()) {
            val current = iterator.next()
            this.add(current - last)
            last = current
        }
    }

    private fun nextExtrapolated(previous: Int, differenceToNext: Int) = previous + differenceToNext

    private fun previousExtrapolated(next: Int, differenceFromNext: Int) = next - differenceFromNext
}

fun parseInputToHistory(lines: List<String>): List<OASISSingleValueHistory> =
    lines.map { line ->
        OASISSingleValueHistory(line.split(' ').map { it.toInt() })
    }

fun main() {
    fun part1(input: List<String>): Int {
        val valueHistories = parseInputToHistory(input)

        return valueHistories.fold(0) { acc, oasisSingleValueHistory ->
            acc + oasisSingleValueHistory.extrapolateNext().historicalValues.last()
        }
    }

    fun part2(input: List<String>): Int {
        val valueHistories = parseInputToHistory(input)

        return valueHistories.fold(0) { acc, oasisSingleValueHistory ->
            acc + oasisSingleValueHistory.extrapolatePrevious().historicalValues.first()
        }
    }

    // Test input:
    val testInput = readInput("Day09_test")
    check(part1(testInput) == 114)
    check(part2(testInput) == 2)

    // Real input:
    val input = readInput("Day09")
    part1(input).println()
    part2(input).println()
}
