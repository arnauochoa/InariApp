#include "getEphMatrix.h"


void getEphMatrix(Info acqInfo, double **& eph, double *& iono, int &ephN, int &ephM){

    if(acqInfo.flags.constellations.GPS){
        std::vector<SVMember> GPS = acqInfo.sv.GPS;
        eph = new double *[22];
        for(int i = 0; i < 22; i++){
            eph[i] = new double [GPS.size()];
        }
        ephN = 22;
        ephM = GPS.size();

        for(int i = 0; i < GPS.size(); i++){

            eph[0][i] = GPS[i].svid;
            eph[1][i] = GPS[i].af2;
            eph[2][i] = GPS[i].keplerModel.m0;
            eph[3][i] = GPS[i].keplerModel.sqrtA;
            eph[4][i] = GPS[i].keplerModel.deltaN;
            eph[5][i] = GPS[i].keplerModel.eccentricity;
            eph[6][i] = GPS[i].keplerModel.omega;
            eph[7][i] = GPS[i].keplerModel.cuc;
            eph[8][i] = GPS[i].keplerModel.cus;
            eph[9][i] = GPS[i].keplerModel.crc;
            eph[10][i] = GPS[i].keplerModel.crs;
            eph[11][i] = GPS[i].keplerModel.i0;
            eph[12][i] = GPS[i].keplerModel.iDot;
            eph[13][i] = GPS[i].keplerModel.cic;
            eph[14][i] = GPS[i].keplerModel.cis;
            eph[15][i] = GPS[i].keplerModel.omega0;
            eph[16][i] = GPS[i].keplerModel.omegaDot;
            eph[17][i] = GPS[i].keplerModel.toeS;
            eph[18][i] = GPS[i].af0;
            eph[19][i] = GPS[i].af1;
            eph[20][i] = GPS[i].keplerModel.toeS;
            eph[21][i] = GPS[i].tgds;

        }
    }

    
    if(acqInfo.flags.constellations.GALILEO){
        std::vector<SVMember> Galileo = acqInfo.sv.GALILEO;
        eph = new double *[22];
        for(int i = 0; i < 22; i++){
            eph[i] = new double [Galileo.size()];
        }
        ephN = 22;
        ephM = Galileo.size();

        for(int i = 0; i < Galileo.size(); i++){

            eph[0][i] = Galileo[i].svid;
            eph[1][i] = Galileo[i].af2;
            eph[2][i] = Galileo[i].keplerModel.m0;
            eph[3][i] = Galileo[i].keplerModel.sqrtA;
            eph[4][i] = Galileo[i].keplerModel.deltaN;
            eph[5][i] = Galileo[i].keplerModel.eccentricity;
            eph[6][i] = Galileo[i].keplerModel.omega0;
            eph[7][i] = Galileo[i].keplerModel.cuc;
            eph[8][i] = Galileo[i].keplerModel.cus;
            eph[9][i] = Galileo[i].keplerModel.crc;
            eph[10][i] = Galileo[i].keplerModel.crs;
            eph[11][i] = Galileo[i].keplerModel.i0;
            eph[12][i] = Galileo[i].keplerModel.iDot;
            eph[13][i] = Galileo[i].keplerModel.cic;
            eph[14][i] = Galileo[i].keplerModel.cis;
            eph[15][i] = Galileo[i].keplerModel.omega0;
            eph[16][i] = Galileo[i].keplerModel.omegaDot;
            eph[17][i] = Galileo[i].keplerModel.toeS;
            eph[18][i] = Galileo[i].af0;
            eph[19][i] = Galileo[i].af1;
            eph[20][i] = Galileo[i].keplerModel.toeS;
            eph[21][i] = Galileo[i].tgds;
        }
    }
    
    iono = new double [8];
    for(int i = 0; i < 8; i++){
        iono[i] = 0;
    }

}