package csd.app.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import csd.app.payload.response.MessageResponse;
import csd.app.payload.response.ProductResponse;
import csd.app.payload.request.AddProductRequest;
import csd.app.payload.request.GiveProductRequest;
import csd.app.product.Product;
import csd.app.product.ProductService;
import csd.app.product.ProductGA;

import csd.app.user.SameUserException;
import csd.app.user.User;
import csd.app.user.UserService;

import org.springframework.web.bind.annotation.GetMapping;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    public ProductController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    // Get all products from database
    @GetMapping("/api/products")
    public List<ProductResponse> getProducts() {
        List<Product> products = productService.listProducts();
        List<ProductResponse> resp = new ArrayList<>();
        for (Product product : products) {
            if (productService.getProductGA(product.getId()) == null) {
                ProductResponse prodResp = new ProductResponse(product.getId(), product.getProductName(),
                        product.getCondition(), product.getDateTime(), product.getDescription(), product.getCategory(),
                        product.getImageUrl(), product.getUser());
                resp.add(prodResp);
            }
        }
        return resp;
    }
    
    // Get product by productId
    @GetMapping("/api/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        ProductResponse resp = new ProductResponse(id, product.getProductName(), product.getCondition(),
                product.getDateTime(), product.getDescription(), product.getCategory(), product.getImageUrl(),
                product.getUser());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/api/products")
    public ResponseEntity<?> addProduct(@Valid @RequestBody AddProductRequest addProductRequest) {

        // Create new product and set to user
        Product newProduct = new Product(addProductRequest.getProductName(), addProductRequest.getCondition(),
                addProductRequest.getDateTime(), addProductRequest.getCategory(),
                addProductRequest.getDescription(), addProductRequest.getImageUrl());
        User user = userService.getUser(addProductRequest.getUserId());
        newProduct.setUser(user);
        if (productService.addProduct(newProduct) == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("User is not logged in."));
        }
        return ResponseEntity.ok(new MessageResponse("Product registered successfully!"));
    }

    // Get all products owned by user
    @GetMapping("api/products/user/{id}")
    public List<ProductResponse> getProductByOwner(User user) {
        List<Product> products = productService.getProductsByUser(user);
        List<ProductResponse> resp = new ArrayList<>();
        for (Product product : products) {
            ProductResponse prodResp = new ProductResponse(product.getId(), product.getProductName(),
                    product.getCondition(), product.getDateTime(), product.getDescription(), product.getCategory(),
                    product.getImageUrl(), product.getUser());
            resp.add(prodResp);
        }
        return resp;
    }

    @PutMapping("api/products/update")
    public ResponseEntity<?> updateProductDetail(@RequestBody Product PR) {
        Product product = productService.getProduct(PR.getId());

        // Validation check for updating user details
        try {
            product.setCategory(PR.getCategory());
            product.setCondition(PR.getCondition());
            product.setDateTime(PR.getDateTime());
            product.setDescription(PR.getDescription());
            product.setImageUrl(PR.getImageUrl());
            product.setProductName(PR.getProductName());
            productService.updateProduct(product);
            return ResponseEntity.ok(new MessageResponse("Product detail updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body((new MessageResponse("Error: Invalid Details")));
        }
    }

    @DeleteMapping("api/products/remove/{id}")
    public ResponseEntity<?> removeProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        productService.deleteProduct(product);
        return ResponseEntity.ok(new MessageResponse("Product has been removed"));
    }

    // Give product from owner to receiver
    @PostMapping("/api/products/give")
    public ResponseEntity<?> giveProduct(@Valid @RequestBody GiveProductRequest giveProductRequest) {
        Long productId = giveProductRequest.getProductId();
        String receiverUsername = giveProductRequest.getReceiverUsername();

        // Check product exists
        Product product = productService.getProduct(productId);
        User owner = product.getUser();
        // Check receiver exists
        User user = userService.getUserByUsername(receiverUsername);
        if (user.getId() == owner.getId()) {
            throw new SameUserException();
        }
        ProductGA productGA = new ProductGA(productId, user.getId());
        productService.addProductGA(productGA);

        return ResponseEntity.ok(new MessageResponse("Item given successfully"));

    }

    // Get all products that have been given away already by the owner
    @GetMapping("api/products/give/{id}")
    public List<ProductGA> getGiveAwayByOwner(@PathVariable Long id) {
        List<ProductGA> productGAs = productService.listProductGAs();
        List<ProductGA> resp = new ArrayList<>();
        for (ProductGA productGA : productGAs) {
            Product product = productService.getProduct(productGA.getId());
            if (id == product.getUser().getId()) {
                resp.add(productGA);
            }
        }
        return resp;
    }
}