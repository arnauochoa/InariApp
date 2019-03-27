#include "getCtrl_corr.h"


#include "Eigen/Dense"

double dotProduct(double vect_A[], double vect_B[]) 
{ 
    double product = 0; 
  
    for (int i = 0; i < 3; i++) 
        product = product + vect_A[i] * vect_B[i]; 
    return product; 
} 

void getCtrl_corr(double **eph, int ephN, int ephM, int svn,float TOW, double pr,double Y[3],double &tCorr){
    int c = 299792458;          //   Speed of light (m/s)


    double tx_RAW = TOW - pr/c;
    double X[3];

    int col = find_eph(eph, ephN, ephM, svn, tx_RAW);

    double * ephCol;
    ephCol = new double [ephN];
    for(int i = 0; i < ephN; i++)
    {
        ephCol[i] = eph[i][col];
    }

    tCorr = sat_clock_error_correction(tx_RAW, ephCol, ephM);

    double tgd = eph[21][col];
    tCorr = tCorr - tgd;

    double tx_GPS = tx_RAW - tCorr;
    
    tCorr = sat_clock_error_correction(tx_GPS, ephCol, ephM);

    tCorr = tCorr - tgd;

    double vel[3];
    sat_pos(tx_GPS, ephCol, ephM, X, vel);

    double trel;
    trel = (double)-2.0 * ( dotProduct(X, vel) / (double)pow(c,2) ); // dotProduct result differs from Matlab's

    tCorr = tCorr + trel;
    tx_GPS = tx_RAW - tCorr;

    double travelTime = TOW - tx_GPS;
    e_r_corr(travelTime, X, Y);

 
}