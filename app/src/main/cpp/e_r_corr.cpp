/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 *
 * e_r_corr.cpp
 *
 * Code generation for function 'e_r_corr'
 *
 */

/* Include files */
#include <cmath>
#include "e_r_corr.h"

/* Function Definitions */
void e_r_corr(double traveltime, const double X_sat[3], double X_sat_rot[3])
{
  double omegatau;
  double dv0[9];
  int i0;
  static const signed char iv0[3] = { 0, 0, 1 };

  int i1;

  /*  E_R_CORR  Returns rotated satellite ECEF coordinates */
  /*            due to Earth rotation during signal travel time */
  /*   rad/sec */
  omegatau = 7.2921159E-5 * traveltime;
  dv0[0] = std::cos(omegatau);
  dv0[3] = std::sin(omegatau);
  dv0[6] = 0.0;
  dv0[1] = -std::sin(omegatau);
  dv0[4] = std::cos(omegatau);
  dv0[7] = 0.0;
  for (i0 = 0; i0 < 3; i0++) {
    dv0[2 + 3 * i0] = iv0[i0];
  }

  for (i0 = 0; i0 < 3; i0++) {
    X_sat_rot[i0] = 0.0;
    for (i1 = 0; i1 < 3; i1++) {
      X_sat_rot[i0] += dv0[i0 + 3 * i1] * X_sat[i1];
    }
  }

  /* %%%%%%% end e_r_corr.m %%%%%%%%%%%%%%%%%%%%% */
}

/* End of code generation (e_r_corr.cpp) */
