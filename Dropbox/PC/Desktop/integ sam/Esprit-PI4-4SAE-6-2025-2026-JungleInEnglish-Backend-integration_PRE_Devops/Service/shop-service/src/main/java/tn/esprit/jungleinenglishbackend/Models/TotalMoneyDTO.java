package tn.esprit.jungleinenglishbackend.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalMoneyDTO {
    private BigDecimal totalMoney;
}
