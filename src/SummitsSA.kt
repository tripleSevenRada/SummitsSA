import data.DetectResult
import data.SAResult
import data.SmoothResult
import dataClasses.Location
import geospatial.Route
import service.DiscretizingService
import service.NoiseReductionService
import service.SummitsValleysService

class SummitsSA {
    fun detect(route: Route): DetectResult {
        val discretized: List<Location> = DiscretizingService().discretizeParallel(route)
        val smoothResult: SmoothResult = NoiseReductionService().reduceNoise(discretized)
        if (smoothResult is SmoothResult.Failure) return DetectResult.Failure("smooth failed: $smoothResult")
        val saResult: SAResult =
            SummitsValleysService().getElevationPoints((smoothResult as SmoothResult.Success).smoothLocations)
        return if (saResult is SAResult.Success) DetectResult.Success(saResult.elevationPoints)
        else DetectResult.Failure("sa failed: $saResult")
    }
}