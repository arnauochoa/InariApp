#ifndef PVT_RCLS_H
#define PVT_RCLS_H

#include "extract_info.h"
#include "getCtrl_corr.h"
#include "getProp_corr.h"
#include "Eigen/Dense"

#include <vector>

void PVT_recls(Info acqInfo, double **eph, int ephN, int ephM, double *iono, int Nit, double PVT0[4], bool enabCorr,
                double * PVT);

#endif