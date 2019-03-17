#include "getProp_corr.h"


double getProp_Corr(double X[3], double pos[3], double *iono, double tow){

    double PVT[3] = {pos[0], pos[1], pos[2]};


    double az, el, h;
    topocent(PVT,X,az,el,h);
    
    double llh[3];
    xyz2llh(PVT, llh);

    double tropoCorr = tropo_error_correction(el,llh[2],1);
    
    double ionoCorr = 0.00001;
     // Implement Iono Correction

    double T = tropoCorr + ionoCorr;

    return T;
    
   return 0;

}
