#include "check_time.h"

double check_time(double t){
    double halfWeek = 302400;
    double tt = t;

    if(t > halfWeek){
        tt = t-2*halfWeek;
    }
    if(t < -halfWeek){
        tt = t+2*halfWeek;
    }

    return tt;
}
