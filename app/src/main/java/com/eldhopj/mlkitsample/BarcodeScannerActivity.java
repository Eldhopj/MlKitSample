package com.eldhopj.mlkitsample;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.eldhopj.mlkitsample.Overlays.GraphicOverlay;
import com.eldhopj.mlkitsample.Overlays.ReactOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.Arrays;
import java.util.List;

public class BarcodeScannerActivity extends AppCompatActivity {

    private static final String TAG = "BarcodeScannerActivity";

    CameraView cameraView;
    GraphicOverlay graphicOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        cameraView = findViewById(R.id.camera);
        graphicOverlay = findViewById(R.id.graphicOverflow);

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

    public void captureBarcode(View view) {
        cameraView.start();
        cameraView.captureImage();
        graphicOverlay.clear();
    }


    private void cameraKit(){
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {
                Toast.makeText(BarcodeScannerActivity.this, cameraKitError.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                //TODO : Show progress bar
                Bitmap  bitmap = cameraKitImage.getBitmap();
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
        FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(bitmap);

        /**If concentrating on a single type use this code */
//        FirebaseVisionBarcodeDetectorOptions barcodeDetectorOptions = new FirebaseVisionBarcodeDetectorOptions.Builder()
//                .setBarcodeFormats(
//
//                        /**Different type of barcodes < a href https://medium.com/google-developer-experts/exploring-firebase-mlkit-on-android-barcode-scanning-part-three-cc6f5921a108/>
//                         * */
//                        FirebaseVisionBarcode.FORMAT_ALL_FORMATS,
//                        FirebaseVisionBarcode.FORMAT_AZTEC // You can try any other formats
//                ).build();
//        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(barcodeDetectorOptions);
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector();


        detector.detectInImage(visionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                        processResult(firebaseVisionBarcodes);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(BarcodeScannerActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processResult(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
        for (FirebaseVisionBarcode barcode : firebaseVisionBarcodes){

            //Draw Rect
            Rect rectBounds = barcode.getBoundingBox();
            ReactOverlay reactOverlay = new ReactOverlay(graphicOverlay,rectBounds);
            graphicOverlay.add(reactOverlay);

            int valueType = barcode.getValueType();
            switch (valueType){

                /**NOTE : a lot more value types are there , here its just a few samples*/
                case FirebaseVisionBarcode.TYPE_TEXT:
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            BarcodeScannerActivity.this);
                    builder.setTitle(barcode.getDisplayValue());
                    builder.setMessage(barcode.getRawValue());
                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                }
                break;
                case FirebaseVisionBarcode.TYPE_URL:
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(barcode.getRawValue())); // Opening URL on a browser
                    startActivity(intent);
                }
                break;
                case FirebaseVisionBarcode.TYPE_CONTACT_INFO:
                {
                    String info = "Name : "+barcode.getContactInfo().getName().getFormattedName()+
                            "\n"+"Address : "+ Arrays.toString(barcode.getContactInfo().getAddresses().get(0).getAddressLines()) +
                            "\n"+"Email : "+barcode.getContactInfo().getEmails().get(0).getAddress();


                    Log.d(TAG, "processResult: "+ info);

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            BarcodeScannerActivity.this);
                    builder.setTitle(barcode.getDisplayValue());
                    builder.setMessage(info);
                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                }
                break;
                case FirebaseVisionBarcode.TYPE_WIFI:
                    String ssid = barcode.getWifi().getSsid();
                    String password = barcode.getWifi().getPassword();
                    int type = barcode.getWifi().getEncryptionType();
                    break;
                default:
                    break;
            }
        }
        //
    }
}
