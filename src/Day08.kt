import java.util.function.Predicate

enum class Direction(val charRepresentation: Char) {
    LEFT('L'),
    RIGHT('R');

    companion object {
        fun fromCharRepresentation(char: Char) = entries.first { char == it.charRepresentation }
    }
}

data class Node(
    val id: String,
    val nodeLeftID: String,
    val nodeRightID: String
)

sealed class DesertMapException(message: String): Exception(message)
class MissingNodeException(nodeID: String) : DesertMapException("Missing node of ID \"$nodeID\".")
class NodeUnreachableException(
    startNodeID: String,
    destinationNodeID: String
) : DesertMapException("The destination node \"$destinationNodeID\" cannot be reached from node \"$startNodeID\"")
class RestlessSpiritException : DesertMapException("At least one node cannot be reached under the conditions imposed on the ghost.")

class DesertMap(private val nodes: Map<String, Node>, private val directions: List<Direction>) {
    operator fun get(nodeID: String) = nodes[nodeID] ?: throw MissingNodeException(nodeID)

    fun getStepsUntil(startNodeID: String, destinationNodeID: String): Int {
        val startNode = this[startNodeID]
        val destinationNode = this[destinationNodeID]

        val startNodeCycle = getCycleOf(startNode)
        val sequenceStartNodes = startNodeCycle.first.map { it.second }.toSet()
        val sequenceCycleNodes = startNodeCycle.second.map { it.second }.toSet()

        if(destinationNode !in sequenceStartNodes && destinationNode !in sequenceCycleNodes) throw NodeUnreachableException(startNodeID, destinationNodeID)

        return getTraversalSequence(startNode).indexOfFirst { it.second == destinationNode }
    }

    fun getSpookyStepsUntil(startNodeRequirement: Predicate<Node>, destinationNodeRequirement: Predicate<Node>): Int {
        val startNodes = nodes.values.filter { startNodeRequirement.test(it) }.toSet()
        val destinationNodes = nodes.values.filter { destinationNodeRequirement.test(it) }.toSet()

        val startNodeCyclesWithStarts = startNodes.associateWith { getCycleOf(it) }
        val solutionSet = startNodeCyclesWithStarts.values.fold(emptySet<Node>()) { acc, (sequenceStart, cycle) ->
            acc + sequenceStart.map { it.second } + cycle.map { it.second }
        }

        if (destinationNodes.any { it !in solutionSet }) throw RestlessSpiritException()

        // A solution can either be found right at the start or once every sequence has settled into its respective
        // cycle.
        // First try to find a solution at the beginning:
        val longestStart = startNodeCyclesWithStarts.values.maxOfOrNull { it.first.size } ?: 0

        val sequenceStarts = startNodes.map { getTraversalSequence(it).take(longestStart).toList() }
        // All sequence starts are of length `longestStart`
        sequenceStarts.indices.forEach { index ->
            if (sequenceStarts.all { it[index].second in destinationNodes }) return index
        }

        TODO(
            """
                Rotate the cycles such, that all are starting at cycle-index 0, when the last sequence enters its cycle.
                Next figure out, at which cycle-indices the node element of `destinationNodes` is.
                Then determine, if a solution exists, such that all cycles hit one of their respective solution cycle
                index.
                And lastly calculate the lowest possible solution. (see: Chinese reminder theorem)
            """.trimIndent()
        )
    }

    private fun getCycleOf(startNode: Node): Pair<Set<Pair<Int, Node>>, Set<Pair<Int, Node>>> {
        /*
         * The set of possible pairs in any traversal-sequence is finite,
         * because both the set direction-indices and the set of nodes are.
         *
         * Therefore, there exists a point at which the sequence becomes cyclic.
         * (I will provide no rigorous proof here, but as a bit of help:
         * Consider how the next elements in the sequence look, when a pair is repeated.)
         */
        val sequenceElements = mutableSetOf<Pair<Int, Node>>()
        var cycleStart: Pair<Int, Node>? = null

        val sequence = getTraversalSequence(startNode)

        for (element in sequence) {
            if (!sequenceElements.add(element)) {
                cycleStart = element
                break
            }
        }

        val sequenceStart = sequence.takeWhile { it != cycleStart }.toSet()
        val cycle = sequenceElements - sequenceStart

        return sequenceStart to cycle
    }

    private fun getTraversalSequence(startNode: Node, startDirectionIndex: Int = 0) =
        generateSequence(startDirectionIndex to startNode) { (directionIndex, currentNode) ->
            val nextDirectionIndex = (directionIndex + 1).takeIf { it < directions.size } ?: 0
            nextDirectionIndex to nextNode(directions[directionIndex], currentNode)
        }

    private fun nextNode(direction: Direction, currentNode: Node) =
        if (direction == Direction.LEFT) this[currentNode.nodeLeftID] else this[currentNode.nodeRightID]
}

fun main() {
    fun parseInput(lines: List<String>): DesertMap {
        val directions = lines.first().toCharArray().map { Direction.fromCharRepresentation(it) }
        val desertMap = DesertMap(
            lines.subList(2, lines.size).associate {
                val splitLine = it.split(" = ")
                val nodeID = splitLine.first()
                val splitPair = splitLine.last().slice(1..8).split(", ")

                nodeID to Node(nodeID, splitPair.first(), splitPair.last())
            },
            directions
        )

        return desertMap
    }

    fun part1(input: List<String>): Int {
        return parseInput(input).getStepsUntil("AAA", "ZZZ")
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
    val testInput2 = readInput("Day08_test2")
    check(part1(testInput) == 6)
    check(part2(testInput2) == 6)

    // Real input
    val input = readInput("Day08")
    part1(input).println()
    part2(input).println()

    // Timings
    timeTrials("Part 1") {
        part1(input)
    }

    timeTrials("Part 2", repetitions = 1000) {
        part2(input)
    }
}
