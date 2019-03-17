#include "find_eph.h"

#include <iostream>

std::vector<int> find_match(double **eph, int ephN, int ephM, int sv){
    int i = 0;
    std::vector<int> matches;
    
    for(int j = 0; j < ephM; j++){
        if(eph[i][j] == sv){
            matches.push_back(j);
        }
    }
    
    return matches;
}

int find_eph(double **eph, int ephN, int ephM, int sv,double time){

    int icol = 0;
    
    std::vector<int> isat;
    isat = find_match(eph,ephN,ephM,sv); // Calculate w/ a function
    
    int n = isat.size();

    if(n==0){
        return 0;
    }
    
    icol = isat[0];
    
    double dtmin = eph[21][icol]-time;
    double dt = 0;
    for(auto it : isat){
        dt = eph[21][it] - time;
        if(dt < 0){
            if(abs(dt) < abs(dtmin)){
                icol = it;
                dtmin = dt;
            }
        }
    }
    
    return icol;
}