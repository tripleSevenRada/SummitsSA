package prune

const val NOISE_LEVEL_SA = 0.08
class FilterNoiseSummits: SAHitsFilter {
    override fun filterMap(map: MutableMap<Int, Int>): Map<Int, Int> {
        val hitsPerSummit: Double = map.values.sumBy { it }.toDouble() / map.size.toDouble()
        val noiseLevel: Double = hitsPerSummit * NOISE_LEVEL_SA
        return map.filterValues { value -> value.toDouble() > noiseLevel}
    }
}