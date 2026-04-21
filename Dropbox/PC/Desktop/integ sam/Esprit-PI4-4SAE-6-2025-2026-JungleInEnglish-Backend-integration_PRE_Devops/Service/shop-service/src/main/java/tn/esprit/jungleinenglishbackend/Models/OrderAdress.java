package tn.esprit.jungleinenglishbackend.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity

public class OrderAdress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;

    private String lastName;

    private String email;

    @jakarta.persistence.Column(length = 8)
    @jakarta.validation.constraints.Pattern(regexp = "\\d{8}", message = "Telephone must be exactly 8 digits")
    private String telephone;

    private String address;

    private String city;

    private String state;

    @jakarta.persistence.Column(length = 4)
    @jakarta.validation.constraints.Pattern(regexp = "\\d{4}", message = "Postal code must be exactly 4 digits")
    private String postalCode;

}
