//
// Created by Arnau Ochoa Bañuelos on 17/03/2019.
//
#include <iomanip> // setprecision
#include <sstream> // stringstream
#include <string>
#include <jni.h>

#include "pvtEngine-lib.h"

double mean_vector(std::vector<double> theVector) {
    double theSum = 0;
    for (auto element : theVector)
        theSum = theSum + element;
    return (double) theSum / theVector.size();
}


extern "C" JNIEXPORT jstring JNICALL


Java_com_inari_team_presentation_ui_main_MainViewModel_obtainPosition(JNIEnv *env, jobject instance,
                                                                              jstring jsonData) {

    const char *gnssData = env->GetStringUTFChars(jsonData, nullptr);
    std::string lat;
    std::string lng;

    // Create aquisistion
    std::vector<Info> vAcqInfo;
    std::vector<Mode> modes;

    std::vector<double> modeLatitudes;
    std::vector<double> modeLongitudes;

    double latitude, longitude;

    try {
        // Read json
        json allGnssInfo;
        allGnssInfo = json::parse(gnssData);

        extract_info(allGnssInfo, vAcqInfo, modes);

        // Some initializations
        double **eph;
        double *iono;
        int ephN, ephM;

        // Number of iterations used to obtain the PVT solution
        int Nit = 5;

        // Preliminary guess for PVT solution
        double PVT0[4];

        // Speed of light (for error calculations)
        int c = 299792458;         // Speed of light (m/s)

        bool enabCorr = true;

        double PVT[4];    //  PVT solution
        double posllh[3];      //  Position in Latitude, Longitude and Height

        std::vector<double> latitudes;
        std::vector<double> longitudes;

        for (auto mode : modes) {

            latitudes.clear();
            longitudes.clear();

            for (auto acqInfo : vAcqInfo) {

                // eph Matrix 
                getEphMatrix(acqInfo, mode, eph, iono, ephN, ephM);

                //TODO: get preliminay guess, from obs header?
                PVT0[0] = acqInfo.RefLocation.XYZ.x;
                PVT0[1] = acqInfo.RefLocation.XYZ.y;
                PVT0[2] = acqInfo.RefLocation.XYZ.z;

                PVT_recls(acqInfo, mode, eph, ephN, ephM, iono, Nit, PVT0, enabCorr, PVT);

                xyz2llh(PVT, posllh);     // Getting position in Latitude, Longitude, Height format

                posllh[0] = rad2deg(posllh[0]);
                posllh[1] = rad2deg(posllh[1]);

                latitudes.push_back(posllh[0]);
                longitudes.push_back(posllh[1]);

            }

            latitude = mean_vector(latitudes);
            longitude = mean_vector(longitudes);

            // std::cout << latitude << ", " << longitude << std::endl;    

            modeLatitudes.push_back(latitude);
            modeLongitudes.push_back(longitude);

        }

    } catch (const std::exception &e) {
        lat = "null";
        lng = "null";
    }

    std::string position = "[";
    for (int i = 0; i < modes.size(); i++) {
        std::stringstream stream1;
        stream1 << std::fixed << std::setprecision(10) << modeLatitudes[i];
        std::string slatitude = stream1.str();

        std::stringstream stream2;
        stream2 << std::fixed << std::setprecision(10) << modeLongitudes[i];
        std::string slongitude = stream2.str();

        position.append("{ \"lat\":" + slatitude + ", \"lng\": " + slongitude + " }, ");
    }

    position.append("]");


    return env->NewStringUTF(position.c_str());
}

