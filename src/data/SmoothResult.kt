package data

import dataClasses.Location

sealed class SmoothResult {
    data class Success(val smoothLocations: List<Location>): SmoothResult()
    data class Failure(val why: String = ""): SmoothResult()
}