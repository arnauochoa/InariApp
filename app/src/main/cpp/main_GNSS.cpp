#include "main_GNSS.h"


using json = nlohmann::json;




void main_GNSS(std::string jsonContent,double &latitude, double &longitude){

     // Read json
    json gnssInfo = json::parse(jsonContent);
    json firstGnssInfo = gnssInfo[1];
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
    int Nit = 1;                   
    // Reference position (check RINEX file or website of the station)
    // int PVTr = acq_info.refLocation.XYZ;   // FIXME: add reference time
    // Preliminary guess for PVT solution
    double PVT0[4];
    //TODO: get preliminay guess, from obs header?
    PVT0[0] = acqInfo.RefLocation.XYZ.x;
    PVT0[1] = acqInfo.RefLocation.XYZ.y;
    PVT0[2] = acqInfo.RefLocation.XYZ.z;


    // Speed of light (for error calculations)
    int c = 299792458;         // Speed of light (m/s)

    bool enabCorr = true;

    double PVT[4];    //  PVT solution
    double posllh[3];      //  Position in Latitude, Longitude and Height

    PVT_recls(acqInfo, eph, ephN, ephM, iono, Nit, PVT0, enabCorr, PVT );

    xyz2llh(PVT, posllh);     // Getting position in Latitude, Longitude, Height format

    posllh[0] = rad2deg(posllh[0]);
    posllh[1] = rad2deg(posllh[1]);


    // Return parameters

    latitude    = posllh[0];
    longitude   = posllh[1];

    
}