package com.seamfix.qrcode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.seamfix.qrcode.enrollment.FormActivity;
import com.seamfix.qrcode.verification.ImageVerificationActivity;
import com.seamfix.qrcode.verification.VerificationCameraActivity;
import com.seamfix.seamcode.R;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;

public class HomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        if(OpenCVLoader.initDebug(false)){
            //Toast.makeText(this, "openCv successfully loaded", Toast.LENGTH_SHORT).show();
        }else{
            //Toast.makeText(this, "openCv cannot be loaded", Toast.LENGTH_SHORT).show();
        }

        Button matchFinger = findViewById(R.id.id_match_finger);
        Button enroll = findViewById(R.id.id_enroll);
        Button verify = findViewById(R.id.id_verify);


        verify.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ImageVerificationActivity.class);
            intent.putExtra("CONTEXT", Operation.VERIFICATION_MATCH_PRINT.name());
            startActivity(intent);
        });

        matchFinger.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        enroll.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FormActivity.class);
            startActivity(intent);
        });
    }
}
