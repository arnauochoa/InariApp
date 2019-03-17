#ifndef MAIN_GNSS_H
#define MAIN_GNSS_H

#include <string>
#include <fstream>

#include "nlohmann/json.hpp"

#include "extract_info.h"
#include "getEphMatrix.h"
#include "PVT_rcls.h"
#include "rad2deg.h"

void main_GNSS(std::string jFile,double &latitude, double &longitude);

#endif