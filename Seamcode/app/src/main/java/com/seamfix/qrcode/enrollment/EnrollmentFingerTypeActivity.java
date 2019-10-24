package com.seamfix.qrcode.enrollment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.seamfix.sdk.fingerprint.CaptureUtil;
import com.seamfix.sdk.fingerprint.FingerTypes;
import com.seamfix.sdk.fingerprint.spinner.SearchableSpinner;
import com.seamfix.seamcode.R;

import java.util.ArrayList;


public class EnrollmentFingerTypeActivity extends AppCompatActivity {

    private SearchableSpinner fingerTypeSpinner;
    private int fingerPosition;
    private String DEFAULT_SELECTION = "Select Finger";
    FingerTypes[] fingerTypes = FingerTypes.values();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_finger_type_layout);

        Button scanFinger = findViewById(R.id.button_finger_capture);
        fingerTypeSpinner = findViewById(R.id.finger_type);


        scanFinger.setOnClickListener(v -> {
            if (!CaptureUtil.hasUsbSupport(this)) {
                return;
            }
            if (!CaptureUtil.checkPermissions(this)) {
                return;
            }
            String fingerType = (String) fingerTypeSpinner.getSelectedItem();
            fingerPosition = fingerTypeSpinner.getSelectedItemPosition();
            if(fingerType.equalsIgnoreCase(DEFAULT_SELECTION)){
                Toast.makeText(this, "Invalid finger has been selected", Toast.LENGTH_SHORT).show();
                return;
            }
            FingerTypes type = fingerTypes[fingerPosition - 1];
            Intent intent = new Intent(EnrollmentFingerTypeActivity.this, EnrollmentFingerprintActivity.class);
            intent.putExtra("FINGER_TYPE",type.getValue());
            startActivity(intent);
        });

        populateSpinner();
    }

    public void populateSpinner() {
        ArrayList<String> array = FingerTypes.getFingerTypes(this);
        array.add(0, DEFAULT_SELECTION);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, array);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fingerTypeSpinner.setAdapter(dataAdapter);
        fingerTypeSpinner.setSelection(0);
    }

}
