package com.fooddelivery.userservice.dto;

import com.fooddelivery.userservice.entity.AddressLabel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SavedAddressResponse {
    private Long id;
    private AddressLabel label;
    private String customLabel;
    private String receiverName;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String landmark;
    private String city;
    private String state;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private Boolean isDefault;
}