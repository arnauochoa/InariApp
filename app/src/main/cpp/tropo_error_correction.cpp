/*
 * Academic License - for use in teaching, academic research, and meeting
 * course requirements at degree granting institutions only.  Not for
 * government, commercial, or other organizational use.
 *
 * tropo_error_correction.cpp
 *
 * Code generation for function 'tropo_error_correction'
 *
 */

/* Include files */
#include <cmath>
#include <math.h>
#include "tropo_error_correction.h"

/* Function Definitions */
double tropo_error_correction(double el, double h, int tropo_model)
{
  double corr;
  double unnamed_idx_0;
  int k;
  double T;
  double t;
  double y[9];
  static const short h_a[9] = { 0, 500, 1000, 1500, 2000, 2500, 3000, 4000, 5000
  };

  int idx;
  double d[9];
  signed char index_idx_0;
  signed char index_idx_1;
  double x;
  static const double B_a[9] = { 1.156, 1.079, 1.006, 0.938, 0.874, 0.813, 0.757,
    0.654, 0.563 };

  /*  SYNTAX: */
  /*    [corr] = tropo_error_correction(el, h); */
  /*  */
  /*  INPUT: */
  /*    el = satellite elevation */
  /*    h  = receiver ellipsoidal height */
  /*  */
  /*  OUTPUT: */
  /*    corr = tropospheric error correction */
  /*  */
  /*  DESCRIPTION: */
  /*    Computation of the pseudorange correction due to tropospheric refraction. */
  /*    Saastamoinen algorithm. */
  /* ---------------------------------------------------------------------------------------------- */
  /*                            goGPS v0.4.3 */
  /*  */
  /*  Copyright (C) 2009-2014 Mirko Reguzzoni, Eugenio Realini */
  /*  */
  /*  Portions of code contributed by Laboratorio di Geomatica, Polo Regionale di Como, */
  /*     Politecnico di Milano, Italy */
  /* ---------------------------------------------------------------------------------------------- */
  /* Saastamoinen model requires positive ellipsoidal height */
  unnamed_idx_0 = h;
  for (k = 0; k < 1; k++) {
    if (h < 0.0) {
      unnamed_idx_0 = 0.0;
    }
  }

  /*  If in ATM_model section in config file tropo is set to 0 it does not use tropospheric model */
  /*  [ATM_model] */
  /*  tropo=0 */
  if (tropo_model == 0) {
    corr = 0.0;
  } else if (unnamed_idx_0 < 5000.0) {
    /* conversion to radians */
    el = std::abs(el) * 3.1415926535897931 / 180.0;

    /* Standard atmosphere - Berg, 1948 (Bernese) */
    /* pressure [mbar] */
    /* temperature [K] */
    /* numerical constants for the algorithm [-] [m] [mbar] */
    T = 291.15 - 0.0065 * unnamed_idx_0;

    /* ---------------------------------------------------------------------- */
    /* linear interpolation */
    for (k = 0; k < 9; k++) {
      t = (double)h_a[k] - unnamed_idx_0;
      y[k] = std::abs(t);
      d[k] = t;
    }

    t = y[0];
    idx = 0;
    for (k = 0; k < 8; k++) {
      if (t > y[k + 1]) {
        t = y[k + 1];
        idx = k + 1;
      }
    }

    if (d[idx] > 0.0) {
      index_idx_0 = (signed char)idx;
      index_idx_1 = (signed char)(idx + 1);
    } else {
      index_idx_0 = (signed char)(idx + 1);
      index_idx_1 = (signed char)(idx + 2);
    }

    t = (unnamed_idx_0 - (double)h_a[index_idx_0 - 1]) / (double)
      (h_a[index_idx_1 - 1] - h_a[index_idx_0 - 1]);

    /* ---------------------------------------------------------------------- */
    /* tropospheric error */
    x = std::tan(el);
    corr = 0.002277 / std::sin(el) * (1013.25 * pow(1.0 - 2.26E-5 *
      unnamed_idx_0, 5.225) - ((1.0 - t) * B_a[index_idx_0 - 1] + t *
      B_a[index_idx_1 - 1]) / (x * x)) + 0.002277 / std::sin(el) * (1255.0 / T +
      0.05) * (0.01 * (50.0 * std::exp(-0.0006396 * unnamed_idx_0)) * std::exp((
      -37.2465 + 0.213166 * T) - 0.000256908 * (T * T)));
  } else {
    corr = 0.0;
  }

  return corr;
}


/* End of code generation (tropo_error_correction.cpp) */
