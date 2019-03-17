/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 *
 * xyz2llh.cpp
 *
 * Code generation for function 'xyz2llh'
 *
 */

/* Include files */
#include <cmath>
#include <math.h>
#include "xyz2llh.h"

#include <iostream>

/* Function Definitions */
void xyz2llh(const double* xyz, double llh[3])
{
  long double z2;
  long double r;
  long double r2;
  long double F;
  long double G;
  long double c;
  long double s;

  /*  Convert from ECEF cartesian coordinates to latitude, longitude and height. */
  /*  Based on the WGS-84 reference frame */
  /*  */
  /*    llh is a (1x3) array */
  /* 	llh(1) = latitude in radians, ranging from [-pi/2,pi/2] positive N */
  /* 	llh(2) = longitude in radians, ranging from [-pi,pi] positive E */
  /* 	llh(3) = height above ellipsoid in meters */
  /*  */
  /*    xyz is a (1x3) array */
  /* 	xyz(1) = ECEF x-coordinate in meters */
  /* 	xyz(2) = ECEF y-coordinate in meters */
  /* 	xyz(3) = ECEF z-coordinate in meters */
  /* 	Reference: Understanding GPS: Principles and Applications, */
  /* 	           Elliott D. Kaplan, Editor, Artech House Publishers, */
  /* 	           Boston, 1996. */
  z2 = xyz[2] * xyz[2];

  /*  earth radius in meters */
  /*  earth semiminor in meters	 */
  r = std::sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1]);
  r2 = r * r;
  F = 2.182048199140701E+15 * z2;
  G = (r2 + 0.99330561999573908 * z2) - 1.8230912622998655E+9;
  c = 4.4814723641448496E-5 * F * r2 / (G * G * G);
  s = pow((1.0 + c) + std::sqrt(c * c + 2.0 * c), 0.33333333333333331);
  c = (s + 1.0 / s) + 1.0;
  c = F / (3.0 * (c * c) * G * G);
  s = std::sqrt(1.0 + 8.9629447282896991E-5 * c);
  c = r - 0.0066943800042609247 * (-(c * 0.0066943800042609247 * r) / (1.0 + s)
    + std::sqrt((2.03403157953845E+13 * (1.0 + 1.0 / s) - c *
                 0.99330561999573908 * z2 / (s * (1.0 + s))) - c * r2 / 2.0));
  c *= c;
  s = std::sqrt(c + 0.99330561999573908 * z2);
  llh[2] = std::sqrt(c + z2) * (1.0 - 4.0408299984087055E+13 / (6.378137E+6 * s));
  llh[0] = std::atan((xyz[2] + 0.0067394967565870042 * (4.0408299984087055E+13 *
    xyz[2] / (6.378137E+6 * s))) / r);
  c = std::atan(xyz[1] / xyz[0]);
  if (xyz[0] >= 0.0) {
    llh[1] = c;
  } else if ((xyz[0] < 0.0) && (xyz[1] >= 0.0)) {
    llh[1] = 3.1415926535897931 + c;
  } else {
    llh[1] = c - 3.1415926535897931;
  }

}

/* End of code generation (xyz2llh.cpp) */
