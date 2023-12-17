/**
 * Represents a [TwoDimGrid], but it contains `null` elements, which aren't contained in the list of elements.
 */
interface SparseTwoDimGrid<T: TwoDimGrid.GridElement> {
    val elements: List<TwoDimensionalIndexedValue<T>>

    operator fun get(coordinates: Pair<Int, Int>): T
}