package tn.esprit.jungleinenglishbackend.Models;

import lombok.Data;
import lombok.ToString;

@ToString
@Data

public class OrderDTO {
    private String firstName;

    private String lastName;

    private String email;

    @jakarta.validation.constraints.Pattern(regexp = "\\d{8}", message = "Telephone must be exactly 8 digits")
    private String telephone;

    private String address;

    private String city;

    private String state;

    @jakarta.validation.constraints.Pattern(regexp = "\\d{4}", message = "Postal code must be exactly 4 digits")
    private String postalCode;

    private String paymentType;

}
