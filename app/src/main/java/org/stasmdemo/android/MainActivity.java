package org.stasmdemo.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.objdetect.CascadeClassifier;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class MainActivity extends Activity implements CvCameraViewListener2 {
	private static final String TAG ="MainActivity";
	
	private Mat mRgba;
	private Mat mGray;
	public int[] points;
	
	private File 					mCascadeFile;
	private CameraBridgeViewBase   	mOpenCvCameraView;
    private CascadeClassifier 		mJavaDetector;
	
	private native int[] findFaceLandmarks(long matAddrGr);
	
	static {
	    if (!OpenCVLoader.initDebug()) {
	        // Handle initialization error
	    	Log.e(TAG, "OpenCVLoader initDebug() faild");
	    }
	    else {
			System.loadLibrary("stasm");
			Log.d(TAG, "JNI lib loaded successfully");                  
	    }
	}
	
	private void isRawDataExists(Context context){
		try{
			File internalDir = context.getDir("stasm", Context.MODE_PRIVATE);
			File frontalface_xml   = new File(internalDir, "haarcascade_frontalface_alt2.xml");
			//File frontalface_xml = new File(internalDir, "lbpcascade_frontalface.xml");
			File lefteye_xml       = new File(internalDir, "haarcascade_mcs_lefteye.xml");
			File righteye_xml      = new File(internalDir, "haarcascade_mcs_righteye.xml");
			File mounth_xml        = new File(internalDir, "haarcascade_mcs_mounth.xml");
			
			if(frontalface_xml.exists() && lefteye_xml.exists() && righteye_xml.exists() && mounth_xml.exists()){
				Log.d(TAG, "RawDataExists");
			}
			else{
				copyRawDataToInternal(context, R.raw.haarcascade_frontalface_alt2, frontalface_xml);
				copyRawDataToInternal(context, R.raw.haarcascade_mcs_lefteye, lefteye_xml);
				copyRawDataToInternal(context, R.raw.haarcascade_mcs_righteye, righteye_xml);
				copyRawDataToInternal(context, R.raw.haarcascade_mcs_mouth, mounth_xml);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void copyRawDataToInternal(Context context, int id, File file){
		Log.d(TAG, "copyRawDataToInternal: " + file.toString());
		try{
			InputStream is = context.getResources().openRawResource(id);
			FileOutputStream fos = new FileOutputStream(file);
			
			int data;
			byte[] buffer = new byte[4096];
			while((data = is.read(buffer)) != -1){
				fos.write(buffer, 0, data);
			}
			is.close();
			fos.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		Log.d(TAG, "copyRawDataToInternal done");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.opencv_surface_view);
		
		mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.opencv_java_surface_view);
	    mOpenCvCameraView.setCvCameraViewListener(this);
	    
	    // 2014.06.10 Elvis copy all raw data to internal
		isRawDataExists(MainActivity.this);
	}
	
	public void onResume() {
		Log.d(TAG, "called onResume");
		super.onResume();
		mOpenCvCameraView.enableView();
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "called onPause");
		super.onPause();
		if(mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}
	
	public void onDestroy(){
		Log.d(TAG, "called onDestroy");
		super.onDestroy();
		if(mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		Log.d(TAG, "called onCameraViewStarted");
	    
		mRgba = new Mat(width, height, CvType.CV_8UC3);
		mGray = new Mat(width, height, CvType.CV_8UC1);
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
    	Log.d(TAG, "called onCameraViewStopped");
    	
    	mRgba.release();
		mGray.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// TODO Auto-generated method stub
		Log.d(TAG, "called onCameraFrame");
		
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		
		// 2014.09.10 Elvis add to flip screen 
		Core.flip(mRgba, mRgba, 1);
		Core.flip(mGray, mGray, 1);
		
		//2014.09.10 Elvis add to find face feature points  
		points = findFaceLandmarks(mGray.getNativeObjAddr());
		
		//2014.09.10 Elvis add to display face feature points  
		if(points[0] > 0){
			Point pt;
			for(int i=0; i < points.length/2; ++i){
				pt = new Point(points[i*2], points[i*2+1]);
                Core.circle(mRgba, pt, 4, new Scalar(0, 255, 0));
			}
		}
		
		return mRgba;
	}
	
	/**
     * This class makes the ad request and loads the ad.
     */
    public static class AdFragment extends Fragment {

        private AdView mAdView;

        public AdFragment() {
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
        	super.onActivityCreated(bundle);
            
            // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
            // values/strings.xml.
            mAdView = (AdView) getView().findViewById(R.id.adView);

            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
            AdRequest adRequest = new AdRequest.Builder().build();

            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_ad, container, false);
        }

        /** Called when leaving the activity */
        @Override
        public void onPause() {
            if (mAdView != null) {
                mAdView.pause();
            }
            super.onPause();
        }

        /** Called when returning to the activity */
        @Override
        public void onResume() {
            super.onResume();
            if (mAdView != null) {
                mAdView.resume();
            }
        }

        /** Called before the activity is destroyed */
        @Override
        public void onDestroy() {
            if (mAdView != null) {
                mAdView.destroy();
            }
            super.onDestroy();
        }

    }

}
