package com.seamfix.qrcode;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.seamfix.seamcode.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        Button matchFinger = findViewById(R.id.id_match_finger);
        Button matchFace   =  findViewById(R.id.id_match_face);

        matchFace.setOnClickListener(v -> {

        });

        matchFinger.setOnClickListener(v -> {

        });
    }
}
