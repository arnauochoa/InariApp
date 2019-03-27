#ifndef GETPROP_CORR_H
#define GETPROP_CORR_H

#include "topocent.h"
#include "xyz2llh.h"
#include "tropo_error_correction.h"
#include "iono_error_correction.h"
#include <math.h>

void getProp_Corr(double X[3], double pos[3], double *iono, double tow, double &tropoCorr, double &ionoCorr);

#endif