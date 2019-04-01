#include "check_towKnown_state.h"

bool check_towKnown_state(int state) {
    bool valid = false;
    if (state & 16384) {
        valid = true;
    } else {
        valid = false;
    }
    return valid;
}