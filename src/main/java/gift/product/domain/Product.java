package gift.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(length = 15, nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(name = "image_url", nullable = false)
    private String imageURL;

    protected Product() {

    }

    public Product(String name, Long price, String imageURL) {
        this.name = name;
        this.price = price;
        this.imageURL = imageURL;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getPrice() {
        return price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void update(String name, Long price, String imageURL) {
        if (name != null) {
            this.name = name;
        }
        if (price != null) {
            this.price = price;
        }
        if (imageURL != null) {
            this.imageURL = imageURL;
        }
    }
}