LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

# OPENCV
OPENCVROOT := /Users/elvis/Library/Android/opencv2411

# OpenCV
OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=on
OPENCV_LIB_TYPE:=SHARED

include ${OPENCVROOT}/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := stasm 
LOCAL_FILE_LIST = $(wildcard $(LOCAL_PATH)/stasm/*.cpp) \
				  $(wildcard $(LOCAL_PATH)/stasm/MOD_1/*.cpp) \
				  $(wildcard $(LOCAL_PATH)/*.cpp)

LOCAL_SRC_FILES := $(LOCAL_FILE_LIST:$(LOCAL_PATH)/%=%)
LOCAL_LDLIBS += -ldl -llog  
LOCAL_CFLAGS += -O3 #-fopenmp
LOCAL_LDFLAGS += -O3 #-fopenmp

include $(BUILD_SHARED_LIBRARY)

