package com.eldhopj.mlkitsample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.eldhopj.mlkitsample.Utils.InternetCheck;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

public class ImageLabelingActivity extends AppCompatActivity {
    private static final String TAG = "ImageLabelingActivity";

    CameraView cameraView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_labeling);
        cameraView = findViewById(R.id.camera);


        cameraKit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    public void captureImage(View view) {
        cameraView.start();
        cameraView.captureImage();
    }

    private void cameraKit(){
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {
                Toast.makeText(getApplicationContext(), cameraKitError.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                //TODO : Show progress bar
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
                cameraView.stop();

                runDetector(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }

    private void runDetector(Bitmap bitmap) {
        final FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(bitmap);

        new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(boolean internet) {
                Log.d(TAG, "internet connection "+internet);
                if (internet){
                    //If we have internet we will do it on cloud
                    processInCloud(visionImage);
                }else {
                    processInLocal(visionImage);
                }
            }
        });
    }

    private void processInCloud(FirebaseVisionImage visionImage){
        FirebaseVisionCloudDetectorOptions cloudDetectorOptions = new FirebaseVisionCloudDetectorOptions.Builder()
                .setMaxResults(1) // Get only one result which have maximum confidence level
                .build();

        FirebaseVisionCloudLabelDetector cloudLabelDetector = FirebaseVision.getInstance().getVisionCloudLabelDetector(cloudDetectorOptions);

        cloudLabelDetector.detectInImage(visionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {
                        processCloudDataResult(firebaseVisionCloudLabels);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Cloud Label Detector Failure : "+ e.getLocalizedMessage());
            }
        });
    }

    private void processInLocal(FirebaseVisionImage visionImage) {
        FirebaseVisionLabelDetectorOptions detectorOptions = new FirebaseVisionLabelDetectorOptions.Builder()
                .setConfidenceThreshold(0.8f)
                .build();
        FirebaseVisionLabelDetector visionLabelDetector =  FirebaseVision.getInstance().getVisionLabelDetector(detectorOptions);
        visionLabelDetector.detectInImage(visionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionLabel> firebaseVisionLabels) {
                      processLocalDataResult(firebaseVisionLabels);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ImageLabelingActivity.this, "Label Detector Failure "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processLocalDataResult(List<FirebaseVisionLabel> firebaseVisionLabels) {
        for (FirebaseVisionLabel label : firebaseVisionLabels){
            Toast.makeText(this, "item : "+label.getLabel(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "processLocalDataResult: "+ label.getLabel());
        }
    }

    private void processCloudDataResult(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {
        for (FirebaseVisionCloudLabel label : firebaseVisionCloudLabels){
            Toast.makeText(this, "item : " + label.getLabel(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "processCloudDataResult: "+label.getLabel());
        }
    }
}
