import data.DetectResult
import data.SAResult
import data.SmoothResult
import dataClasses.Elevation
import dataClasses.Location
import geospatial.Route
import service.DiscretizingService
import service.NoiseReductionService
import service.SummitsValleysService

class SummitsSA {
    fun detect(route: Route): DetectResult {
        if (route.getElements().any { it.elevation is Elevation.NoValue })
            return DetectResult.Failure("contains no value elevation")
        val discretized: List<Location> = DiscretizingService().discretizeParallel(route)
        assert(discretized.all { it.elevation is Elevation.Value })
        val smoothResult: SmoothResult = NoiseReductionService().reduceNoise(discretized)
        if (smoothResult is SmoothResult.Failure)
            return DetectResult.Failure("smooth failed: $smoothResult")
        assert((smoothResult as SmoothResult.Success).smoothLocations.all { it.elevation is Elevation.Value })
        val saResult: SAResult = SummitsValleysService().getElevationPoints(smoothResult.smoothLocations)
        return if (saResult is SAResult.Success) DetectResult.Success(saResult.elevationPoints)
        else DetectResult.Failure("sa failed: $saResult")
    }
}