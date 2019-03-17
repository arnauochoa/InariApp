#include "extract_info.h"

void extract_info(json gnssInfo, Info& acqInfo){

    //Info acqInfo;

    // Initial declarations
    int c = 299792458;

    // Flags
    acqInfo.flags.constellations.GPS = gnssInfo["Params"]["constellations"]["GPS"];
    acqInfo.flags.constellations.GALILEO = gnssInfo["Params"]["constellations"]["Galileo"];
    acqInfo.flags.constellations.GLONASS = gnssInfo["Params"]["constellations"]["GLONASS"];

    acqInfo.flags.corrections.ionosphere = gnssInfo["Params"]["corrections"]["ionosphere"];
    acqInfo.flags.corrections.troposphere = gnssInfo["Params"]["corrections"]["troposphere"];
    acqInfo.flags.corrections.mutipath = gnssInfo["Params"]["corrections"]["multipath"];
    acqInfo.flags.corrections.ppp = gnssInfo["Params"]["corrections"]["PPP"];
    acqInfo.flags.corrections.camera = gnssInfo["Params"]["corrections"]["camera"];


    // Location
    acqInfo.RefLocation.LLH.latitute = gnssInfo["Location"]["latitude"];
    acqInfo.RefLocation.LLH.longitude = gnssInfo["Location"]["longitude"];
    acqInfo.RefLocation.LLH.altitude = gnssInfo["Location"]["altitude"];


    //lla2ecef
    // COMMENT: It does not return the exact same number
    lla2ecef(acqInfo.RefLocation.LLH.latitute, acqInfo.RefLocation.LLH.longitude, acqInfo.RefLocation.LLH.altitude,
    acqInfo.RefLocation.XYZ.x, acqInfo.RefLocation.XYZ.y, acqInfo.RefLocation.XYZ.z);


    // Clock info
    acqInfo.nsrxTime = gnssInfo["Clock"]["timeNanos"];
    if(gnssInfo["Clock"]["hasBiasNanos"] == true){
        double biasNanos = gnssInfo["Clock"]["biasNanos"];
        double fullBias = gnssInfo["Clock"]["fullBiasNanos"];
        acqInfo.nsGPSTime = acqInfo.nsrxTime - ( biasNanos + fullBias);
    }



    double tow;
    double now;
    nsgpst2gpst(acqInfo.nsGPSTime, tow, now);
    acqInfo.tow = tow;
    acqInfo.now = now;


    // Measurements

    for(int i = 0; i < gnssInfo["Meas"].size(); i++){
        int constellationType = gnssInfo["Meas"][i]["constellationType"];

        switch (constellationType)
        {
            case 1:
            SVMember GPS; 
            GPS.svid            = gnssInfo["Meas"][i]["svid"];
            GPS.carrierFreq     = gnssInfo["Meas"][i]["carrierFrequencyHz"];
            GPS.t_tx            = gnssInfo["Meas"][i]["receivedSvTimeNanos"];
            GPS.pseudorangeRate = gnssInfo["Meas"][i]["pseudorangeRateMetersPerSecond"];
            GPS.p               = pseudo_gen(GPS.t_tx,tow,c);
            acqInfo.sv.GPS.push_back(GPS);
                break;

            case 2:
            SVMember SBAS; 
            SBAS.svid            = gnssInfo["Meas"][i]["svid"];
            SBAS.carrierFreq     = gnssInfo["Meas"][i]["carrierFrequencyHz"];
            SBAS.t_tx            = gnssInfo["Meas"][i]["receivedSvTimeNanos"];
            SBAS.pseudorangeRate = gnssInfo["Meas"][i]["pseudorangeRateMetersPerSecond"];
            SBAS.CNO             = gnssInfo["Meas"][i]["cn0DbHz"];
            acqInfo.sv.SBAS.push_back(SBAS);

            case 3:
            SVMember GLONASS; 
            GLONASS.svid            = gnssInfo["Meas"][i]["svid"];
            GLONASS.carrierFreq     = gnssInfo["Meas"][i]["carrierFrequencyHz"];
            GLONASS.t_tx            = gnssInfo["Meas"][i]["receivedSvTimeNanos"];
            GLONASS.pseudorangeRate = gnssInfo["Meas"][i]["pseudorangeRateMetersPerSecond"];
            GLONASS.CNO             = gnssInfo["Meas"][i]["cn0DbHz"];
            acqInfo.sv.GLONASS.push_back(GLONASS);

            case 4:
            SVMember QZSS; 
            QZSS.svid            = gnssInfo["Meas"][i]["svid"];
            QZSS.carrierFreq     = gnssInfo["Meas"][i]["carrierFrequencyHz"];
            QZSS.t_tx            = gnssInfo["Meas"][i]["receivedSvTimeNanos"];
            QZSS.pseudorangeRate = gnssInfo["Meas"][i]["pseudorangeRateMetersPerSecond"];
            QZSS.CNO             = gnssInfo["Meas"][i]["cn0DbHz"];
            acqInfo.sv.QZSS.push_back(QZSS);

            case 5:
            SVMember BEIDU; 
            BEIDU.svid            = gnssInfo["Meas"][i]["svid"];
            BEIDU.carrierFreq     = gnssInfo["Meas"][i]["carrierFrequencyHz"];
            BEIDU.t_tx            = gnssInfo["Meas"][i]["receivedSvTimeNanos"];
            BEIDU.pseudorangeRate = gnssInfo["Meas"][i]["pseudorangeRateMetersPerSecond"];
            BEIDU.CNO             = gnssInfo["Meas"][i]["cn0DbHz"];
            acqInfo.sv.BEIDU.push_back(BEIDU);

            case 6:
            SVMember GALILEO; 
            GALILEO.svid            = gnssInfo["Meas"][i]["svid"];
            GALILEO.carrierFreq     = gnssInfo["Meas"][i]["carrierFrequencyHz"];
            GALILEO.t_tx            = gnssInfo["Meas"][i]["receivedSvTimeNanos"];
            GALILEO.pseudorangeRate = gnssInfo["Meas"][i]["pseudorangeRateMetersPerSecond"];
            GALILEO.CNO             = gnssInfo["Meas"][i]["cn0DbHz"];
            acqInfo.sv.GALILEO.push_back(GALILEO);
        
            default:
            SVMember UNK; 
            UNK.svid            = gnssInfo["Meas"][i]["svid"];
            UNK.carrierFreq     = gnssInfo["Meas"][i]["carrierFrequencyHz"];
            UNK.t_tx            = gnssInfo["Meas"][i]["receivedSvTimeNanos"];
            UNK.pseudorangeRate = gnssInfo["Meas"][i]["pseudorangeRateMetersPerSecond"];
            UNK.CNO             = gnssInfo["Meas"][i]["cn0DbHz"];
            acqInfo.sv.UNK.push_back(UNK);
                break;
        }
    }

    // SUPL information
    // GPS
    int gpsEphData = gnssInfo["ephData"]["GPS"].size();
    for(int i = 0; i < gpsEphData; i++){
        for(int j = 0; j < acqInfo.sv.GPS.size(); j++){
            int ephDataSvid = gnssInfo["ephData"]["GPS"][i]["svid"];
            if(acqInfo.sv.GPS[j].svid == ephDataSvid){

                acqInfo.sv.GPS[j].tow = gnssInfo["ephData"]["GPS"][i]["tocS"];
                acqInfo.sv.GPS[j].now = gnssInfo["ephData"]["GPS"][i]["week"];
                acqInfo.sv.GPS[j].af0 = gnssInfo["ephData"]["GPS"][i]["af0S"];
                acqInfo.sv.GPS[j].af1 = gnssInfo["ephData"]["GPS"][i]["af1SecPerSec"];
                acqInfo.sv.GPS[j].af2 = gnssInfo["ephData"]["GPS"][i]["af2SecPerSec2"];
                acqInfo.sv.GPS[j].tgds = gnssInfo["ephData"]["GPS"][i]["tgdS"];
                
                // kepler model
                acqInfo.sv.GPS[j].keplerModel.cic = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["cic"];
                acqInfo.sv.GPS[j].keplerModel.cis = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["cis"];
                acqInfo.sv.GPS[j].keplerModel.crc = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["crc"];
                acqInfo.sv.GPS[j].keplerModel.crs = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["cis"];
                acqInfo.sv.GPS[j].keplerModel.cuc = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["cuc"];
                acqInfo.sv.GPS[j].keplerModel.cus = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["cus"];
                acqInfo.sv.GPS[j].keplerModel.deltaN = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["deltaN"];
                acqInfo.sv.GPS[j].keplerModel.eccentricity = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["eccentricity"];
                acqInfo.sv.GPS[j].keplerModel.i0 = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["i0"];
                acqInfo.sv.GPS[j].keplerModel.iDot = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["iDot"];
                acqInfo.sv.GPS[j].keplerModel.m0 = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["m0"];
                acqInfo.sv.GPS[j].keplerModel.omega = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["omega"];
                acqInfo.sv.GPS[j].keplerModel.omega0 = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["omega0"];
                acqInfo.sv.GPS[j].keplerModel.omegaDot = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["omegaDot"];
                acqInfo.sv.GPS[j].keplerModel.sqrtA = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["sqrtA"];
                acqInfo.sv.GPS[j].keplerModel.toeS = gnssInfo["ephData"]["GPS"][i]["keplerModel"]["toeS"];

            }
        }
    }

    int galEphData = gnssInfo["ephData"]["Galileo"].size();
    for(int i = 0; i < galEphData; i++){
        for(int j = 0; j < acqInfo.sv.GALILEO.size(); j++){
            int ephDataSvid = gnssInfo["ephData"]["Galileo"][i]["svid"];
            if(acqInfo.sv.GALILEO[j].svid == ephDataSvid){
                
                acqInfo.sv.GALILEO[j].tow = gnssInfo["ephData"]["Galileo"][i]["tocS"];
                acqInfo.sv.GALILEO[j].now = gnssInfo["ephData"]["Galileo"][i]["week"];
                acqInfo.sv.GALILEO[j].af0 = gnssInfo["ephData"]["Galileo"][i]["af0S"];
                acqInfo.sv.GALILEO[j].af1 = gnssInfo["ephData"]["Galileo"][i]["af1SecPerSec"];
                acqInfo.sv.GALILEO[j].af2 = gnssInfo["ephData"]["Galileo"][i]["af2SecPerSec2"];
                acqInfo.sv.GALILEO[j].tgds = gnssInfo["ephData"]["Galileo"][i]["tgdS"];

                // kepler model
                acqInfo.sv.GALILEO[j].keplerModel.cic = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["cic"];
                acqInfo.sv.GALILEO[j].keplerModel.cis = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["cis"];
                acqInfo.sv.GALILEO[j].keplerModel.crc = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["crc"];
                acqInfo.sv.GALILEO[j].keplerModel.crs = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["cis"];
                acqInfo.sv.GALILEO[j].keplerModel.cuc = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["cuc"];
                acqInfo.sv.GALILEO[j].keplerModel.cus = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["cus"];
                acqInfo.sv.GALILEO[j].keplerModel.deltaN = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["deltaN"];
                acqInfo.sv.GALILEO[j].keplerModel.eccentricity = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["eccentricity"];
                acqInfo.sv.GALILEO[j].keplerModel.i0 = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["i0"];
                acqInfo.sv.GALILEO[j].keplerModel.iDot = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["iDot"];
                acqInfo.sv.GALILEO[j].keplerModel.m0 = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["m0"];
                acqInfo.sv.GALILEO[j].keplerModel.omega = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["omega"];
                acqInfo.sv.GALILEO[j].keplerModel.omega0 = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["omega0"];
                acqInfo.sv.GALILEO[j].keplerModel.omegaDot = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["omegaDot"];
                acqInfo.sv.GALILEO[j].keplerModel.sqrtA = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["sqrtA"];
                acqInfo.sv.GALILEO[j].keplerModel.toeS = gnssInfo["ephData"]["Galileo"][i]["keplerModel"]["toeS"];
                
            }
        }
    }


    acqInfo.ionoProto[0] = gnssInfo["ephData"]["Klobuchar"]["alpha_"][0];
    acqInfo.ionoProto[1] = gnssInfo["ephData"]["Klobuchar"]["alpha_"][1];
    acqInfo.ionoProto[2] = gnssInfo["ephData"]["Klobuchar"]["alpha_"][2];
    acqInfo.ionoProto[3] = gnssInfo["ephData"]["Klobuchar"]["alpha_"][3];
    acqInfo.ionoProto[4] = gnssInfo["ephData"]["Klobuchar"]["beta_"][0];
    acqInfo.ionoProto[5] = gnssInfo["ephData"]["Klobuchar"]["beta_"][1];
    acqInfo.ionoProto[6] = gnssInfo["ephData"]["Klobuchar"]["beta_"][2];
    acqInfo.ionoProto[7] = gnssInfo["ephData"]["Klobuchar"]["beta_"][3];



    acqInfo.svs = gpsEphData + galEphData;
}