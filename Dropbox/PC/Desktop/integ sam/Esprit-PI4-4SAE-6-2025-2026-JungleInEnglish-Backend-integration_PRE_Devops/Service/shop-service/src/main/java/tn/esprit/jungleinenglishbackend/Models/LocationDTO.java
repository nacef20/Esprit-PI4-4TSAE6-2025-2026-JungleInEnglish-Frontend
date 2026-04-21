package tn.esprit.jungleinenglishbackend.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LocationDTO {
    private Double latitude;
    private Double longitude;
    private String address;
}
