package com.example.obd_kkb01;

public class Self_Diagnosis_data {

    String error_code, explanation;

    public Self_Diagnosis_data(String error_code, String explanation) {
        this.error_code = error_code;
        this.explanation = explanation;
    }

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
