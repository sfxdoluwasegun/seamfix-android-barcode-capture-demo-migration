package com.seamfix.qrcode.model;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.HashMap;

public class FormActivityViewModel extends ViewModel {
    private MutableLiveData<HashMap<String, String>> textData;

    public void init(){
        if(textData != null){
            return;
        }

        /*
        Initializes view modes for the session of capture and the repository
        for data base
        ....................................................................
         */
        textData = new MutableLiveData<>();
    }
}
