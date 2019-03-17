#ifndef EXTRACT_INFO_H
#define EXTRACT_INFO_H

#include <vector>
#include <math.h>
#include "nlohmann/json.hpp"

#include "nsgpst2gpst.h"
#include "pseudo_gen.h"
#include "lla2ecef.h"

#include <iostream>

using json = nlohmann::json;

struct KeplerModel{
    double cic;
    double cis;
    double crc;
    double crs;
    double cuc;
    double cus;
    double deltaN;
    double eccentricity;
    double i0;
    double iDot;
    double m0;
    double omega;
    double omega0;
    double omegaDot;
    double sqrtA;
    double toeS;
};

// Creates data structs
struct SVMember{
    double t_tx;
    double p;
    int pseudorangeRate;
    int svid;
    double CNO;
    double azimuth;
    double elevation;
    double carrierFreq;
    bool ok;

    double tow;
    double now;
    double af0;
    double af1;
    double af2;
    double tgds;

    KeplerModel keplerModel;
};

struct SVList{
    std::vector<int> SVlist_GPS;
    std::vector<int> SVlist_SBAS;
    std::vector<int> SVlist_GLONASS;
    std::vector<int> SVlist_QZSS;
    std::vector<int> SVlist_BEIDOU;
    std::vector<int> SVlist_Galileo;
    std::vector<int> SVlist_UNK;
};

// Location
struct LatLong{
    double latitute;
    double longitude;
    double altitude;
};

struct Ecef{
    double x;
    double y;
    double z;
};

struct RefLocation{
    LatLong LLH;
    Ecef XYZ;  // Finish
};

// Clock info
//

// Status

struct SV{
    std::vector<SVMember> GPS;
    std::vector<SVMember> SBAS;
    std::vector<SVMember> GLONASS;
    std::vector<SVMember> QZSS;
    std::vector<SVMember> BEIDU;
    std::vector<SVMember> GALILEO;
    std::vector<SVMember> UNK;
};

struct Constellations{
    bool GPS = false; 
    bool GALILEO = false;
    bool GLONASS = false;
};

struct Corrections{
    bool ionosphere = false;
    bool troposphere = false;
    bool mutipath = false;
    bool ppp = false;
    bool camera = false;
};

struct Flags{
    Constellations constellations;
    Corrections corrections;
};


struct Info{
    SVList svList;
    SV sv;
    RefLocation RefLocation;
    double nsGPSTime;
    double nsrxTime;
    double tow;
    double now;
    Flags flags;
    int svs;
    double ionoProto[8];
};

void extract_info(json gnssInfo, Info& acqInfo);

#endif