package service

import data.SAResult
import data.SmoothResult
import dataClasses.Location
import geospatial.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import preprocessing.SimpleBlur
import preprocessing.Weight
import sa.RESTARTS_PER_DEFERRED
import sa.SA
import sa.numberOfDeferredInvocations

class SummitsValleysService {
    fun getElevationPoints(locations: List<Location>): SAResult{
        val locationsDiscretized = DiscretizingService().discretizeParallel(Route(locations))
        val weight = Weight.MEDIUM
        val locationsSmooth: List<Location> =
            when (val smoothResult: SmoothResult = SimpleBlur().smooth(locationsDiscretized, weight)){
            is SmoothResult.Success -> smoothResult.smoothLocations
            is SmoothResult.Failure -> locationsDiscretized
        }
        val job = Job()
        val scope = CoroutineScope(Dispatchers.Default + job)
        val map: Map<Int, Int> = SA().saParallel(locationsSmooth, scope)
        val totalRestarts = numberOfDeferredInvocations(locationsSmooth.size) * RESTARTS_PER_DEFERRED

        return SAResult.Failure("not implemented")
    }
}