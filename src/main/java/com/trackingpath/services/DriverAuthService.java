package com.trackingpath.services;

import com.trackingpath.dtos.*;

public interface DriverAuthService {
	DriverLoginResponse login(DriverLoginRequest request);
	DriverMeResponse me();
    void logout();
    void changePassword(ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
