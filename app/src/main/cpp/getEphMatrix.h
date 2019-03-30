#ifndef GETEPHMATRIX_H
#define GETEPHMATRIX_H

#include "extract_info.h"
#include <vector>

void getEphMatrix(Info acqInfo, Mode mode, double **& eph, double *& iono, int &ephN, int &ephM);

#endif