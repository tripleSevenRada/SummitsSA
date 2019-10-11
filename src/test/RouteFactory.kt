package test

import dataClasses.Elevation
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

    fun getRouteContainingNoValueElevation(): Route{
        val incr = 0.01
        val locations = mutableListOf<Location>()
        repeat(100){
            locations.add(Location(50.0 + (it.toDouble() * incr),
                14.0 + (it.toDouble() * incr),
                Elevation.Value(100.0 + (it * incr * 100))))
        }
        locations.add(Location(50.0, 14.0, Elevation.NoValue("testing failure")))
        return Route(locations)
    }
}