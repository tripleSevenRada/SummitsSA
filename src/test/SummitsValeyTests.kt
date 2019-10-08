package test

import data.SmoothResult
import dataClasses.Elevation
import dataClasses.Location
import geospatial.Route
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.Test
import preprocessing.SimpleBlur
import preprocessing.Weight
import prune.FilterNoiseSummits
import sa.SA
import sa.Direction
import sa.RESTARTS_PER_DEFERRED
import sa.numberOfDeferredInvocations
import service.DiscretizingService
import java.io.File
import java.util.*
import kotlin.system.measureTimeMillis

class SummitsValleyTests {
    @Test
    fun monkeyTestSA() {
        val random = Random()
        val locations = mutableListOf<Location>()
        //repeat(4000) {
            //locations.add(Location(0.0, 0.0, Elevation.Value(random.nextDouble() * 100.0)))
        //}
        var count = 0
        repeat(80) {
            var map: Map<Int, Int> = mutableMapOf()
            var pmap: Map<Int, Int> = mutableMapOf()
            val timeMap = measureTimeMillis { map = SA().sa(locations,
                RESTARTS_PER_DEFERRED * numberOfDeferredInvocations(locations.size)) }
            val job = Job()
            val scope = CoroutineScope(Dispatchers.Default + job)
            val timePmap = measureTimeMillis { pmap = SA().saParallel(locations, scope) }
            println("\n\nlocation.size ${locations.size}")
            println("map.size - ${map.size} pmap.size - ${pmap.size}")
            println("map.time - $timeMap pmap.time - $timePmap")
            println("-----------------------------MAP")
            println(map)
            println("-----------------------------P_MAP")
            println(pmap)

            assertEquals(mapSumInt(map), mapSumInt(pmap))
            count++
            repeat(40) {
                locations.add(Location(0.0, 0.0, Elevation.Value(random.nextDouble() * 100.0)))
            }
        }
    }

    private fun <K, V: Number>mapSumInt(map: Map<K,V>): Int{
        var sum = 0
        map.forEach { k, v ->
            sum += v.toInt()
        }
        return sum
    }

    @Test
    fun testRandomDirection() {
        val random = Random()
        var f = 0
        var b = 0
        val saIns = SA()
        repeat(100) {
            if (saIns.getRandomDirection(random) == sa.Direction.FORWARD) f++ else b++
        }
        assert(f > 10)
        assert(b > 10)
    }

