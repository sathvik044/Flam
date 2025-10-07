cmake_minimum_required(VERSION 3.22.1)
project("native-lib")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Try to find OpenCV
if(DEFINED OPENCV_SDK_DIR AND EXISTS "${OPENCV_SDK_DIR}/sdk/native/jni")
    set(OpenCV_DIR "${OPENCV_SDK_DIR}/sdk/native/jni")
elseif(DEFINED ENV{OPENCV_SDK_DIR} AND EXISTS "$ENV{OPENCV_SDK_DIR}/sdk/native/jni")
    set(OpenCV_DIR "$ENV{OPENCV_SDK_DIR}/sdk/native/jni")
endif()
find_package(OpenCV QUIET)

if(OpenCV_FOUND)
    message(STATUS "OpenCV found: ${OpenCV_VERSION}")
    add_definitions(-DUSE_OPENCV)
    include_directories(${OpenCV_INCLUDE_DIRS})
else()
    message(WARNING "OpenCV not found. Building with fallback stub processing.")
endif()

add_library(native-lib SHARED
    native-lib.cpp
)

target_link_libraries(native-lib
    android
    log
    jnigraphics
)

if(OpenCV_FOUND)
    target_link_libraries(native-lib ${OpenCV_LIBS})
endif()
