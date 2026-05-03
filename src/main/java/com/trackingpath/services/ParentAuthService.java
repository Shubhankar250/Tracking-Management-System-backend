package com.trackingpath.services;

import com.trackingpath.dtos.*;

public interface ParentAuthService {
    ParentLoginResponse login(ParentLoginRequest request);
    ParentMeResponse me();
    void logout();
    void changePassword(ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
