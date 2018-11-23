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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.selector.FocusModeSelectorsKt;
import io.fotoapparat.selector.LensPositionSelectorsKt;
import io.fotoapparat.selector.ResolutionSelectorsKt;
import io.fotoapparat.selector.SelectorsKt;
import io.fotoapparat.view.CameraView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ImageLabelingActivity extends AppCompatActivity {
    private static final String TAG = "ImageLabelingActivity";

    @BindView(R.id.camera_view)
    CameraView cameraView;

    Fotoapparat fotoapparat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_labeling);
        ButterKnife.bind(this);

        fotoapparat = Fotoapparat
                .with(this)
                .into(cameraView)           // view which will draw the camera preview
                .previewScaleType(ScaleType.CenterCrop)  // we want the preview to fill the view
                .photoResolution(ResolutionSelectorsKt.highestResolution())   // we want to have the smallest photo possible
                .lensPosition(LensPositionSelectorsKt.back())       // we want back camera
                .focusMode(SelectorsKt.firstAvailable(  // (optional) use the first focus mode which is supported by device
                        FocusModeSelectorsKt. continuousFocusPicture(),
                        FocusModeSelectorsKt.autoFocus(),        // in case if continuous focus is not available on device, auto focus will be used
                        FocusModeSelectorsKt.fixed()             // if even auto focus is not available - fixed focus mode will be used
                ))
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();

        fotoapparat.start();
    }

    @Override
    protected void onStop() {
        super.onStop();

        fotoapparat.stop();
    }

    public void captureImage(View view) {
        captureBitmap();
    }

    public void captureBitmap(){
        fotoapparat.start();
        PhotoResult photoResult = fotoapparat.takePicture();

// obtain Bitmap
        photoResult
                .toBitmap()
                .whenAvailable(new Function1<BitmapPhoto, Unit>() {
                    @Override
                    public Unit invoke(BitmapPhoto bitmapPhoto) {
                        Bitmap bitmap = bitmapPhoto.bitmap;
                        bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                        runDetector(bitmap);
                        Log.d(TAG, "invoke: bitmap" + bitmap.toString());
                        return null;
                    }
                });
    }

    private void runDetector(Bitmap bitmap) {
        final FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(bitmap);

        new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(boolean internet) {
                Log.d(TAG, "internet connection " + internet);
                if (internet) {
                    //If we have internet we will do it on cloud
                    processInCloud(visionImage);
                } else {
                    processInLocal(visionImage);
                }
            }
        });
    }

    private void processInCloud(FirebaseVisionImage visionImage) {
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
                        Log.d(TAG, "Cloud Label Detector Failure : " + e.getLocalizedMessage());
                    }
                });
    }

    private void processInLocal(FirebaseVisionImage visionImage) {
        FirebaseVisionLabelDetectorOptions detectorOptions = new FirebaseVisionLabelDetectorOptions.Builder()
                .setConfidenceThreshold(0.8f)
                .build();
        FirebaseVisionLabelDetector visionLabelDetector = FirebaseVision.getInstance().getVisionLabelDetector(detectorOptions);
        visionLabelDetector.detectInImage(visionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionLabel> firebaseVisionLabels) {
                        processLocalDataResult(firebaseVisionLabels);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ImageLabelingActivity.this, "Label Detector Failure " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processLocalDataResult(List<FirebaseVisionLabel> firebaseVisionLabels) {
        for (FirebaseVisionLabel label : firebaseVisionLabels) {
            Toast.makeText(this, "item : " + label.getLabel(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "processLocalDataResult: " + label.getLabel());
        }
    }

    private void processCloudDataResult(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {
        for (FirebaseVisionCloudLabel label : firebaseVisionCloudLabels) {
            Toast.makeText(this, "item : " + label.getLabel(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "processCloudDataResult: " + label.getLabel());
        }
    }
}
