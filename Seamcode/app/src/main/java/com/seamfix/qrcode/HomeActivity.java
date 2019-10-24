package com.seamfix.qrcode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.seamfix.qrcode.enrollment.FormActivity;
import com.seamfix.qrcode.verification.VerificationCameraActivity;
import com.seamfix.seamcode.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        Button matchFinger = findViewById(R.id.id_match_finger);
        Button enroll = findViewById(R.id.id_enroll);
        Button verify = findViewById(R.id.id_verify);


        verify.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, VerificationCameraActivity.class);
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
