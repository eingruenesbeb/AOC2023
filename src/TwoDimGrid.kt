interface TwoDimGrid<T: TwoDimGrid.GridElement> {
    val width: Int
    val height: Int

    // The indices of the outer list are the y-index, and the indices of the inner list are the x-indices.
    val elements: List<List<T>>
    operator fun get(coordinates: Pair<Int, Int>): T

    interface GridElement

}