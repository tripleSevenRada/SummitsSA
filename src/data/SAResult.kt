package data

sealed class SAResult {
    data class Success(val elevationPoints: List<ElevationPoint>): SAResult()
    data class Failure(val message: String): SAResult()
}