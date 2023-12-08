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

/**
 * Slices the current [UIntRange] based on the provided [other] range, dividing it into three parts:
 * 1. The portion of the current range that is entirely smaller than the other.
 * 2. The intersection with the other range.
 * 3. The portion of the current range that is entirely bigger than the other.
 *
 * Example:
 * ```
 * val range1 = 1u..5u
 * val range2 = 3u..7u
 * val result = range1.slice(range2)
 * // Result: (1u..2u, 3u..5u, 6u..7u)
 * ```
 *
 * @param other The [UIntRange] to slice the current range with.
 * @return A [Triple] representing the sliced portions as described above.
 */
fun LongRange.slice(other: LongRange): Triple<LongRange, LongRange, LongRange> {
    return when {
        this.last < other.first -> Triple(this, LongRange.EMPTY, LongRange.EMPTY)  // Entirely smaller than the other
        this.first < other.first && other.first <= this.last && this.last <= other.last -> {
            // Intersection with left overhang
            Triple(this.first..<other.first, other.first..this.last, LongRange.EMPTY)
        }
        this.first < other.first && other.last < this.last -> {
            // Intersection with left and right overhang
            Triple(this.first..<other.first, other, (other.last + 1)..this.last)
        }
        other.first <= this.first && this.last <= other.last -> Triple(LongRange.EMPTY, this, LongRange.EMPTY)  // The other range covers this entirely
        other.first < this.first && this.first <= other.last && other.last < this.last -> {
            // Intersection with right overhang
            Triple(LongRange.EMPTY, this.first..other.last, (other.last + 1)..this.last)
        }
        /*other.last < this.first*/ else -> Triple(LongRange.EMPTY, LongRange.EMPTY, this)  // Entirely bigger than the other
    }
}

fun LongRange.shift(shiftBy: Long) = (this.first + shiftBy)..(this.last + shiftBy)
