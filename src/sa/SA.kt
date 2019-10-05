package sa

import dataClasses.Elevation
import dataClasses.Location
import kotlinx.coroutines.*
import service.DISCRETIZING_DISTANCE_AVERAGE
import java.util.*
import kotlin.math.exp
import kotlin.math.roundToInt

const val RESTARTS_PER_DEFERRED = 100
fun numberOfDeferredInvocations(size: Int): Int = (size / RESTARTS_PER_DEFERRED) + 1

class SA {

    fun saParallel(locations: List<Location>, scope: CoroutineScope): Map<Int, Int>{
        val invocations = numberOfDeferredInvocations(locations.size)
        val consumedResults = mutableListOf<Map<Int,Int>>()
        val masterResult = mutableMapOf<Int, Int>()
        runBlocking(scope.coroutineContext) {
            val deferredArray = Array<Deferred<Map<Int, Int>>>(invocations) { index ->
                async(Dispatchers.Default) {
                    sa(locations, RESTARTS_PER_DEFERRED)
                }
            }
            deferredArray.forEach { deferred ->
                val deferredMap = deferred.await()
                consumedResults.add(deferredMap)
            }
        }
        consumedResults.forEach { consumed ->
            mapMerge(masterResult, consumed)
        }
        return masterResult
    }

    fun mapMerge(master: MutableMap<Int, Int>, consumed: Map<Int, Int>){
        consumed.forEach { (key, value) ->
            if(master.containsKey(key)){
                val sum = master[key]?.plus(value)
                if (sum != null) master[key] = sum
            } else master[key] = value
        }
    }

    fun sa(locations: List<Location>, restarts: Int = locations.size): Map<Int, Int> {
        val hits = mutableMapOf<Int, Int>()
        if (locations.size < 10) return hits

        val numberOfCycles = 1200//1200
        val numberOfGreedyCycles = 20//20
        var state: Double
        var nextState: Double
        val initTemp = 0.8
        val minTemp = 0.0006
        val decrement = initTemp / numberOfCycles
        var delta: Double
        var probabilityAccept: Double
        val random = Random()
        val move: Double = DISCRETIZING_DISTANCE_AVERAGE * initTemp * 0.8
        var step: Int
        var direction: Direction

        repeat(restarts) {
            var temp = initTemp
            var position = random.nextInt(locations.size)
            var nextPosition = position
            for (j in 0 until numberOfCycles + numberOfGreedyCycles) {
                step = (exp(temp) + temp).roundToInt()
                if (step < 1 || j >= numberOfCycles) step = 1
                direction = getRandomDirection(random)
                if (direction == Direction.FORWARD) {
                    nextPosition =
                        if (step > 1) shiftDirectional(position, step * move, locations.lastIndex, Direction.FORWARD)
                        else if (step == 1) if (position < locations.lastIndex) position++ else locations.lastIndex
                        else position
                }
                if (direction == Direction.BACKWARD) {
                    nextPosition =
                        if (step > 1) shiftDirectional(position, step * move, locations.lastIndex, Direction.BACKWARD)
                        else if (step == 1) if (position >= 1) position-- else position
                        else position
                }

                state = (locations[position].elevation as Elevation.Value).elevation
                nextState = (locations[nextPosition].elevation as Elevation.Value).elevation

                if (nextState >= state)
                    position = nextPosition
                else if (j < numberOfCycles) {
                    delta = state - nextState
                    probabilityAccept = exp(-(temp / delta))
                    if (probabilityAccept < random.nextDouble()) position = nextPosition
                }
                if (j < numberOfCycles) temp -= decrement
                if (temp < minTemp) temp = minTemp
            }

            position = hillClimber(locations, position)
            position = lockOnFirstIfEqual(locations, position)

            if (hits.containsKey(position)) {
                val hit: Int = hits[position] ?: 0
                hits[position] = hit + 1
            } else hits[position] = 1
        }
        return hits
    }

    fun hillClimber(locations: List<Location>, position: Int): Int {
        var positionLocal = position
        if (positionLocal < 0) positionLocal = 0
        if (positionLocal > locations.lastIndex) positionLocal = locations.lastIndex
        if (locations.size < 2) return positionLocal
        if (positionLocal == 0) {
            val zero: Double = (locations[0].elevation as Elevation.Value).elevation
            val one: Double = (locations[1].elevation as Elevation.Value).elevation
            if (one > zero) positionLocal = 1 else return 0
        }
        if (positionLocal == locations.lastIndex) {
            val last: Double = (locations[locations.lastIndex].elevation as Elevation.Value).elevation
            val lastMinusOne: Double = (locations[locations.lastIndex - 1].elevation as Elevation.Value).elevation
            if (lastMinusOne > last) positionLocal = locations.lastIndex - 1 else return locations.lastIndex
        }
        while (positionLocal in 1 until locations.lastIndex) {
            val the: Double = (locations[positionLocal].elevation as Elevation.Value).elevation
            val plusOne: Double = (locations[positionLocal + 1].elevation as Elevation.Value).elevation
            val minusOne: Double = (locations[positionLocal - 1].elevation as Elevation.Value).elevation
            if (the >= minusOne && the >= plusOne) break
            if (minusOne > the) {
                positionLocal--
                continue
            }
            if (plusOne > the) positionLocal++
        }
        return positionLocal
    }

    fun lockOnFirstIfEqual(locations: List<Location>, position: Int): Int {
        var positionLocal = position
        if (positionLocal < 1) return 0
        if (positionLocal > locations.lastIndex) positionLocal = locations.lastIndex
        while (positionLocal > 0) {
            val the: Double = (locations[positionLocal].elevation as Elevation.Value).elevation
            val minusOne: Double = (locations[positionLocal - 1].elevation as Elevation.Value).elevation
            if (minusOne == the) positionLocal-- else break
        }
        return positionLocal
    }

    private fun clamp(index: Int, lastIndex: Int): Int = when {
        index in 0..lastIndex -> index
        index < 0 -> 0
        index > lastIndex -> lastIndex
        else -> 0
    }

    private fun getShiftRounded(metres: Double): Int = (metres / DISCRETIZING_DISTANCE_AVERAGE).roundToInt()

    fun shiftDirectional(
        index: Int,
        metres: Double,
        lastIndex: Int,
        direction: Direction
    ): Int {
        return when (direction) {
            Direction.FORWARD -> clamp(index + getShiftRounded(metres), lastIndex)
            Direction.BACKWARD -> clamp(index - getShiftRounded(metres), lastIndex)
        }
    }

    fun getRandomDirection(random: Random): Direction = Direction.values()[random.nextInt(2)]
}

enum class Direction {
    FORWARD, BACKWARD
}
