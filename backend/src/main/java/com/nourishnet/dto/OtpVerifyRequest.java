package com.nourishnet.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpVerifyRequest {
    @NotBlank(message = "Handover OTP is required")
    private String otp;

    public OtpVerifyRequest() {}

    public OtpVerifyRequest(String otp) {
        this.otp = otp;
    }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
