package com.seamfix.qrcode.verification;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.seamfix.qrcode.EnrollmentData;
import com.seamfix.qrcode.FingerQrCode;
import com.seamfix.seamcode.R;


public class FailedVerificationDetailsActivity extends AppCompatActivity {

    Button next;
    TextView firstName, lastName, regNumber, score;
    ImageView candidateImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_failed_verification_details);

        firstName      = findViewById(R.id.id_field_first_name);
        lastName       = findViewById(R.id.id_field_last_name);
        regNumber      = findViewById(R.id.id_field_registration_number);
        score          = findViewById(R.id.id_field_score);
        next           = findViewById(R.id.proceed_to_verification);
        candidateImage = findViewById(R.id.id_candidate_image);

        String templateData = getIntent().getStringExtra("value");
        EnrollmentData enrollmentData = FingerQrCode.decode(templateData);
        if(enrollmentData != null && enrollmentData.getT() != null){
            String []textData = enrollmentData.getT().split(":");
            if(textData.length == 4){
                firstName.setText(textData[0]);
                lastName.setText(textData[1]);
                regNumber.setText(textData[2]);
                score.setText(textData[3]);
                candidateImage.setImageBitmap(Session.getInstance().getCroppedBitmap());
            }else{
                finish();
            }
        }


        next.setOnClickListener(v -> {
            Bundle bundle = getIntent().getExtras();
            Intent intent = new Intent(FailedVerificationDetailsActivity.this, VerificationFingerprintActivity.class);
            assert bundle != null;
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        });
    }

}
