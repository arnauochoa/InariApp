#include "check_Gal_state.h"

bool check_Gal_state(int state){
    bool valid = false;
    if(state & 2048){
        valid = true;
    }else{
        valid = false;
    }
    return valid;
}