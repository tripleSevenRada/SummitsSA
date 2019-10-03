package test

import dataClasses.Location
import geospatial.Route

class RouteFactory {
    fun getRouteTwoPoints(): Route {
        val locationA = Location(50.0, 14.0)
        val locationB = Location(50.01, 14.01)
        val locations = mutableListOf<Location>()
        with(locations){
            add(locationA); add(locationB)
        }
        return Route(locations)
    }
}