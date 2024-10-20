package com.example.logintest001;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.util.Xml;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
//import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Dictionary;

import static org.opencv.imgproc.Imgproc.rectangle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class FaceCameraActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2{

    private final int CAMERA_PERMISSION_CODE = 102;
    private static final String TAG = "AndroidOpenCv";
    private JavaCameraView mCameraView;
    private Mat mInputMat;
    private Mat mResultMat;

    private File mCascadeFile;

    private CascadeClassifier mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mCameraView = findViewById(R.id.activity_surface_view);

        Intent intent = getIntent();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        // checking if the permission has already been granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions granted");
            initializeCamera();
        } else {
            // prompt system dialog
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        InitCascade();
        mFaceRect = new Rect();
        frameForActivate = 0;
        savedImage = new LinkedList<Pair<Long, Mat>>();
        currentFrame = 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // camera can be turned on
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                initializeCamera();
            } else {
                // camera will stay off
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeCamera(){
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully");
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }

        mCameraView.setCameraPermissionGranted();
        mCameraView.setCameraIndex(1); // rear = 0, front = 1
        mCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mCameraView.setCvCameraViewListener(this);
        mCameraView.enableView();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mCameraView != null)
            mCameraView.enableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        frameForActivate = 0;
    }

    @Override
    public void onCameraViewStopped() {
    }


    private int frameForActivate;

    private int limitFrameForSavedImage = 60;
    private Queue<Pair<Long, Mat>> savedImage = new LinkedList<Pair<Long, Mat>>();


    private long currentFrame = 0;
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mInputMat = inputFrame.rgba();


        if(frameForActivate >= 5) {
            frameForActivate = 0;

            Mat result = detectFace();
            if(result != null) {
                savedImage.add(new Pair<Long, Mat>(currentFrame, result));
            }
        }
        else frameForActivate++;

        drawFaceRect();

        if(checkSavedImage())
            sentImages();

        currentFrame++;

        return mInputMat;
    }

    private void sentImages() {
        // Image Download to Internal Storage
        int count = savedImage.size();
        for(int i=0;i<count;i++){
            Pair<Long, Mat> element = savedImage.poll();
            Mat imageMat = element.second;
            Bitmap bitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imageMat, bitmap);

            FileOutputStream fos = null;
            try{
                fos = getBaseContext().openFileOutput("img-" + i + ".jpg", Context.MODE_PRIVATE);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // zipping
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(getBaseContext().getFilesDir(), "images.zip"));
            ZipOutputStream zos = new ZipOutputStream(fos);

            for(int i=0;i<6;i++){
                String fileName = "img-" + i + ".jpg";
                File file = new File(getBaseContext().getFilesDir(), fileName);
                FileInputStream fis = new FileInputStream(file);
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }

                zos.closeEntry();
                fis.close();
            }

            zos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int i=0;i<6;i++) {
            getBaseContext().deleteFile("img-" + i + ".jpg");
        }

        //sent
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://20.39.198.179/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        File zipFile = new File(getBaseContext().getFilesDir(), "images.zip");
        if (!zipFile.exists()){
            getBaseContext().deleteFile("images.zip");
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("application/zip"), zipFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", zipFile.getName(), requestFile);

        FaceAuthService apiService = retrofit.create(FaceAuthService.class);
        Call<FaceDataResource> call = apiService.AuthFace(body);

        call.enqueue(new Callback<FaceDataResource>() {
            @Override
            public void onResponse(Call<FaceDataResource> call, Response<FaceDataResource> response) {
                if (response.isSuccessful()) {
                    FaceDataResource result = response.body();


                    if (result.statusResult == 200) {

                    } else {
                        Toast.makeText(getBaseContext(), "정보를 불러올 수 없음", Toast.LENGTH_SHORT);
                    }
                }
            }
            @Override
            public void onFailure(Call<FaceDataResource> call, Throwable t) {
                Log.d("Fail", "연결이 원활하지 않습니다. :" + t.getMessage());
            }
        });

        getBaseContext().deleteFile("images.zip");
    }

    private boolean checkSavedImage() {
        while (!savedImage.isEmpty()) {
            Pair<Long, Mat> top = savedImage.peek();

            if (top.first + limitFrameForSavedImage < currentFrame)
                savedImage.poll();
            else break;
        }

        return savedImage.size() >= 6;
    }

    private void InitCascade() {
        InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
        mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");

        try {
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());

            if (mDetector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mDetector = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
        }
        catch (IOException e) {
            Log.e(TAG, "Fail IO");
        }

    }

    private Rect mFaceRect;
    private Mat detectFace() {
        if (mDetector.empty()) {
            mFaceRect.width = 0;
            mFaceRect.height = 0;
        }

        Mat resultImage = null;
        Mat gray = new Mat();
        Mat resizingGray = new Mat();
        Imgproc.cvtColor(mInputMat, gray, Imgproc.COLOR_BGRA2GRAY);
        //Imgproc.resize(gray, resizingGray, new Size(640, 360));

        MatOfRect faces = new MatOfRect();
        //mDetector.detectMultiScale(resizingGray, faces, 1.3, 3, 0, new Size(40, 40));
        mDetector.detectMultiScale(gray, faces, 1.3, 5);
        if (faces.total() == 1) {
            Size screenSize = mInputMat.size();
            Rect rc = faces.toList().get(0);
            int widthAdd = (int)(rc.width * 0.3f);
            int heightAdd = (int)(rc.height * 0.7f);

            rc.x = Math.max(rc.x - (int)(widthAdd / 2), 0);
            rc.y = Math.max(rc.y - (int)(heightAdd / 2), 0);
            rc.width = Math.min(widthAdd + rc.width, (int)screenSize.width - rc.x);
            rc.height = Math.min(heightAdd + rc.y, (int)screenSize.height - rc.y);

            //rc.x *= 3;
            //rc.y *= 3;
            //rc.width *= 3;
            //rc.height *= 3;


            mFaceRect = rc;

            resultImage = new Mat(mInputMat, rc);

        }
        else {
            mFaceRect.width = 0;
            mFaceRect.height = 0;
        }

        return resultImage;
    }

    private void drawFaceRect() {
        if(mFaceRect.empty()) return;
        rectangle(mInputMat, mFaceRect, new Scalar(255, 50, 100), 3);
    }
}
