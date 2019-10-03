package test

import data.SmoothResult
import dataClasses.Elevation
import dataClasses.Location
import junit.framework.Assert.assertEquals
import org.junit.Test
import preprocessing.SimpleBlur
import preprocessing.Weight
import java.util.*

class SmoothTests {
    @Test
    fun testSimpleBlur() {
        val singleValue = Array<Double>(1){100.0}
        Weight.values().forEach { weight ->
            val output = SimpleBlur().smooth(getLocationList(singleValue), weight)
            assert(output is SmoothResult.Success)
            assert((output as SmoothResult.Success).smoothLocations.size == 1)
            assertEquals(100.0, (output.smoothLocations[0].elevation as Elevation.Value).elevation)
        }
        val twoValues = Array<Double>(2){100.0}
        Weight.values().forEach { weight ->
            val output = SimpleBlur().smooth(getLocationList(twoValues), weight)
            assert(output is SmoothResult.Success)
            assert((output as SmoothResult.Success).smoothLocations.size == 2)
            assertEquals(100.0, (output.smoothLocations[0].elevation as Elevation.Value).elevation)
            assertEquals(100.0, (output.smoothLocations[1].elevation as Elevation.Value).elevation)
        }
        val rnd = Random()
        val manyValues = Array<Double>(30){rnd.nextDouble() * 100.0}
        Weight.values().forEach { weight ->
            val output = SimpleBlur().smooth(getLocationList(manyValues), weight)
            assert(output is SmoothResult.Success)
            assert((output as SmoothResult.Success).smoothLocations.size == 30)
            output.smoothLocations.forEach {
                assert((it.elevation as Elevation.Value).elevation in 0.0..100.0)
            }
            // eyeball
            println("----------manyValues------------------------------")
            println("--------------------------------------------------")
            for (i in manyValues.indices){
                println("input: ${manyValues[i]}, output: ${output.smoothLocations[i]}")
            }
        }
        val gradual = arrayOf<Double>(12.0,13.0,14.0,16.0,20.0,13.0,10.0,14.0,18.0,19.0,22.0,25.0,20.0,20.0,17.0)
        Weight.values().forEach { weight ->
            val output = SimpleBlur().smooth(getLocationList(gradual), weight)
            assert(output is SmoothResult.Success)
            assert((output as SmoothResult.Success).smoothLocations.size == gradual.size)
            output.smoothLocations.forEach {
                assert((it.elevation as Elevation.Value).elevation in 10.0..25.0)
            }
            // eyeball
            println("----------gradual---------------------------------")
            println("--------------------------------------------------")
            for (i in output.smoothLocations.indices){
                println("input: ${gradual[i]}, output: ${output.smoothLocations[i]}")
            }
        }
    }

    @Test
    fun testFailureScenarios(){
        val empty = mutableListOf<Location>()
        Weight.values().forEach { weight ->
            val output = SimpleBlur().smooth(empty, weight)
            assert(output is SmoothResult.Failure)
        }
        val containsNoValues = mutableListOf<Location>()
        containsNoValues.add(Location(0.0, 0.0, Elevation.NoValue()))
        containsNoValues.add(Location(0.0, 0.0, Elevation.Value(100.0)))
        Weight.values().forEach { weight ->
            val output = SimpleBlur().smooth(containsNoValues, weight)
            assert(output is SmoothResult.Failure)
            println(output)
        }
    }

    private fun getLocationList(elevs: Array<Double>): List<Location>{
        val mockLocations = mutableListOf<Location>()
        elevs.forEach { elev -> mockLocations.add(Location(0.0, 0.0, Elevation.Value(elev))) }
        return mockLocations
    }
}