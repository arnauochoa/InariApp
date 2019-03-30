#include "rad2deg.h"

long double rad2deg(long double angleInRadians){
    return (180/M_PI) * angleInRadians;
}