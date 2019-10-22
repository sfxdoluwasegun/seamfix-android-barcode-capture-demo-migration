package com.seamfix.qrcode.enrollment;

import java.util.LinkedHashMap;

public class DataSession {
    private static final DataSession ourInstance = new DataSession();
    private LinkedHashMap<Integer, String> textData = new LinkedHashMap<>();

    public static DataSession getInstance() {
        return ourInstance;
    }

    private DataSession() {
    }

    public LinkedHashMap<Integer, String> getTextData() {
        return textData;
    }

    public void setTextData(LinkedHashMap<Integer, String> textData) {
        this.textData = textData;
    }
}
