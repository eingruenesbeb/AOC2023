enum class Card(val initial: Char) {
    JOKER('★'),
    TWO('2'),
    THREE('3'),
    FOUR('4'),
    FIVE('5'),
    SIX('6'),
    SEVEN('7'),
    EIGHT('8'),
    NINE('9'),
    TEN('T'),
    JACK('J'),
    QUEEN('Q'),
    KING('K'),
    ACE('A');

    companion object {
        fun fromInitial(char: Char) = entries.first { it.initial == char }
    }
}

enum class HandType {
    HIGH_CARD,
    ONE_PAIR,
    TWO_PAIR,
    THREE_OF_A_KIND,
    FULL_HOUSE,
    FOUR_OF_A_KIND,
    FIVE_OF_A_KIND
}

data class Hand(
    val cards: List<Card>,
    val bid: Int
) : Comparable<Hand> {
    private val highestType: HandType = getHandTypeFromCards()
    override fun compareTo(other: Hand): Int = when {
        this.highestType > other.highestType -> 1
        this.highestType < other.highestType -> -1
        else -> breakTypeTie(other)
    }

    private fun breakTypeTie(other: Hand): Int {
        cards.zip(other.cards).forEach {
            when {
                it.first > it.second -> return 1
                it.first < it.second -> return -1
            }
        }

        return 0
    }

    private fun getHandTypeFromCards(): HandType {
        val cardsWithoutJokers = cards.filter { it != Card.JOKER }
        val jokerAmount = 5 - cardsWithoutJokers.size
        val distinctAmountsWithoutJokers = cardsWithoutJokers.distinct().map {
            distinctNotJoker -> cardsWithoutJokers.count { it == distinctNotJoker }
        }.sorted()

        return when (jokerAmount) {
            0 -> typeWithZeroJokers(distinctAmountsWithoutJokers)
            1 -> typeWithOneJoker(distinctAmountsWithoutJokers)
            2 -> typeWithTwoJokers(distinctAmountsWithoutJokers)
            3 -> typeWithThreeJokers(distinctAmountsWithoutJokers)
            else -> HandType.FIVE_OF_A_KIND  // Four or five jokers
        }
    }

    private fun typeWithZeroJokers(distinctAmountsNoJokers: List<Int>) = when (distinctAmountsNoJokers) {
        listOf(5) -> HandType.FIVE_OF_A_KIND
        listOf(1, 4) -> HandType.FOUR_OF_A_KIND
        listOf(2, 3) -> HandType.FULL_HOUSE
        listOf(1, 1, 3) -> HandType.THREE_OF_A_KIND
        listOf(1, 2, 2) -> HandType.TWO_PAIR
        listOf(1, 1, 1, 2) -> HandType.ONE_PAIR
        else -> HandType.HIGH_CARD
    }

    private fun typeWithOneJoker(distinctAmountsNoJokers: List<Int>) = when (distinctAmountsNoJokers) {
        listOf(4) -> HandType.FIVE_OF_A_KIND
        listOf(1, 3) -> HandType.FOUR_OF_A_KIND
        listOf(2, 2) -> HandType.FULL_HOUSE
        listOf(1, 1, 2) -> HandType.THREE_OF_A_KIND
        else -> HandType.ONE_PAIR
    }

    private fun typeWithTwoJokers(distinctAmountsNoJokers: List<Int>) = when (distinctAmountsNoJokers) {
        listOf(3) -> HandType.FIVE_OF_A_KIND
        listOf(1, 2) -> HandType.FOUR_OF_A_KIND
        else -> HandType.THREE_OF_A_KIND
    }

    private fun typeWithThreeJokers(distinctAmountsNoJokers: List<Int>) = when (distinctAmountsNoJokers) {
        listOf(2) -> HandType.FIVE_OF_A_KIND
        else -> HandType.FOUR_OF_A_KIND
    }
}

fun main() {
    val testInput = readInput("Day07_test")
    val input = readInput("Day07")

    fun parseInput(input: List<String>, jackIsJoker: Boolean = false) = input.map { line ->
        val splitLine = line.split(' ')
        val cards = splitLine[0].toCharArray().map {
            if (it == 'J' && jackIsJoker) Card.JOKER else Card.fromInitial(it)
        }
        val bid = splitLine[1].toInt()

        Hand(cards, bid)
    }

    fun part1(input: List<String>): Int =
        parseInput(input).sorted().foldIndexed(0) { index, acc, hand ->
            (index + 1) * hand.bid + acc
        }

    fun part2(input: List<String>): Int =
        parseInput(input, true).sorted().foldIndexed(0) { index, acc, hand ->
            (index + 1) * hand.bid + acc
        }

    // Test inputs
    check(part1(testInput) == 6440)
    check(part2(testInput) == 5905)

    // Real inputs
    part1(input).println()
    part2(input).println()

    // Timings
    timeTrials("Part 1") {
        part1(input)
    }

    timeTrials("Part 2") {
        part2(input)
    }
}
