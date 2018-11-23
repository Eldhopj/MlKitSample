package com.eldhopj.mlkitsample;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.eldhopj.mlkitsample.Utils.PermissionsHelper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Add classpath and dependencies
 */

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    PermissionsHelper permissionsHelper;
    @BindView(R.id.barcode)
    Button barcode;
    @BindView(R.id.imageLabel)
    Button imageLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    //Butterknife  onClicks
    @OnClick({R.id.barcode, R.id.imageLabel})
    public void onViewClicked(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // checking permissions if android version > M
            permissionsHelper = new PermissionsHelper(this);
            if (permissionsHelper.isPermissionsGranted()) { // checks whether the permission is granted or not
                switch (view.getId()) {
                    case R.id.barcode:
                        Intent barcodeIntent = new Intent(getApplicationContext(), BarcodeScannerActivity.class);
                        startActivity(barcodeIntent);
                        break;
                    case R.id.imageLabel:
                        Intent imageLabelIntent = new Intent(getApplicationContext(), ImageLabelingActivity.class);
                        startActivity(imageLabelIntent);
                        break;
                }
            }
        }

    }


    // -------------------------------------Easy permissions-------------------------------------//
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // This will forcefully ask permission again and again if permission denied, Cant able to use the activity unless the permission given
        // Do it in emergency situations only
        permissionsHelper.isPermissionsGranted();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        /** if some permissions denys the user will SEND TO SETTINGS of the app to manually grand the permission*/

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
// -------------------------------------Easy permissions-------------------------------------//

}
