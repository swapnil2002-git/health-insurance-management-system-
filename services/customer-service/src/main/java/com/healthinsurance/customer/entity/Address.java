package com.healthinsurance.customer.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.util.Set;

@Data
@Entity
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID addressId;

    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    @OneToMany(mappedBy = "address", cascade = CascadeType.ALL)
    private Set<CustomerAddress> customerAddresses;
}