package com.inari.team.computation.corrections

import kotlin.math.pow

/**
 * Computes ionosphere correction for band 'frequencies[0]' using  measurements of band 'frequencies[0]' and
 * band 'frequencies[1]'
 */
fun getIonoCorrDualFreq(frequencies: ArrayList<Double>, pseudoranges: ArrayList<Double>): Double {
    val freq1 = frequencies[0]
    val freq2 = frequencies[1]
    val pr1 = pseudoranges[0]
    val pr2 = pseudoranges[1]

    // Compute frequency factor
    val fact = freq2.pow(2) / (freq1.pow(2) - freq2.pow(2))

    // Return iono correction
    return fact * (pr2 - pr1)
}
