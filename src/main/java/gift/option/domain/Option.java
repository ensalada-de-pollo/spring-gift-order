package gift.option.domain;

import gift.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "option")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(nullable = false)
    private Long quantity;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    protected Option() {

    }

    public Option(String name, Long quantity, Product product) {
        this.name = name;
        this.quantity = quantity;
        this.product = product;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Product getProduct() {
        return product;
    }

    public Option subQuantity(Long quantity) {
        this.quantity = this.quantity - quantity;

        return this;
    }

    public void update(String name) {
        this.name = name;
    }
}
