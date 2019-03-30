#ifndef GETCTRL_CORR_H
#define GETCTRL_CORR_H

#include "find_eph.h"
#include "sat_clock_error_correction.h"
#include "sat_pos.h"
#include "e_r_corr.h"


void getCtrl_corr(double **eph, int ephN, int ephM, int svn,float TOW, double pr,double X[3],double &tCorr);

#endif
