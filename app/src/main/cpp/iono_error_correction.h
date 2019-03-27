#ifndef IONO_ERROR_CORRECTION_H
#define IONO_ERROR_CORRECTION_H

#include "math.h"

const double c = 299792458;

double iono_error_correction(double lat, double lon, double az, double el, double time_rx, double * ionoparams, double * sbas);

#endif