    @Test
    fun readFromGPXRunSAExportIntoFile() {
        val paths = mutableListOf<String>(
            "/home/radim/Dropbox/outFit/summits/s1.gpx",
            "/home/radim/Dropbox/outFit/summits/s2.gpx",
            "/home/radim/Dropbox/outFit/summits/s3.gpx",
            "/home/radim/Dropbox/outFit/summits/s4.gpx"
        )
        val outputPaths = mutableListOf<String>(
            "/home/radim/Dropbox/outFit/summits/s1-summits.gpx",
            "/home/radim/Dropbox/outFit/summits/s2-summits.gpx",
            "/home/radim/Dropbox/outFit/summits/s3-summits.gpx",
            "/home/radim/Dropbox/outFit/summits/s4-summits.gpx"
        )
        var countInOutput = 0
        paths.forEach { path ->
            val locations = parseGPX(path)
            val locationsDiscretized = DiscretizingService().discretizeParallel(Route(locations))
            val weight = Weight.VERY_LIGHT
            val smoothLocations: List<Location> = (SimpleBlur().smooth(locationsDiscretized, weight) as SmoothResult.Success).smoothLocations
            val job = Job()
            val scope = CoroutineScope(Dispatchers.Default + job)
            val map: Map<Int, Int> = SA().saParallel(smoothLocations, scope)
            println()
            println()
            println(path)
            println("export -> ${outputPaths[countInOutput]}")
            val builder = StringBuilder()

            builder.append("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n" +
                    "<gpx version=\"1.1\" creator=\"Locus Map, addon test\"\n" +
                    " xmlns=\"http://www.topografix.com/GPX/1/1\"\n" +
                    " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"\n" +
                    " xmlns:gpx_style=\"http://www.topografix.com/GPX/gpx_style/0/2\"\n" +
                    " xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\"\n" +
                    " xmlns:gpxtrkx=\"http://www.garmin.com/xmlschemas/TrackStatsExtension/v1\"\n" +
                    " xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v2\"\n" +
                    " xmlns:locus=\"http://www.locusmap.eu\">\n" +
                    "\t<metadata>\n" +
                    "\t\t<desc>File with points/tracks from an Locus addon - test</desc>\n" +
                    "\t</metadata>\n")
            builder.append(getMapAsGPXWaypoints(smoothLocations, map))
            builder.append("<trk>\n" +
                    "<name>test</name>\n" +
                    "\t<extensions>\n" +
                    "\t\t<gpx_style:line>\n" +
                    "\t\t\t<gpx_style:color>0000FF</gpx_style:color>\n" +
                    "\t\t\t<gpx_style:opacity>0.59</gpx_style:opacity>\n" +
                    "\t\t\t<gpx_style:width>6.0</gpx_style:width>\n" +
                    "\t\t</gpx_style:line>\n" +
                    "\t\t<locus:activity>cycling</locus:activity>\n" +
                    "\t\t<locus:rteComputeType>9</locus:rteComputeType>\n" +
                    "\t\t<locus:rteSimpleRoundabouts>1</locus:rteSimpleRoundabouts>\n" +
                    "\t</extensions>\n" +
                    "<trkseg>\n")
            builder.append(getLocationsAsGPXTrackpoints(smoothLocations))
            builder.append("</trkseg>\n" +
                    "</trk>\n" +
                    "</gpx>")
            File(outputPaths[countInOutput]).writeText(builder.toString())
            countInOutput++
        }
    }
    /*
    <trkpt lat="50.04977736668049" lon="14.489408934401386"><ele>281.37770600258295</ele></trkpt>
     */
    private fun getLocationsAsGPXTrackpoints(locations: List<Location>): String{
        val builder = StringBuilder()
        locations.forEach { location ->
            val lat = location.lat
            val lon = location.lon
            val ele = (location.elevation as Elevation.Value).elevation
            builder.append("<trkpt lat=\"$lat\" lon=\"$lon\"><ele>$ele</ele></trkpt>\n")
        }
        return builder.toString()
    }
    /*
    <wpt lat="46.85446996294488" lon="10.04142969759414">
        <ele>2332</ele>
        <name>Silvrettahütte (SAC)</name>
        <src>alpstein.21430.12639302</src>
        <link href="http://www.silvrettahuette.ch/"/>
        <type>Horská chata</type>
    </wpt>
    */
    private fun getMapAsGPXWaypoints(locations: List<Location>, map: Map<Int, Int>): String {
        //map: index -> hits
        val builder = StringBuilder()
        val sorted = map.toSortedMap()
        sorted.forEach { (key, value) ->
            builder.append("<wpt lat=\"${locations[key].lat}\" lon=\"${locations[key].lon}\">\n")
            builder.append("<ele>${(locations[key].elevation as Elevation.Value).elevation}</ele>\n")
            builder.append("<name>hits:$value</name>\n")
            builder.append("</wpt>\n")
        }
        return builder.toString()
    }

