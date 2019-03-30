#include "sat_pos.h"

double calculate_remainder(double a, double b){
    double r = a - b * floor(a/b);
    return r;
}

// this function might be messing returning nan
void sat_pos(double t,double *eph, int ephM, double satp[3], double satv[3]){


    double GM = 3.986008e14;

    double Omegae_dot = 7.2921151467e-5;

    double M0       =   eph[2];
    double roota    =   eph[3];
    double deltan   =   eph[4];
    double ecc      =   eph[5];
    double omega    =   eph[6];
    double cuc      =   eph[7];
    double cus      =   eph[8];
    double crc      =   eph[9];
    double crs      =   eph[10];
    double i0       =   eph[11];
    double idot     =   eph[12];
    double cic      =   eph[13];
    double cis      =   eph[14];
    double Omega0   =   eph[15];
    double Omegadot =   eph[16];
    double toe      =   eph[17];


    //Procedure for coordinate calculation
    double A = roota*roota;
    double tk;
    tk = check_time(t-toe);
    double n0 = sqrt(GM / pow(A,3));
    double n = n0+deltan;
    double M = M0+n*tk;
    M = calculate_remainder(M + 2*M_PI, 2*M_PI);
    double E = M;
    double E_old, dE;
    for(int i = 0; i < 10; i++){
        E_old = E;
        E = M+ecc*sin(E);
        dE = calculate_remainder(E-E_old,2*M_PI);
        if(abs(dE) < 1e-12)
            break;
    }
    E = calculate_remainder(E+2*M_PI,2*M_PI);
    double v = atan2(sqrt(1.0-pow(ecc,2))*sin(E), cos(E)-ecc);
    
    double phi = v+omega;
    phi = calculate_remainder(phi,2*M_PI);
    double u = phi + cuc*cos(2*phi)+cus*sin(2*phi);
    double r = A*(1-ecc*cos(E)) + crc*cos(2*phi)+crs*sin(2*phi);
    double i = i0+idot*tk       + cic*cos(2*phi)+cis*sin(2*phi);
    double Omega = Omega0+(Omegadot-Omegae_dot)*tk-Omegae_dot*toe;
    Omega = calculate_remainder(Omega+2*M_PI,2*M_PI);
    double x1 = cos(u)*r;
    double y1 = sin(u)*r;

    satp[0] = x1*cos(Omega)-y1*cos(i)*sin(Omega);
    satp[1] = x1*sin(Omega)+y1*cos(i)*cos(Omega);
    satp[2] = y1*sin(i);


    // Compute satellite velocity
    double Ek          =   E;
    double fk          =   v;
    double phik        =   phi;
    double uk          =   u;
    double x1k         =   x1;
    double y1k         =   y1;
    double ik          =   i;
    double xk          =   satp[0];
    double yk          =   satp[1];
    
    double Omegak      =   Omega;
    double Ek_dot      =   n/(1-ecc*cos(Ek));
    double fk_dot      =   sin(Ek)*Ek_dot*(1+ecc*cos(fk)) / ((1-cos(Ek)*ecc)*sin(fk));
    double phik_dot    =   fk_dot;
    double uk_dot      =   phik_dot + 2*(cus*cos(2*phik)-cuc*sin(2*phik))*phik_dot;
    double rk_dot      =   A*ecc*sin(Ek)*Ek_dot + 2*(crs*cos(2*phik)-crc*sin(2*phik))*phik_dot;
    double ik_dot      =   idot + 2*(cis*cos(2*phik)-cic*sin(2*phik))*phik_dot;
    double Omegak_dot  =   Omegadot - Omegae_dot;
    double x1k_dot     =   rk_dot*cos(uk) - y1k*uk_dot;
    double y1k_dot     =   rk_dot*sin(uk) + x1k*uk_dot;

    double xk_dot      =   x1k_dot*cos(Omegak) - y1k_dot*cos(ik)*sin(Omegak) + y1k*sin(ik)*sin(Omegak)*ik_dot - yk*Omegak_dot;
    double yk_dot      =   x1k_dot*sin(Omegak) + y1k_dot*cos(ik)*cos(Omegak) - y1k*sin(ik)*ik_dot*cos(Omegak) + xk*Omegak_dot;
    double zk_dot      =   y1k_dot*sin(ik) + y1k*cos(ik)*ik_dot;

    satv[0] = xk_dot;
    satv[1] = yk_dot;
    satv[2] = zk_dot;

}