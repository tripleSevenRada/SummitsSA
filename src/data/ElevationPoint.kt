package data

sealed class ElevationPoint(): Indexable, Comparable<Indexable>{
    abstract val elevation: Double
    abstract val index: Int
    override fun getIndexInLocations() = index
    override fun compareTo(other: Indexable): Int {
        return index.compareTo(other.getIndexInLocations())
    }
    data class Summit(override val elevation: Double, override val index: Int) : ElevationPoint()
    data class Valley(override val elevation: Double, override val index: Int) : ElevationPoint()
}
interface Indexable{
    fun getIndexInLocations(): Int
}