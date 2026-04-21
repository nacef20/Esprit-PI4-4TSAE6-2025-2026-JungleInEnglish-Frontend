package tn.esprit.jungleinenglishbackend.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(
    name = "product",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_product_title_category",
        columnNames = {"title", "category"}
    )
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 500)
    private String title;

    @Column(length = 5000)
    private String description;

    private String category;

    private Double price;

    private int stock;

    /**
     * Minimum stock level threshold. When current stock drops below this value,
     * an inventory alert is triggered. Defaults to 10.
     */
    private int minStockLevel = 10;

    private String image;

    private int discount;

    private Double discountPrice;

    private Boolean isActive;

}
