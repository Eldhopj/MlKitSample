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

import java.util.Arrays;
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

public class BarcodeScannerActivity extends AppCompatActivity {

    private static final String TAG = "BarcodeScannerActivity";
    @BindView(R.id.graphicOverflow)
    GraphicOverlay graphicOverlay;
    @BindView(R.id.camera_view)
    CameraView cameraView;

    Fotoapparat fotoapparat;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
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

    public void captureBarcode(View view) {
        graphicOverlay.clear();
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
        Log.d(TAG, "runDetector: bitmap" + bitmap.toString());
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
                        Log.d(TAG, "onSuccess: barcodes" + firebaseVisionBarcodes);
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
        for (FirebaseVisionBarcode barcode : firebaseVisionBarcodes) {

            //Draw Rect
            Rect rectBounds = barcode.getBoundingBox();
            ReactOverlay reactOverlay = new ReactOverlay(graphicOverlay, rectBounds);
            graphicOverlay.add(reactOverlay);

            int valueType = barcode.getValueType();
            Log.d(TAG, "processResult: barcodeValue : "+valueType);
            switch (valueType) {

                /**NOTE : a lot more value types are there , here its just a few samples*/
                case FirebaseVisionBarcode.TYPE_TEXT: {
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
                case FirebaseVisionBarcode.TYPE_URL: {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(barcode.getRawValue())); // Opening URL on a browser
                    startActivity(intent);
                }
                break;
                case FirebaseVisionBarcode.TYPE_CONTACT_INFO: {
                    String info = "Name : " + barcode.getContactInfo().getName().getFormattedName() +
                            "\n" + "Address : " + Arrays.toString(barcode.getContactInfo().getAddresses().get(0).getAddressLines()) +
                            "\n" + "Email : " + barcode.getContactInfo().getEmails().get(0).getAddress();


                    Log.d(TAG, "processResult: " + info);

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
