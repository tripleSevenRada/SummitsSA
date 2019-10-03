package data

sealed class DetectResult {
    data class Success(val elevationPoints: List<ElevationPoint>): DetectResult()
    data class Failure(val why: String = ""): DetectResult()
}