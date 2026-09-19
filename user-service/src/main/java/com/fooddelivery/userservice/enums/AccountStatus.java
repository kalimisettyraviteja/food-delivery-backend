package com.fooddelivery.userservice.enums;

public enum AccountStatus {
    ACTIVE,
    PENDING_APPROVAL,
    REJECTED,
    SUSPENDED,
    DEACTIVATED   // new — self-service, reversible via OTP
}