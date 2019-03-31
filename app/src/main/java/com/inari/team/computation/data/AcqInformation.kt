package com.inari.team.computation.data

import com.inari.team.presentation.model.Mode
import com.inari.team.presentation.model.RefLocation

data class AcqInformation(
    val svList: ArrayList<SvInfo> = arrayListOf(),
    val modes: ArrayList<Mode> = arrayListOf(),
    var refLocation: RefLocation = RefLocation(),
    val timeNanosGnss: Double = 0.0,
    val tow: Int = 0,
    val now: Int = 0,
    val totalSatNumber: Int = 0,
    val ionoProto: ArrayList<Double> = arrayListOf(),
    val satellites: Satellites = Satellites()
)

data class SvInfo(
    var svIds: ArrayList<Int> = arrayListOf()
)

data class Satellites(
    var gpsSatellite: GPSSatellite = GPSSatellite(),
    var galSatellites: GALSatellite = GALSatellite()
)

data class GPSSatellite(
    var gpsL1: ArrayList<Satellite> = arrayListOf(),
    var gpsL5: ArrayList<Satellite> = arrayListOf()
)

data class GALSatellite(
    var galE1: ArrayList<Satellite> = arrayListOf(),
    var galE5a: ArrayList<Satellite> = arrayListOf()
)

data class Satellite(
    var svid: Int = 0,
    var state: Int = 0,
    val multipath: Int = 0,
    val carrierFreq: Double = 0.0,
    val tTx: Double = 0.0,
    val tRx: Double = 0.0,
    val cn0: Double = 0.0,
    val pR: Double = 0.0,
    val azimuth: Int = 0,
    val elevation: Int = 0,
    val tow: Int = 0,
    val now: Int = 0,
    val af0: Double = 0.0,
    val af1: Double = 0.0,
    val af2: Double = 0.0,
    val keplerModel: KeplerModel = KeplerModel()
)

data class KeplerModel(
    val cic: Double = 0.0,
    val cis: Double = 0.0,
    val crc: Double = 0.0,
    val cuc: Double = 0.0,
    val cus: Double = 0.0,
    val deltaN: Double = 0.0,
    val eccentricity: Double = 0.0,
    val i0: Double = 0.0,
    val iDot: Double = 0.0,
    val m0: Double = 0.0,
    val omega: Double = 0.0,
    val omega0: Double = 0.0,
    val omegaDot: Double = 0.0,
    val sqrtA: Double = 0.0,
    val toeS: Double = 0.0
)