    @Test
    fun testShiftForwardBackward() {
        val sa = SA()
        val lastIndex = 49 // 0-49
        val distances = mutableListOf(15.0, 30.0, 45.0, 60.0, 130.0)
        distances.forEach { distance ->
            println("distance: $distance")
            val shiftedForward = sa.shiftDirectional(lastIndex, distance, lastIndex, Direction.FORWARD)
            assertEquals(lastIndex, shiftedForward)
            val shiftedBackward = sa.shiftDirectional(0, distance, lastIndex, Direction.BACKWARD)
            assertEquals(0, shiftedBackward)
            for (i in -10..100) {
                val shiftedForward2 = sa.shiftDirectional(i, distance, lastIndex, Direction.FORWARD)
                val shiftedBackward2 = sa.shiftDirectional(i, distance, lastIndex, Direction.BACKWARD)
                assert(shiftedForward2 in 0..49 && shiftedBackward2 in 0..49)
                if (i < lastIndex) assert(shiftedForward2 > i)
                if (i > 0) assert(shiftedBackward2 < i)
                println("i: $i, distance: $distance, shiftedForward: $shiftedForward2, shiftedBackward: $shiftedBackward2")
            }
        }
    }

    @Test
    fun testMapMerge(){
        val master = mutableMapOf<Int, Int>()
        val consumed = mutableMapOf<Int, Int>()
        val sa = SA()
        sa.mapMerge(master, consumed)
        assert(master.isEmpty())
        consumed[1] = 1
        sa.mapMerge(master, consumed)
        assert(master.size == 1)
        assert(master[1] == 1)
        sa.mapMerge(master, consumed)
        assert(master.size == 1)
        assert(master[1] == 2)
        sa.mapMerge(master, consumed)
        assert(master.size == 1)
        assert(master[1] == 3)
        consumed.clear()
        consumed[2] = 22
        sa.mapMerge(master, consumed)
        assert(master.size == 2)
        assert(master[1] == 3 && master[2] == 22)
    }

    @Test
    fun testLockOnFirstIfEqual() {
        val locations = mutableListOf<Location>()
        locations.add(Location(0.0, 0.0, Elevation.Value(10.0)))
        locations.add(Location(0.0, 0.0, Elevation.Value(11.0)))
        repeat(7) {
            locations.add(Location(0.0, 0.0, Elevation.Value(12.0)))
        }
        // size = 9
        // indices 0..8
        val sa = SA()
        val locked1 = sa.lockOnFirstIfEqual(locations, -1)
        val locked2 = sa.lockOnFirstIfEqual(locations, 0)
        assert(locked1 == 0 && locked2 == 0)
        val locked3 = sa.lockOnFirstIfEqual(locations, 1)
        assertEquals(1, locked3)
        for (i in 2..20) {
            val locked4 = sa.lockOnFirstIfEqual(locations, i)
            assertEquals(2, locked4)
        }
        val locations2 = mutableListOf<Location>()
        repeat(7) {
            locations2.add(Location(0.0, 0.0, Elevation.Value(12.0)))
        }
        for (i in -2..20) {
            val locked5 = sa.lockOnFirstIfEqual(locations2, i)
            assertEquals(0, locked5)
        }
        val locations3 = mutableListOf<Location>()
        repeat(3) {
            locations3.add(Location(0.0, 0.0, Elevation.Value(12.0)))
        }
        locations3.add(Location(0.0, 0.0, Elevation.Value(11.0)))
        repeat(3) {
            locations3.add(Location(0.0, 0.0, Elevation.Value(12.0)))
        }
        val locked6 = sa.lockOnFirstIfEqual(locations3, 2)
        assertEquals(0, locked6)
        val locked7 = sa.lockOnFirstIfEqual(locations3, 3)
        assertEquals(3, locked7)
        val locked8 = sa.lockOnFirstIfEqual(locations3, 5)
        assertEquals(4, locked8)
    }

