@file:Suppress("MemberVisibilityCanBePrivate", "unused")

import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)

/**
 * A class for converting string or chars into digits.
 */
enum class ValidDigits {
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE;

    fun stringRepresentations() = when (this) {
        ZERO -> listOf("0", "zero")
        ONE -> listOf("1", "one")
        TWO -> listOf("2", "two")
        THREE -> listOf("3", "three")
        FOUR -> listOf("4", "four")
        FIVE -> listOf("5", "five")
        SIX -> listOf("6", "six")
        SEVEN -> listOf("7", "seven")
        EIGHT -> listOf("8", "eight")
        NINE -> listOf("9", "nine")
    }

    companion object {
        fun fromStringRepresentation(input: String) = when(input) {
            in ZERO.stringRepresentations() -> ZERO.ordinal
            in ONE.stringRepresentations() -> ONE.ordinal
            in TWO.stringRepresentations() -> TWO.ordinal
            in THREE.stringRepresentations() -> THREE.ordinal
            in FOUR.stringRepresentations() -> FOUR.ordinal
            in FIVE.stringRepresentations() -> FIVE.ordinal
            in SIX.stringRepresentations() -> SIX.ordinal
            in SEVEN.stringRepresentations() -> SEVEN.ordinal
            in EIGHT.stringRepresentations() -> EIGHT.ordinal
            in NINE.stringRepresentations() -> NINE.ordinal
            else -> null
        }

        fun fromChar(input: Char) = fromStringRepresentation(input.toString())

        fun isDigit(input: String) = fromStringRepresentation(input) != null

        fun isDigitChar(input: Char) = isDigit(input.toString())

        fun allRepresentations() = buildList {
            ValidDigits.entries.forEach {
                this.addAll(it.stringRepresentations())
            }
        }
    }
}
