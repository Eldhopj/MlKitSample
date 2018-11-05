package com.eldhopj.mlkitsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**Add classpath and dependencies */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void barCode(View view) {
        Intent intent = new Intent(getApplicationContext(),BarcodeScannerActivity.class);
        startActivity(intent);
    }

    public void imageLabel(View view) {
        Intent intent = new Intent(getApplicationContext(),ImageLabelingActivity.class);
        startActivity(intent);
    }
}
