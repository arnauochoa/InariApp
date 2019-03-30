#include "topocent.h"
#include <math.h>

void topocent(double X[], double dx[3], double &Az, double &el, double &D){
    double dtr = M_PI/180;
    double phi, lambda, h;
    togeod(6378137,298.257223563, X[0], X[1], X[2], phi, lambda,h);

    double cl = cos(lambda*dtr);
    double sl = sin(lambda*dtr);
    double cb = cos(phi*dtr); 
    double sb = sin(phi*dtr);

    Eigen::Matrix3d F;
    F << -sl, -sb*cl, cb*cl,
        cl, -sb*sl, cb*sl,
        0, cb, sb;

    Eigen::Vector3d eigenDx;
    eigenDx << dx[0], dx[1], dx[2]; 

    Eigen::VectorXd localVector = F.transpose()*eigenDx;

    double E = localVector(0);
    double N = localVector(1);
    double U = localVector(2);

    double hor_dis = sqrt(pow(E,2) + pow(N,2));

    if(hor_dis<1.0e-20){
        Az = 0;
        el = 90;
    }else{
        Az = atan2(E,N)/dtr;
        el = atan2(U,hor_dis)/dtr;
    }
    if(Az < 0)
        Az = Az + 360;

    D = sqrt( pow(dx[0],2) + pow(dx[1],2) + pow(dx[2],2) );
}