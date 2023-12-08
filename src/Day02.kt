import kotlin.time.DurationUnit

/**
 * Represents a color that a cube could be.
 */
enum class Color {
    RED,
    GREEN,
    BLUE;

    companion object {
        fun fromString(input: String) = when (input) {
            "red" -> RED
            "green" -> GREEN
            "blue" -> BLUE
            else -> throw IllegalArgumentException("Input doesn't match any color.")
        }
    }
}

data class GameSet(
    val amountRed: Int,
    val amountGreen: Int,
    val amountBlue: Int
) {
    companion object {
        fun fromStringRepresentation(string: String): GameSet {
            /*
             * Strings can only be of the following form:
             * `[amountRed], [amountBlue], [amountGreen]`
             * These elements can be in any order and are optional. (Although at least 1 is present.)
             * They have the following structure: `[amount] [red|green|blue]`
             */
            val splitString = string.split(", ")
            val colors = splitString.associate {
                val splitElement = it.split(" ")
                Color.fromString(splitElement.last()) to splitElement.first().toInt()
            }

            return GameSet(
                colors[Color.RED] ?: 0,
                colors[Color.GREEN] ?:0,
                colors[Color.BLUE] ?: 0
            )
        }
    }
}

/**
 * Represents a "game". For further clarifications, please refer to [this site](https://adventofcode.com/2023/day/2)
 */
data class Game(
    val id: Int,
    val sets: List<GameSet>,
    val redRequired: Int,
    val greenRequired: Int,
    val blueRequired: Int
) {
    companion object {
        /**
         *  Constructs a game-based input, Strings will (given the limited selection of possible inputs) always have the
         *  following pattern:
         * `Game \[ID]: \[set1]; \[set3]; [...]`
         */
        fun fromStringRepresentation(string: String): Game {
            val splitString = string.split(": ")
            val gameID = splitString.first().split(" ").last().toInt()

            var maxRed = 0
            var maxGreen = 0
            var maxBlue = 0

            val sets = splitString.last().split("; ").map { setRepresentation ->
                GameSet.fromStringRepresentation(setRepresentation).also {
                    if (it.amountRed > maxRed) maxRed = it.amountRed
                    if (it.amountGreen > maxGreen) maxGreen = it.amountGreen
                    if (it.amountBlue > maxBlue) maxBlue = it.amountBlue
                }
            }

            return Game(gameID, sets, maxRed, maxGreen, maxBlue)
        }
    }

    /**
     * Calculates the "power" of the game. It is defined as the number of required color cubes multiplied together.
     *
     * @return [redRequired] * [greenRequired] * [blueRequired]
     */
    fun power() = redRequired * greenRequired * blueRequired

    /**
     * A game is considered possible if the number of required cubes is less or equal to the given amounts per color.
     *
     * @param maxRedAllowed The maximum amount of allowed red cubes.
     * @param maxGreenAllowed The maximum amount of allowed green cubes.
     * @param maxBlueAllowed The maximum amount of allowed blue cubes.
     *
     * @return Whether the game is possible.
     */
    fun isPossible(maxRedAllowed: Int, maxGreenAllowed: Int, maxBlueAllowed: Int): Boolean =
        redRequired <= maxRedAllowed && greenRequired <= maxGreenAllowed && blueRequired <= maxBlueAllowed
}

fun extractGamesFromInput(input: List<String>) = input.map {
    Game.fromStringRepresentation(it)
}


fun main() {
    fun part1(gameList: List<Game>): Int {
        val redInBag = 12
        val greenInBag = 13
        val blueInBag = 14
        return gameList.fold(0) { currentSum, game ->
            val isPossible = game.isPossible(redInBag, greenInBag, blueInBag)
            val toAdd = game.id.takeIf { isPossible } ?: 0
            currentSum + toAdd
        }
    }

    fun part2(gameList: List<Game>): Int {
        return gameList.fold(0) { currentSum, game ->
            currentSum + game.power()
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    val testGames = extractGamesFromInput(testInput)
    check(part1(testGames) == 8)
    check(part2(testGames) == 2286)

    val input = readInput("Day02")
    val games = extractGamesFromInput(input)

    timeAndPrint("Part 1", DurationUnit.MICROSECONDS) { part1(games).println() }
    timeAndPrint("Part 2", DurationUnit.MICROSECONDS) { part2(games).println() }
}
