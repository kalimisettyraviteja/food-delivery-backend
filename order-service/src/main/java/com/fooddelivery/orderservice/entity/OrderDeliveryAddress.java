package com.fooddelivery.orderservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDeliveryAddress {

    @Column(name = "delivery_address_id")
    private Long addressId;

    @Column(name = "delivery_label")
    private String label;

    @Column(name = "delivery_custom_label")
    private String customLabel;

    @Column(name = "delivery_receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "delivery_phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "delivery_address_line1", nullable = false)
    private String addressLine1;

    @Column(name = "delivery_address_line2")
    private String addressLine2;

    @Column(name = "delivery_landmark")
    private String landmark;

    @Column(name = "delivery_city", nullable = false)
    private String city;

    @Column(name = "delivery_state", nullable = false)
    private String state;

    @Column(name = "delivery_postal_code", nullable = false)
    private String postalCode;

    @Column(name = "delivery_latitude", nullable = false)
    private Double latitude;

    @Column(name = "delivery_longitude", nullable = false)
    private Double longitude;

    @Column(name = "delivery_is_default")
    private Boolean isDefault;
}