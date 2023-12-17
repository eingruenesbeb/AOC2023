/**
 * Represents a [TwoDimGrid], but it contains `null` elements, which aren't contained in the list of elements.
 *
 * The grid class used as grid-elements should always have a representative for an empty of zero element.
 * (As a kind of "typed-null")
 */
interface SparseTwoDimGrid<T: TwoDimGrid.GridElement> {
    val elements: List<TwoDimensionalIndexedValue<T>>

    operator fun get(coordinates: Pair<Int, Int>): T
}