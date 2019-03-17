#include "sat_clock_error_correction.h"


double sat_clock_error_correction(double time,double *eph, int ephM){

    double af2 = eph[1];
    double af0 = eph[18];
    double af1 = eph[19];
    double ref_toc = eph[20];
    
    double dt = check_time(time - ref_toc);

    double corr = (af2 * dt + af1) * dt + af0;


    return corr;
}