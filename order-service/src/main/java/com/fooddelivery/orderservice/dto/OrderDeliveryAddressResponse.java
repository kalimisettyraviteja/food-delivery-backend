package com.fooddelivery.orderservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDeliveryAddressResponse {
    private Long addressId;
    private String label;
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