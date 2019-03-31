package com.inari.team.computation.data

data class GeometricMatrix(
    var geometric: ArrayList<XYZBias> = arrayListOf(),
    var pRs: ArrayList<Double> = arrayListOf(),
    var cn0s: ArrayList<Double> = arrayListOf(),
    var constellation: Int = 0
)

data class XYZBias(
    var x: Double,
    var y: Double,
    var z: Double,
    var bias: Double
)