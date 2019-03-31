
bool check_towDecoded_state(int state){
    bool valid = false;
    if(state & 8){
        valid = true;
    }else{
        valid = false;
    }
    return valid;
}