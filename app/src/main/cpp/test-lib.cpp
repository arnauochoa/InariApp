//
// Created by Albert Becerra Hervás on 31/03/2019.
//

#include <jni.h>
#include "test-lib.h"

extern "C" JNIEXPORT jstring JNICALL

Java_com_inari_team_presentation_ui_main_MainViewModel_testcpp(JNIEnv *env, jobject instance,
                                                               jstring jsonData) {

    return env->NewStringUTF("test string");

}
