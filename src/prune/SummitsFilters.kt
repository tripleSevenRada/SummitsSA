package prune

import data.ElevationPoint
import dataClasses.Elevation
import dataClasses.Location

const val UP_AND_DOWN_DEFAULT = 25.0
class SummitsFilterUpDown(val upAndDown: Double = UP_AND_DOWN_DEFAULT, val locations: List<Location>) : SummitsFilter {
    override fun filterSummits(points: List<ElevationPoint>): List<ElevationPoint> {
        TODO()
    }
}

fun intervalIsValid(left: ElevationPoint, right: ElevationPoint, locations: List<Location>){
    
}

@Throws(IllegalArgumentException::class)
fun getLocalMin(A: ElevationPoint, B: ElevationPoint, locations: List<Location>): Double {
    val minLoc = locations.subList(A.index, (B.index +1)).minBy { (it.elevation as Elevation.Value).elevation } ?:
        throw java.lang.IllegalArgumentException("min loc is null")
    return (minLoc.elevation as Elevation.Value).elevation
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