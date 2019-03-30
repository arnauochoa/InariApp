/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 *
 * lla2ecef.cpp
 *
 * Code generation for function 'lla2ecef'
 *
 */

/* Include files */
#include <cmath>
#include "lla2ecef.h"

/* Function Definitions */
void lla2ecef(double lat, double lon, double alt, double& x, double& y, double
              &z)
{
  double a = 6378137;
  double e = 8.1819190842622e-2;

  lat = lat / 180 * M_PI;
  lon = lon / 180 * M_PI;

  double e2 = e*e;
  double slat = std::sin(lat);
  double clat = std::cos(lat);

  double N = a / sqrt(1 -e2 * slat * slat);

  x = ((N+alt) * clat * cos(lon));
  y = (N+alt) * clat * sin(lon);
  z = (N * (1 - e2) + alt) * slat;

}

/* End of code generation (lla2ecef.cpp) */

