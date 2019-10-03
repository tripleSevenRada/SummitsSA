package service

import dataClasses.Location
import geospatial.DiscretizerWrapper
import geospatial.Route

const val DISCRETIZING_DISTANCE = 20.0
const val DISCRETIZING_DISTANCE_AVERAGE = 15.0
class DiscretizingService {
    fun discretizeParallel(route: Route): List<Location> {
        return DiscretizerWrapper().discretizeParallel(route, DISCRETIZING_DISTANCE)
    }
}