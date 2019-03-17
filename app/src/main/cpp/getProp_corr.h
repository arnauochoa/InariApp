#ifndef GETPROP_CORR_H
#define GETPROP_CORR_H

#include "topocent.h"
#include "xyz2llh.h"
#include "tropo_error_correction.h"

double getProp_Corr(double X[3], double pos[3], double *iono, double tow);

#endif