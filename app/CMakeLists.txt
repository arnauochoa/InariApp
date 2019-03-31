# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

file(GLOB CPP_FILES "./src/main/cpp/*.cpp")

add_library( # Specifies the name of the library.
        pvtEngine-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${CPP_FILES}
        src/main/cpp/pvtEngine-lib.cpp)

add_library( # Specifies the name of the library.
        test-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${CPP_FILES}
        src/main/cpp/test-lib.cpp)

