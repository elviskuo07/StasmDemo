/*
 * demo.cpp
 *
 *  Created on: Oct 27, 2014
 *      Author: elvis
 */

#include <jni.h>
#include <android/log.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>

#include "stasm/stasm_lib.h"

using namespace std;
using namespace cv;

extern "C" {
JNIEXPORT jintArray JNICALL Java_org_stasmdemo_android_MainActivity_findFaceLandmarks(
		JNIEnv* env, jobject obj, jlong addrGray){

	static const char* path = "/data/data/org.stasmdemo.android/app_stasm/testface.jpg";
	Mat* mGray = (Mat*)addrGray;

	jintArray arrayOfLandmarks = env->NewIntArray(2*stasm_NLANDMARKS);
	jint* output = env->GetIntArrayElements(arrayOfLandmarks, NULL);

	// 2014.07.03 Elvis start calculate time
	double start = clock();

	// 2014.07.03 Elvis could not find image path
	if(!mGray->data){
		__android_log_print(ANDROID_LOG_ERROR, "Stasm", "Cannot load image", path);

		mGray->release();
		env->ReleaseIntArrayElements(arrayOfLandmarks, output, 0);
		return arrayOfLandmarks;
	}

	int foundface;
	float landmarks[2*stasm_NLANDMARKS] = {0};

	if(!stasm_search_single(&foundface, landmarks, (const char*)mGray->data, mGray->cols, mGray->rows, path,
			"/data/data/org.stasmdemo.android/app_stasm/")){
		__android_log_print(ANDROID_LOG_ERROR, "Stasm", "Error in stasm_search_single %s", stasm_lasterr());

		mGray->release();
		env->ReleaseIntArrayElements(arrayOfLandmarks, output, 0);
		return arrayOfLandmarks;
	}

	// 2014.07.03 Elvis can not find face
	if(!foundface){
		__android_log_print(ANDROID_LOG_ERROR, "Stasm", "face not found");

		mGray->release();
		env->ReleaseIntArrayElements(arrayOfLandmarks, output, 0);
		return arrayOfLandmarks;
	}
	else{// 2014.07.03 Elvis asm find face and detect drowsiness driving
		__android_log_print(ANDROID_LOG_DEBUG, "Stasm", "face founded");

		stasm_force_points_into_image(landmarks, mGray->cols, mGray->rows);
		double asmTime = double(clock() - start)/CLOCKS_PER_SEC;
		__android_log_print(ANDROID_LOG_DEBUG, "Stasm Time Cost", "Time:%.3fsec", asmTime);

		// 2014.06.23 Elvis store landmarks
		for(int i=0; i<stasm_NLANDMARKS; i++){
			output[2*i] = cvRound(landmarks[2*i]);
			output[2*i+1] = cvRound(landmarks[2*i+1]);
		}

		mGray->release();
		env->ReleaseIntArrayElements(arrayOfLandmarks, output, 0);
		return arrayOfLandmarks;
	}
}
}