    @Test
    fun testHillClimberAndLockOnFirstIfEqual() {
        val sa = SA()
        val positionsToTest = arrayOf(-10, 10, -1, 3, 0, 1, 2)
        val positionsToTestLonger = arrayOf(-10, 10, -1, 5, 0, 1, 2, 3, 4)


        val case0 = mutableListOf<Location>()
        val case0Array = arrayOf(10.0, 9.0, 8.0)
        case0Array.forEach { value ->
            case0.add(Location(0.0, 0.0, Elevation.Value(value)))
        }
        positionsToTest.forEach { position ->
            val climbed = sa.hillClimber(case0, position)
            val locked = sa.lockOnFirstIfEqual(case0, climbed)
            assertEquals(locked, climbed)
            assertEquals(0, climbed)
        }


        val case1 = mutableListOf<Location>()
        val case1Array = arrayOf(10.0, 11.0, 12.0)
        case1Array.forEach { value ->
            case1.add(Location(0.0, 0.0, Elevation.Value(value)))
        }
        positionsToTest.forEach { position ->
            assertEquals(2, sa.hillClimber(case1, position))
        }


        val case2 = mutableListOf<Location>()
        val case2Array = arrayOf(10.0, 10.0, 10.0)
        case2Array.forEach { value ->
            case2.add(Location(0.0, 0.0, Elevation.Value(value)))
        }


        val case3 = mutableListOf<Location>()
        val case3Array = arrayOf(8.0, 9.0, 10.0)
        case3Array.forEach { value ->
            case3.add(Location(0.0, 0.0, Elevation.Value(value)))
        }
        positionsToTest.forEach { position ->
            assertEquals(2, sa.hillClimber(case3, position))
        }


        val case4 = mutableListOf<Location>()
        val case4Array = arrayOf(12.0, 11.0, 10.0)
        case4Array.forEach { value ->
            case4.add(Location(0.0, 0.0, Elevation.Value(value)))
        }
        positionsToTest.forEach { position ->
            assertEquals(0, sa.hillClimber(case4, position))
        }


        val case5 = mutableListOf<Location>()
        val case5Array = arrayOf(10.0, 10.0, 10.0)
        case5Array.forEach { value ->
            case5.add(Location(0.0, 0.0, Elevation.Value(value)))
        }
        assertEquals(0, sa.hillClimber(case5, -10))
        assertEquals(0, sa.hillClimber(case5, -1))
        assertEquals(0, sa.hillClimber(case5, 0))
        assertEquals(1, sa.hillClimber(case5, 1))
        assertEquals(2, sa.hillClimber(case5, 2))
        assertEquals(2, sa.hillClimber(case5, 3))
        assertEquals(2, sa.hillClimber(case5, 10))


        val case6 = mutableListOf<Location>()
        val case6Array = arrayOf(10.0, 11.0, 12.0, 11.0, 10.0)
        case6Array.forEach { value ->
            case6.add(Location(0.0, 0.0, Elevation.Value(value)))
        }
        positionsToTestLonger.forEach { position ->
            val climbed = sa.hillClimber(case6, position)
            val locked = sa.lockOnFirstIfEqual(case6, climbed)
            assertEquals(locked, climbed)
            assertEquals(2, climbed)
        }


        val case7 = mutableListOf<Location>()
        val case7Array = arrayOf(10.0, 11.0, 12.0, 11.0, 12.0, 13.0, 12.0, 13.0, 12.0, 11.0, 10.0, 11.0, 10.0)
        case7Array.forEach { value ->
            case7.add(Location(0.0, 0.0, Elevation.Value(value)))
        }
        val rnd = Random()
        repeat(10000) {
            val position = rnd.nextInt(100) - 20
            val climbed = sa.hillClimber(case7, position)
            val locked = sa.lockOnFirstIfEqual(case7, climbed)
            assertEquals(locked, climbed)
            when (position) {
                in -100..3 -> assertEquals(2, climbed)
                in 4..6 -> assertEquals(5, climbed)
                in 7..10 -> assertEquals(7, climbed)
                in 11..100 -> assertEquals(11, climbed)
            }
        }
    }

    @Test
    fun testFilterNoiseSummits(){
        val map = mutableMapOf<Int, Int>()
        for(i in 0 until 20) map[i] = 5
        for (i in 20 until 29) map[i] = 311
        val filtered = FilterNoiseSummits().filterMap(map)
        assertEquals(9, filtered.size)
    }
}