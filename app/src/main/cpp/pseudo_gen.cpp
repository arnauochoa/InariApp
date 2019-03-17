#include "pseudo_gen.h"

long pseudo_gen(long t_tx, long t_rx, int c){

    return (t_rx - (t_tx/pow(10,9)))*c;

}