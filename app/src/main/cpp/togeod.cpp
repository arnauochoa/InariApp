#include "togeod.h"
#include <math.h>

void togeod(double a, double finv, double X, double Y, double Z, double &dphi, double &dlambda, double &h){

    h = 0;
    double tolsq = 1.0e-10;
    int maxit = 10;

    double rtd = 180/M_PI;

    double esq;
    if(finv < 1.0e-20){
        esq = 0;
    }else{
        esq = (2-1/finv)/finv;
    }
    double oneesq = 1-esq;

    double P = sqrt(pow(X,2) + pow(Y,2));

    dlambda = 0;
    if(P > 1.0e-20){
        dlambda = atan2(Y,X)*rtd;
    }else{
        dlambda = 0;
    }
    
    if(dlambda < 0){
        dlambda = dlambda + 360;
    }

    double r = sqrt(pow(P,2) + pow(Z,2));
    double sinphi;
    if(r > 1.0e-20){
        sinphi = Z/r;
    }else{
        sinphi = 0;
    }
    dphi = asin(sinphi);

    if(r<1.0e-20){
        h = 0;
        return;
    }

    h = r-a*(1-sinphi*sinphi/finv);     // The order of operation could have a conflict.

    double cosphi;
    double N_phi, dP, dZ;
    for(int i = 0; i< maxit; i++){
        sinphi = sin(dphi);
        cosphi = cos(dphi);

        N_phi = a/sqrt(1-esq*sinphi*sinphi);

        dP = P - (N_phi + h) * cosphi;
        dZ = Z - (N_phi*oneesq + h) * sinphi;

        h = h + (sinphi*dZ + cosphi*dP);
        dphi = dphi + (cosphi*dZ - sinphi*dP) / (N_phi + h);

        if((dP*dP + dZ*dZ) < tolsq){
            break;
        }
    }
    dphi = dphi*rtd;

}