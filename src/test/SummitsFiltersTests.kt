package test

import data.ElevationPoint
import dataClasses.Elevation
import dataClasses.Location
import org.junit.Test
import prune.convertHitsMapToListSummits
import prune.getLocalMin
import kotlin.test.assertEquals

class SummitsFiltersTests {

    @Test
    fun testConvertHitsMapToSummitsLists(){
        val map = mutableMapOf<Int, Int>()
        val locations = mutableListOf<Location>()
        repeat(25){
            locations.add(Location(1.0,1.0, Elevation.Value(1.0)))
        }
        map[12] = 12
        map[1] = 1
        map[22] = 22
        map[3] = 3
        map[8] = 8
        map[9] = 9
        val listSummits = convertHitsMapToListSummits(locations, map)
        assertEquals(1,listSummits[0].index)
        assertEquals(3,listSummits[1].index)
        assertEquals(8,listSummits[2].index)
        assertEquals(9,listSummits[3].index)
        assertEquals(12,listSummits[4].index)
        assertEquals(22,listSummits[5].index)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetLocMin(){
        val locations = mutableListOf<Location>()
        locations.add(Location(1.0, 2.0, Elevation.Value(10.0))) // 0
        locations.add(Location(1.0, 2.0, Elevation.Value(9.0)))
        locations.add(Location(1.0, 2.0, Elevation.Value(8.0)))
        locations.add(Location(1.0, 2.0, Elevation.Value(7.0)))
        locations.add(Location(1.0, 2.0, Elevation.Value(10.0)))
        locations.add(Location(1.0, 2.0, Elevation.Value(11.0)))
        locations.add(Location(1.0, 2.0, Elevation.Value(6.0))) // 6

        val point1 = ElevationPoint.Summit(9.0, 1)
        val point2 = ElevationPoint.Summit(11.0, 5)

        val minExpectValid = getLocalMin(point1, point2, locations)
        assertEquals(7.0, minExpectValid)

        val point3 = ElevationPoint.Summit(10.0,0)
        val point4 = ElevationPoint.Summit(6.0,6)

        val minExpectValidFullScan = getLocalMin(point3, point4, locations)
        assertEquals(6.0, minExpectValidFullScan)

        // (expected = IllegalArgumentException::class)
        val minExpectInvalid = getLocalMin(point2, point1, locations)
    }

}