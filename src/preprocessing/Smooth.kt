package preprocessing

import data.SmoothResult
import dataClasses.Elevation
import dataClasses.Location

//https://stackoverflow.com/questions/20618804/how-to-smooth-a-curve-in-the-right-way

class SimpleBlur : SmoothElevTransform {

    private val weightToKernelSize: Map<Weight, Int> = mutableMapOf(
        Weight.VERY_LIGHT to 3,
        Weight.LIGHT to 5,
        Weight.MEDIUM to 7,
        Weight.HEAVY to 9,
        Weight.VERY_HEAVY to 11
    )

    override fun smooth(input: List<Location>, weight: Weight): SmoothResult {
        val elevationValue: (Location) -> Double  = {location -> (location.elevation as Elevation.Value).elevation}
        val accumulator: (Double, Int, Double) -> Double = { sum, index, padding ->
            sum + if (index in input.indices) { elevationValue(input[index]) } else padding
        }

        // validate input
        if (input.isEmpty()) return SmoothResult.Failure("empty")
        var noValueCount = 0
        input.forEach{ location -> if(location.elevation is Elevation.NoValue) noValueCount ++ }
        if (noValueCount > 0) return SmoothResult.Failure("contains $noValueCount no value element(s)")
        // input is valid, all locations have elevation value

        val outputList = mutableListOf<Location>()
        val kernelSize: Int = weightToKernelSize[weight] ?: 7
        val kernelWings: Int = (kernelSize - 1) / 2
        val paddingLeft = elevationValue(input[0])
        val paddingRight = elevationValue(input[input.lastIndex])

        for (i in input.indices) {
            var sum = 0.0
            for (l in (i - kernelWings)..i) {
                sum = accumulator(sum, l, paddingLeft)
            }
            for (r in i + 1..(i + kernelWings)) {
                sum = accumulator(sum, r, paddingRight)
            }
            outputList.add(
                Location(
                    input[i].lat,
                    input[i].lon,
                    Elevation.Value(sum / kernelSize.toDouble())
                )
            )
        }
        return SmoothResult.Success(outputList)
    }
}

interface SmoothElevTransform {
    fun smooth(input: List<Location>, weight: Weight): SmoothResult
}

enum class Weight {
    VERY_LIGHT, LIGHT, MEDIUM, HEAVY, VERY_HEAVY
}