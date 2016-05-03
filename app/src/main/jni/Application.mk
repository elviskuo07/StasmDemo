NDK_TOOLCHAIN_VERSION := 4.9
APP_ABI := armeabi-v7a
APP_PLATFORM := android-9

# Instruct to use the static GNU STL implementation
APP_STL := gnustl_static

#  Enable C++11. However, pthread, rtti and exceptions aren�t enabled
APP_CPPFLAGS += -std=c++11 -frtti -fexceptions

#APP_CPPFLAGS += -frtti -fexceptions
#LOCAL_C_INCLUDES += ${NDKROOT}/sources/cxx-stl/gnu-libstdc++/4.9/include
# -Wno-long-long        disables the "long long warnings" for the OpenCV headers
# -Wno-unused-parameter allows virtual func defs that don't use all params
# -Wno-unknown-pragmas  allows OpenMP pragmas without complaint