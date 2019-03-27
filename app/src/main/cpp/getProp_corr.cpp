#include "getProp_corr.h"

#include <iostream>

void getProp_Corr(double X[3], double pos[3], double *iono, double tow, double &tropoCorr, double &ionoCorr){

    double PVT[3] = {pos[0], pos[1], pos[2]};

    double az, el, h;
    topocent(PVT,X,az,el,h);
    
    double llh[3];
    xyz2llh(PVT, llh);

    tropoCorr = tropo_error_correction(el,llh[2],1);
    
    ionoCorr = 0.00001;
    if(iono){
        ionoCorr = iono_error_correction(llh[0]*180/M_PI, llh[1]*180/M_PI, az, el, tow,iono, nullptr);
    }

}
