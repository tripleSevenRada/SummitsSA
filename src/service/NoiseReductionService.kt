package service

import data.SmoothResult
import dataClasses.Location
import preprocessing.SimpleBlur
import preprocessing.Weight

val NOISE_REDUCTION_WEIGHT = Weight.MEDIUM
class NoiseReductionService {
    fun reduceNoise(input: List<Location>): SmoothResult {
        return SimpleBlur().smooth(input, NOISE_REDUCTION_WEIGHT)
    }
}