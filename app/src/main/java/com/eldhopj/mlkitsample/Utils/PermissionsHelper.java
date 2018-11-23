package com.eldhopj.mlkitsample.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class PermissionsHelper {

    Context context;

    public PermissionsHelper(Context context) {
        this.context = context;
    }

    private final int CAMERA_PERMISSION_CODE = 1;

    @AfterPermissionGranted(CAMERA_PERMISSION_CODE)
    public Boolean isPermissionsGranted() {
        String[] perms = {Manifest.permission.CAMERA}; //Array of permission
        if (EasyPermissions.hasPermissions(context, perms)) { //check permission is granted or not
            return true;
        } else {
            /**Rationalte dialog write here*/
            EasyPermissions.requestPermissions((Activity) context, "Please grand permission to access camera and storage",
                    CAMERA_PERMISSION_CODE, perms);
            return false;
        }
    }
}
