package com.seamfix.qrcode.enrollment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.seamfix.qrcode.control.FieldPane;
import com.seamfix.qrcode.model.FormActivityViewModel;
import com.seamfix.seamcode.R;

public class FormActivity extends AppCompatActivity {

    private FormActivityViewModel formActivityViewModel;
    private LinearLayout formData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_form);
        formActivityViewModel = ViewModelProviders.of(this).get(FormActivityViewModel.class);
        formActivityViewModel.init();



        Button next = findViewById(R.id.form_next_button);
        formData    = findViewById(R.id.form_data);
        next.setOnClickListener(v -> {
            if(saveText()){
                Intent intent = new Intent(this, ImageEnrollmentCameraActivity.class);
                startActivity(intent);
            }else{
                Toast.makeText(this, "One or more fields are empty, all fields are required", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateText(FieldPane fieldPane){
        String text = fieldPane.getText().getText().toString();
        return !fieldPane.isRequired() || !TextUtils.isEmpty(text);
    }

    private boolean saveText(){
        for (int m = 0; m < formData.getChildCount(); m++) {
            if (formData.getChildAt(m) instanceof FieldPane) {
                FieldPane fieldPane = (FieldPane)formData.getChildAt(m);
                EditText editText = fieldPane.getText();
                Integer id = fieldPane.getId();
                String text = editText.getText().toString();

                if(validateText(fieldPane)) {
                    DataSession.getInstance().getTextData().put(id, text);
                }else {
                    return false;
                }
            }
        }
        return true;
    }

}
