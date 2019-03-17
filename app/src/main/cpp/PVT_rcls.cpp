#include "PVT_rcls.h"

#include <math.h>
#include <cmath>

#include <iostream>

double v_mean(std::vector<double> vector){
    double sum = 0;
    for(int i = 0; i < vector.size(); i++){
        sum = sum + vector[i];
    }
    return sum/vector.size();
}

double ** create_matrix(double ** Matrix, int n, int m){
    Matrix = new double *[n];
    for(int i = 0; i < n; i++){
        Matrix[i] = new double [m];
    }
    return Matrix;
}

Eigen::VectorXd convert_Vector_to_Eigen(std::vector<double> v1){
    Eigen::VectorXd v2(v1.size());
    for(int i = 0; i < v1.size(); i++){
        v2(i) = v1[i];
    }
    return v2;
}

void PVT_recls(Info acqInfo, double **eph, int ephN, int ephM, double *iono, int Nit, double PVT0[4], bool enabCorr,
                double * PVT ){

    std::vector<int> pr;
    std::vector<int> svn;
    for (int i = 0; i < acqInfo.sv.GPS.size(); i++){
        pr.push_back(acqInfo.sv.GPS[i].p);
        svn.push_back(acqInfo.sv.GPS[i].svid);
    }

    // Initialize parameters
    float tow = acqInfo.tow;
    const int c = 299792458;
    int nSat = pr.size();
    std::vector<double> tCorr(nSat);
    std::vector<double> pCorr(nSat);
    double **X;
    X = create_matrix(X,nSat,3);

    double corr = 0;
    double pr_c = 0;
    double d0 = 0;
    double ax = 0;
    double ay = 0;
    double az = 0;

    std::vector<double> p(nSat);

    PVT[0] = PVT0[0];
    PVT[1] = PVT0[1];
    PVT[2] = PVT0[2];
    PVT[3] = PVT0[3];


    double finalTCorr, finalPCorr;

    Eigen::MatrixXd A(nSat,4);
    Eigen::MatrixXd pinv,d;
    Eigen::VectorXd pEigen;
    
    
    // Iterative LS to compute the PVT solution
    for(int iter = 0; iter < Nit; iter++){

        for(int sat = 0; sat < nSat; sat++){
            corr = 0;
            
            if(iter == 0){
                getCtrl_corr(eph,ephN,ephM,svn[sat],tow,pr[sat],X[sat],tCorr[sat]);
            }

            // Get Propagation Corrections
            pCorr[sat] = getProp_Corr(X[sat],PVT0,iono,tow);  // Debug

            if(enabCorr){
                corr = pCorr[sat] + c * tCorr[sat];
            }else{
                corr = 0;
            }

            pr_c = pr[sat] + corr;

            if(!isnan(pr_c)){
                d0 = std::sqrt(pow(X[sat][0]-PVT[0],2) + pow(X[sat][1]-PVT[1],2) + pow(X[sat][2]-PVT[2],2) );
                
                p[sat] = pr_c - d0;
                ax = - (X[sat][0] - PVT[0])/d0;
                ay = - (X[sat][1] - PVT[1])/d0;
                az = - (X[sat][2] - PVT[2])/d0;

                A(sat,0) = ax;
                A(sat,1) = ay;
                A(sat,2) = az;
                A(sat,3) = 1;
            }
        }

        pEigen = convert_Vector_to_Eigen(p);

        pinv = A.completeOrthogonalDecomposition().pseudoInverse();
        d = pinv * pEigen;

        PVT[0] = PVT[0] + d(0);
        PVT[1] = PVT[1] + d(1);
        PVT[2] = PVT[2] + d(2);
        PVT[3] = d(3);

    }


    finalTCorr = v_mean(tCorr);
    finalPCorr = v_mean(pCorr);
 
}