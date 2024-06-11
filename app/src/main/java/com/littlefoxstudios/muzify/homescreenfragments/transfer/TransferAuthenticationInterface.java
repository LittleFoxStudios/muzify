package com.littlefoxstudios.muzify.homescreenfragments.transfer;

public interface TransferAuthenticationInterface {
    int CANCEL_REQUEST_CLICKED = 0;
    int REQUEST_ACCESS_CLICKED = 1;

    void handleMusicServiceEnableCheck(boolean successResponse, boolean enabled, String accountEmail, String connectedAccountEmail);
    void handleServiceAccessRequestRaiseCheck(boolean successResponse, boolean requestExists, String requestRaiseDate, String developerMessage);
    void handleAddServiceAccessRequest(boolean successResponse);
    void handleButtonOperation(int mode);
    void switchToNextFragment(String accountEmailID, String userName, String profilePictureURL);
}
