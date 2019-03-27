#include "iono_error_correction.h"

double my_mod(double a,double m){
    double b = a - m*floor(a/m);
    return b;
}

double klobuchar_model(double lat, double lon, double az, double el, double time_rx, double * ionoparams){
    double delay;
    double a0 = ionoparams[0];
    double a1 = ionoparams[1];
    double a2 = ionoparams[2];
    double a3 = ionoparams[3];
    double b0 = ionoparams[4];
    double b1 = ionoparams[5];
    double b2 = ionoparams[6];
    double b3 = ionoparams[7];

    double c = 299792458;

    el = abs(el);

    lat = lat/180;
    lon = lon/180;
    az = az/180;
    el = el/180;

    double f = 1 + 16*pow((0.53 - el),3);

    double psi = (0.0137 / (el+0.11) - 0.022);

    double phi = lat + psi * cos(az*M_PI);
    if(phi > 0.416) phi = 0.416;
    if(phi < -0.416) phi = -0.416;

    double lambda = lon + ( (psi*sin(az*M_PI)) / cos(phi*M_PI) );

    double ro = phi + 0.064*cos((lambda - 1.617)*M_PI);

    double t = lambda*43200 + time_rx;
    t = my_mod(t, 86400);

    double a = a0 + a1*ro + a2*pow(ro,2) + a3*pow(ro,3);
    if(a < 0) a = 0;

    double p = b0 + b1*ro + b2*pow(ro,2) + b3*pow(ro,3);

    double x = ( 2 * M_PI * (t-50400) ) / p;

    if(abs(x) < 1.57)
        delay = c * f * ( 5e-9 + a * ( 1 - pow(x,2)/2 + pow(x,4)/24 ));

    if(abs(x) >= 1.57)
        delay = c * f * 5e-9;

    return delay;
}


double iono_error_correction(double lat, double lon, double az, double el, double time_rx, double * ionoparams, double * sbas){

    double iono_model = 0;
    double corr=0;

    corr = klobuchar_model(lat, lon, az, el, time_rx, ionoparams);

    return corr;

}