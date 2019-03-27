#include "extract_info.h"

void extract_info(json allGnssInfo, std::vector<Info>& vAcqInfo, std::vector<Mode>& modes){
    
    // Initial declarations
    Info acqInfo;
    json gnssInfo;
    Mode mode;
    json infoMode;
    int c = 299792458;

    for(int m = 0; m < allGnssInfo["modes"].size(); m++){
        infoMode =  allGnssInfo["modes"][m];
        
        mode.algorithm = (int)infoMode["algorithm"];

        for(int i = 0; i < infoMode["bands"].size(); i++){
            if(infoMode["bands"][i] == 1)
                mode.L1 = true;
            if(infoMode["bands"][i] == 2)
                mode.L5 = true;
        }

        for(int i = 0; i < infoMode["constellations"].size(); i++){
            if(infoMode["constellations"][i] == 1)
                mode.gps = true;
            if(infoMode["constellations"][i] == 2)
                mode.gal = true;
        }

        for(int i = 0; i < infoMode["corrections"].size(); i++){
            if(infoMode["corrections"][i] == 1)
                mode.iono = true;
            if(infoMode["corrections"][i] == 2)
                mode.tropo = true;
            if(infoMode["corrections"][i] == 3)
                mode.dual = true;
        }

        modes.push_back(mode);

    }


    for (int k = 0; k < allGnssInfo["MeasData"].size(); k++){
        
        gnssInfo = allGnssInfo["MeasData"][k];
        
        // Location
        acqInfo.RefLocation.LLH.latitute = allGnssInfo["location"]["latitude"];
        acqInfo.RefLocation.LLH.longitude = allGnssInfo["location"]["longitude"];
        acqInfo.RefLocation.LLH.altitude = allGnssInfo["location"]["altitude"];


        //lla2ecef
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
        int gpsEphData = allGnssInfo["ephData"]["GPS"].size();
        for(int i = 0; i < gpsEphData; i++){
            for(int j = 0; j < acqInfo.sv.GPS.size(); j++){
                int ephDataSvid = allGnssInfo["ephData"]["GPS"][i]["svid"];
                if(acqInfo.sv.GPS[j].svid == ephDataSvid){

                    acqInfo.sv.GPS[j].tow = allGnssInfo["ephData"]["GPS"][i]["tocS"];
                    acqInfo.sv.GPS[j].now = allGnssInfo["ephData"]["GPS"][i]["week"];
                    acqInfo.sv.GPS[j].af0 = allGnssInfo["ephData"]["GPS"][i]["af0S"];
                    acqInfo.sv.GPS[j].af1 = allGnssInfo["ephData"]["GPS"][i]["af1SecPerSec"];
                    acqInfo.sv.GPS[j].af2 = allGnssInfo["ephData"]["GPS"][i]["af2SecPerSec2"];
                    acqInfo.sv.GPS[j].tgds = allGnssInfo["ephData"]["GPS"][i]["tgdS"];
                    
                    // kepler model
                    acqInfo.sv.GPS[j].keplerModel.cic = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["cic"];
                    acqInfo.sv.GPS[j].keplerModel.cis = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["cis"];
                    acqInfo.sv.GPS[j].keplerModel.crc = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["crc"];
                    acqInfo.sv.GPS[j].keplerModel.crs = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["cis"];
                    acqInfo.sv.GPS[j].keplerModel.cuc = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["cuc"];
                    acqInfo.sv.GPS[j].keplerModel.cus = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["cus"];
                    acqInfo.sv.GPS[j].keplerModel.deltaN = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["deltaN"];
                    acqInfo.sv.GPS[j].keplerModel.eccentricity = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["eccentricity"];
                    acqInfo.sv.GPS[j].keplerModel.i0 = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["i0"];
                    acqInfo.sv.GPS[j].keplerModel.iDot = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["iDot"];
                    acqInfo.sv.GPS[j].keplerModel.m0 = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["m0"];
                    acqInfo.sv.GPS[j].keplerModel.omega = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["omega"];
                    acqInfo.sv.GPS[j].keplerModel.omega0 = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["omega0"];
                    acqInfo.sv.GPS[j].keplerModel.omegaDot = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["omegaDot"];
                    acqInfo.sv.GPS[j].keplerModel.sqrtA = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["sqrtA"];
                    acqInfo.sv.GPS[j].keplerModel.toeS = allGnssInfo["ephData"]["GPS"][i]["keplerModel"]["toeS"];

                }
            }
        }

        int galEphData = allGnssInfo["ephData"]["Galileo"].size();
        for(int i = 0; i < galEphData; i++){
            for(int j = 0; j < acqInfo.sv.GALILEO.size(); j++){
                int ephDataSvid = allGnssInfo["ephData"]["Galileo"][i]["svid"];
                if(acqInfo.sv.GALILEO[j].svid == ephDataSvid){
                    
                    acqInfo.sv.GALILEO[j].tow = allGnssInfo["ephData"]["Galileo"][i]["tocS"];
                    acqInfo.sv.GALILEO[j].now = allGnssInfo["ephData"]["Galileo"][i]["week"];
                    acqInfo.sv.GALILEO[j].af0 = allGnssInfo["ephData"]["Galileo"][i]["af0S"];
                    acqInfo.sv.GALILEO[j].af1 = allGnssInfo["ephData"]["Galileo"][i]["af1SecPerSec"];
                    acqInfo.sv.GALILEO[j].af2 = allGnssInfo["ephData"]["Galileo"][i]["af2SecPerSec2"];
                    acqInfo.sv.GALILEO[j].tgds = allGnssInfo["ephData"]["Galileo"][i]["tgdS"];

                    // kepler model
                    acqInfo.sv.GALILEO[j].keplerModel.cic = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["cic"];
                    acqInfo.sv.GALILEO[j].keplerModel.cis = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["cis"];
                    acqInfo.sv.GALILEO[j].keplerModel.crc = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["crc"];
                    acqInfo.sv.GALILEO[j].keplerModel.crs = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["cis"];
                    acqInfo.sv.GALILEO[j].keplerModel.cuc = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["cuc"];
                    acqInfo.sv.GALILEO[j].keplerModel.cus = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["cus"];
                    acqInfo.sv.GALILEO[j].keplerModel.deltaN = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["deltaN"];
                    acqInfo.sv.GALILEO[j].keplerModel.eccentricity = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["eccentricity"];
                    acqInfo.sv.GALILEO[j].keplerModel.i0 = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["i0"];
                    acqInfo.sv.GALILEO[j].keplerModel.iDot = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["iDot"];
                    acqInfo.sv.GALILEO[j].keplerModel.m0 = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["m0"];
                    acqInfo.sv.GALILEO[j].keplerModel.omega = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["omega"];
                    acqInfo.sv.GALILEO[j].keplerModel.omega0 = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["omega0"];
                    acqInfo.sv.GALILEO[j].keplerModel.omegaDot = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["omegaDot"];
                    acqInfo.sv.GALILEO[j].keplerModel.sqrtA = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["sqrtA"];
                    acqInfo.sv.GALILEO[j].keplerModel.toeS = allGnssInfo["ephData"]["Galileo"][i]["keplerModel"]["toeS"];
                    
                }
            }
        }


        acqInfo.ionoProto[0] = allGnssInfo["ephData"]["Klobuchar"]["alpha_"][0];
        acqInfo.ionoProto[1] = allGnssInfo["ephData"]["Klobuchar"]["alpha_"][1];
        acqInfo.ionoProto[2] = allGnssInfo["ephData"]["Klobuchar"]["alpha_"][2];
        acqInfo.ionoProto[3] = allGnssInfo["ephData"]["Klobuchar"]["alpha_"][3];
        acqInfo.ionoProto[4] = allGnssInfo["ephData"]["Klobuchar"]["beta_"][0];
        acqInfo.ionoProto[5] = allGnssInfo["ephData"]["Klobuchar"]["beta_"][1];
        acqInfo.ionoProto[6] = allGnssInfo["ephData"]["Klobuchar"]["beta_"][2];
        acqInfo.ionoProto[7] = allGnssInfo["ephData"]["Klobuchar"]["beta_"][3];

        acqInfo.svs = gpsEphData + galEphData;

        vAcqInfo.push_back(acqInfo);

    }
    
}