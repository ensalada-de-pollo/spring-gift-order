package gift.product.controller;

import gift.product.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductViewController {

    private final ProductService productService;

    public ProductViewController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String getProducts(Model model) {
        model.addAttribute(productService.findAllProducts());
        return "index";
    }

    @GetMapping("/create")
    public String create() {
        return "create";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {
        model.addAttribute(
            "product",
            productService.findById(id)
        );
        return "edit";
    }
}
