package prune

import data.ElevationPoint

interface SAHitsFilter{
    fun filterMap(map: MutableMap<Int, Int>): Map<Int, Int>
}

interface SummitsFilter {
    fun filterSummits(points: List<ElevationPoint>): List<ElevationPoint>
}
interface ValleysFilter {
    fun filterValleys(points: List<ElevationPoint>): List<ElevationPoint>
}