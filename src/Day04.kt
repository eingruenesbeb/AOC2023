import kotlin.math.pow

data class ScratchCard(
    val winningNumbers: List<Int>,
    val playingNumbers: List<Int>
) {
    val score = 2.0.pow(
        (playingNumbers.filter { it in winningNumbers }.size - 1).toDouble()
    ).takeIf { it >= 1 }?.toInt() ?: 0
    val winningPlayingNumbers = playingNumbers.intersect(winningNumbers.toSet())

    companion object {
        fun fromStringRepresentation(string: String): ScratchCard {
            /*
             * Input string will always have the following pattern:
             * Card [cardNumber]: [set of 5 winning numbers] | [set of 8 playing numbers]
             *
             * Each number takes up exactly 2 characters (max=99), with single-digit numbers having an additional ' '
             * character in front. (Implies constant string length and rectangular collection of stringified
             * scratchcards)
             */
            val splitString = string.substringAfter(':').split('|')
            val winningNumbers = splitString.first().split(' ').filter { it != "" }.map { it.toInt() }
            val playingNumbers = splitString.last().split(' ').filter { it != "" }.map { it.toInt() }

            return ScratchCard(winningNumbers, playingNumbers)
        }
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        return input.fold(0) { acc, line ->
            acc + ScratchCard.fromStringRepresentation(line).score
        }
    }

    fun part2(input: List<String>): Int {
        val scratchCardsWithAmounts = input.map {
            // Index is the scratch-card number - 1
            ScratchCard.fromStringRepresentation(it) to 1
        }.toMutableList()

        var finalAmount = 0

        scratchCardsWithAmounts.indices.forEach { cardIndex ->
            val winningPlayingNumbersAmount = scratchCardsWithAmounts[cardIndex].first.winningPlayingNumbers.size
            val cardsToCopyIndices = (cardIndex + 1)..(cardIndex + winningPlayingNumbersAmount)
            val newCopiesAmount = scratchCardsWithAmounts[cardIndex].second

            // The amount of this particular card will not be updated again. It's contribution to the total can be added
            // now.
            finalAmount += scratchCardsWithAmounts[cardIndex].second

            // Update the amounts of the following cards.
            cardsToCopyIndices.forEach {
                scratchCardsWithAmounts[it] =
                    scratchCardsWithAmounts[it].first to scratchCardsWithAmounts[it].second + newCopiesAmount
            }
        }

        return finalAmount
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 30)


    val input = readInput("Day04")
    part1(input).println()  // 15268
    part2(input).println()  // 6283755
}
