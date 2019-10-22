package com.seamfix.qrcode.control;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.seamfix.seamcode.R;

/**
 * Created by emmanuel on 18/10/2019.
 *
 */

public class FieldPane extends LinearLayout {

    private TextView label;
    private EditText text;
    private Context context;
    private boolean required;

    public FieldPane(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.form_field_layout, this);
        label = view.findViewById(R.id.label);
        text  = view.findViewById(R.id.text);
        this.context = context;
        setUpAttributes(attrs);
    }

    private void setUpAttributes(AttributeSet attrs){
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FormField,0,0);
        try{
            int attLabel     = typedArray.getResourceId(R.styleable.FormField_label, R.string.error);
            int attText      = typedArray.getResourceId(R.styleable.FormField_text, R.string.empty);
            int attInputType = typedArray.getInt(R.styleable.FormField_inputType, InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            int attInput     = typedArray.getInt(R.styleable.FormField_length, 500);
            required         = typedArray.getBoolean(R.styleable.FormField_required, false);


            label.setText(attLabel);
            text.setText(attText);
            text.setHint(String.format("Enter %s", label.getText()));
            text.setInputType(attInputType);
            text.setFilters(new InputFilter[] {new InputFilter.LengthFilter(attInput)});
        }finally{
            typedArray.recycle();
        }
    }

    public TextView getLabel() {
        return label;
    }

    public void setLabel(TextView label) {
        this.label = label;
    }

    public EditText getText() {
        return text;
    }

    public void setText(EditText text) {
        this.text = text;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
