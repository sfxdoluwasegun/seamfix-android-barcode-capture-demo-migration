package com.seamfix.qrcode;

public class VerificationSession {
    private static final VerificationSession ourInstance = new VerificationSession();
    private String templateData;

    public static VerificationSession getInstance() {
        return ourInstance;
    }

    private VerificationSession() {
    }

    public String getTemplateData() {
        return templateData;
    }

    public void setTemplateData(String templateData) {
        this.templateData = templateData;
    }
}
