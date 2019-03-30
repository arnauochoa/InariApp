#include "nsgpst2gpst.h"


void nsgpst2gpst(double time, double& tow,double& now){
    int year_seconds    = 365*24*60*60;
    int week_seconds    = 7*24*60*60;
    int day_seconds     = 24*60*60;
    
    time = time/pow(10,9);
    
    // Now
    now = floor(time/week_seconds);
    
    //Tow
    tow = round(fmod(time, week_seconds)); // CHECK IF OPERATION IS CORRECT 
}