package prune

import data.ElevationPoint
import dataClasses.Elevation
import dataClasses.Location

class SummitsFilterUpDown(val up: Double, val down: Double, val locations: List<Location>) : SummitsFilter {
    override fun filterSummits(points: List<ElevationPoint>): List<ElevationPoint> {
        TODO()
    }
}

fun getLocalMin(A: ElevationPoint, B: ElevationPoint, locations: List<Location>): Double {
    val minLoc = locations.subList(A.index, B.index).minBy { (it.elevation as Elevation.Value).elevation }
    return if (minLoc == null) (locations[A.index].elevation as Elevation.Value).elevation else
        (minLoc.elevation as Elevation.Value).elevation
}

fun convertHitsMapToListSummits(locations: List<Location>, hitsMap: Map<Int, Int>): List<ElevationPoint> {
    val elevationPoints = mutableListOf<ElevationPoint>()
    hitsMap.forEach { (key, _) ->
        if (locations[key].elevation is Elevation.Value) {
            val elevValue = (locations[key].elevation as Elevation.Value).elevation
            elevationPoints.add(ElevationPoint.Summit(elevValue, key))
        }
    }
    return elevationPoints.sorted()
}