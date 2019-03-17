//
// Created by Arnau Ochoa Bañuelos on 17/03/2019.
//

#include <string>
#include <jni.h>

#include "pvtEngine-lib.h"


extern "C" JNIEXPORT jstring JNICALL

Java_com_inari_team_ui_position_PositionPresenter_obtainPosition(JNIEnv *env, jobject instance, jstring jsonData) {

    const char *gnssData = env->GetStringUTFChars(jsonData, nullptr);

    // Read json
    json gnssInfo = json::parse(gnssData);
    json firstGnssInfo = gnssInfo[2];

    // Create aquisistion
    Info acqInfo;
    extract_info(firstGnssInfo, acqInfo);

    // eph Matrix
    double ** eph;
    double * iono;
    int ephN, ephM;
    getEphMatrix(acqInfo,eph,iono, ephN, ephM);

    // Some initializations

    // Number of iterations used to obtain the PVT solution
    int Nit = 5;
    // Reference position (check RINEX file or website of the station)
    // int PVTr = acq_info.refLocation.XYZ;   // FIXME: add reference time
    // Preliminary guess for PVT solution
    double PVT0[4];
    //TODO: get preliminay guess, from obs header?
    PVT0[0] = acqInfo.RefLocation.XYZ.x;
    PVT0[1] = acqInfo.RefLocation.XYZ.y;
    PVT0[2] = acqInfo.RefLocation.XYZ.z;

    bool enabCorr = true;

    double PVT[4];    //  PVT solution
    double posllh[3];      //  Position in Latitude, Longitude and Height

    PVT_recls(acqInfo, eph, ephN, ephM, iono, Nit, PVT0, enabCorr, PVT );

    xyz2llh(PVT, posllh);     // Getting position in Latitude, Longitude, Height format

    posllh[0] = rad2deg(posllh[0]);
    posllh[1] = rad2deg(posllh[1]);

    // Return parameters
    std::string lat = std::to_string(posllh[0]);
    std::string lng = std::to_string(posllh[1]);

    std::string position = "{ \"lat\":" + lat + ", \"lng\": " + lng + " }";
    return env->NewStringUTF(position.c_str());
}

