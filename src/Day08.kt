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

fun gcdExtended(a: Int, b: Int): Triple<Int, Int, Int> {
    var secondLastR = a
    var secondLastS = 1
    var secondLastT = 0

    var lastR = b
    var lastS = 0
    var lastT = 1

    while (lastR != 0) {
        val q = secondLastR / lastR

        val currentR = secondLastR % lastR
        val currentS = secondLastS - q * lastS
        val currentT = secondLastT - q * lastT

        secondLastR = lastR
        secondLastS = lastS
        secondLastT = lastT

        lastR = currentR
        lastS = currentS
        lastT = currentT
    }

    return Triple(secondLastR, secondLastS, secondLastT)
}

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

        val startNodeSequenceStartsAndCycles = startNodes.map { startNode ->
            val startAndCycle = getCycleOf(startNode)
            startAndCycle.first.map { it.second }.toSet() to startAndCycle.second.map { it.second }.toSet()
        }

        val nodeSequenceStarts = startNodeSequenceStartsAndCycles.map { it.first }

        // All sequences should contain at least 1 destination.
        if (!allSequencesContainDestination(startNodeSequenceStartsAndCycles, destinationNodeRequirement)) throw RestlessSpiritException()

        // A solution can either be found right at the start or once every sequence has settled into its respective
        // cycle.
        // First try to find a solution at the beginning:
        val longestStart = nodeSequenceStarts.maxOfOrNull { it.size } ?: 0

        var steps: Int
        with(allReachedDestinationIn(startNodes, longestStart, destinationNodeRequirement)) {
            if (this != null) return this
            else steps = longestStart
        }

        // Rotate cycles such that every sequence has entered its cyclic phase.
        val rotatedCycles = startNodeSequenceStartsAndCycles.map { (sequenceStart, sequenceCycle) ->
            // All cycles can go through a non-zero number of elements, before entering a cycle.
            // During that time, both it and the sequence with the longest start are not yet in their cycle.
            // Therefore, the amount to rotate has to be properly adjusted.
            val rotateBy = -(longestStart - sequenceStart.size)
            rotateCycle(sequenceCycle, rotateBy)
        }

        val cycleModuli = rotatedCycles.map { it.size }

        val solutionIndices = buildList {
            rotatedCycles.forEach { cycle ->
                this.add(
                    buildSet {
                        cycle.forEachIndexed { index, node ->
                            if (destinationNodeRequirement.test(node)) this.add(index)
                        }
                    }
                )
            }
        }

        // Test-zone:
        gcdExtended(cycleModuli[0], cycleModuli[1])

        // For testing: consider only fist possible solution-set.
        TODO()
    }

    private fun allReachedDestinationIn(
        startNodes: Set<Node>,
        steps: Int,
        destinationNodeRequirement: Predicate<Node>
    ): Int? {
        val sequences = startNodes.map { node ->
            getTraversalSequence(node).take(steps).toSet().map { it.second }
        }

        sequences[0].indices.forEach { sequenceIndex ->
            if (sequences.all { destinationNodeRequirement.test(it[sequenceIndex]) }) return sequenceIndex
        }

        return null
    }

    private fun allSequencesContainDestination(
        startNodeSequenceStartsAndCycles: List<Pair<Set<Node>, Set<Node>>>,
        destinationNodeRequirement: Predicate<Node>
    ): Boolean =
        startNodeSequenceStartsAndCycles
            .map { it.first.union(it.second) }
            .all { reachedNodes ->
                reachedNodes.any { destinationNodeRequirement.test(it) }
            }

    private fun rotateCycle(cycle: Set<Node>, rotateBy: Int): Set<Node> {
        val normalizedRotateBy = rotateBy % cycle.size
        val cycleBy = if (normalizedRotateBy < 0) normalizedRotateBy + cycle.size else normalizedRotateBy

        if (cycleBy == 0) return cycle

        val splitIndex = cycle.size - cycleBy

        val parts = cycle.foldIndexed(listOf<Set<Node>>(setOf(), setOf())) { index, acc, node ->
            if (index >= splitIndex) listOf(acc[0].plus(node), acc[1]) else listOf(acc[0], acc[1].plus(node))
        }

        return parts[0].plus(parts[1])
    }

    private fun getCycleOf(startNode: Node): Pair<Set<Pair<Int, Node>>, Set<Pair<Int, Node>>> {
        // TODO: This function doesn't get the actual full cycle.
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
        return parseInput(input).getSpookyStepsUntil({ it.id.endsWith("A") }, { it.id.endsWith("Z") })
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
