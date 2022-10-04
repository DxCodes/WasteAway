package csd.week6.product;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import csd.app.product.Product;
import csd.app.product.ProductRepository;
import csd.app.user.User;
import csd.app.user.UserRepository;
import csd.app.user.UserInfo;
import csd.app.user.UserInfoRepository;

/** Start an actual HTTP server listening at a random port */
@SpringBootTest(classes = csd.app.Main.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductIntegrationTest {

        @LocalServerPort
        private int port;

        private final String baseUrl = "http://localhost:";

        @Autowired
        /**
         * Use TestRestTemplate for testing a real instance of your application as an
         * external actor.
         * TestRestTemplate is just a convenient subclass of RestTemplate that is
         * suitable for integration tests.
         * It is fault tolerant, and optionally can carry Basic authentication headers.
         */
        private TestRestTemplate restTemplate;

        @Autowired
        private ProductRepository products;

        @Autowired
        private UserRepository users;

        @Autowired
        private UserInfoRepository userInfos;

        @Autowired
        private BCryptPasswordEncoder encoder;

        @Autowired
        private final ObjectMapper objectMapper = new ObjectMapper();

        @AfterEach
        void tearDown() {
                // clear the database after each test
                products.deleteAll();
                users.deleteAll();
                userInfos.deleteAll();
        }

        @Test
        public void getProducts_Success() throws Exception {
                URI uri = new URI(baseUrl + port + "/api/products");
                User user = new User("tester1", "abc@test.com", encoder.encode("password"));
                users.save(user);
                UserInfo userInfo = new UserInfo(user.getId(), user.getUsername(), "SINGAPORE 511111", 87231231);
                userInfos.save(userInfo);
                Product product = new Product("PHONE", "NEW", LocalDateTime.now().toString(),
                                "ELECTRONICS");
                product.setUser(user);
                products.save(product);

                ResponseEntity<Product[]> result = restTemplate.getForEntity(uri,
                                Product[].class);
                Product[] products = result.getBody();

                assertEquals(200, result.getStatusCode().value());
                assertEquals(1, products.length);
        }

        @Test
        public void getProduct_ValidProductId_Success() throws Exception {
                User user = new User("tester2", "blabla@hotmail.com",
                                encoder.encode("password"));
                users.save(user);
                UserInfo userInfo = new UserInfo(user.getId(), user.getUsername(), "SINGAPORE 511111", 87231231);
                userInfos.save(userInfo);
                Product product = new Product("CAMERA", "OLD",
                                LocalDateTime.now().toString(), "ELECTRONICS", "TestDescription");
                product.setUser(user);
                Long id = products.save(product).getId();
                URI uri = new URI(baseUrl + port + "/api/products/" + id);

                ResponseEntity<Product> result = restTemplate.getForEntity(uri,
                                Product.class);

                assertEquals(200, result.getStatusCode().value());
                assertEquals(product.getProductName(), result.getBody().getProductName());
        }

        @Test
        public void getProduct_InvalidProductId_Failure() throws Exception {
                URI uri = new URI(baseUrl + port + "/api/products/4");

                ResponseEntity<Product> result = restTemplate.getForEntity(uri,
                                Product.class);

                assertEquals(400, result.getStatusCode().value());
        }

        @Test
        public void addProduct_Success() throws Exception {
                URI uri = new URI(baseUrl + port + "/api/products");
                User user = new User("tester2", "blabla@hotmail.com",
                                encoder.encode("password"));
                users.save(user);

                JSONObject productJsonObject = new JSONObject();
                productJsonObject.put("productName", "CAMERA");
                productJsonObject.put("condition", "NEW");
                productJsonObject.put("dateTime", LocalDateTime.now().toString());
                productJsonObject.put("category", "ELECTRONICS");
                productJsonObject.put("description", "Test Description");
                productJsonObject.put("imageUrl", "https://fdn2.gsmarena.com/vv/bigpic/apple-iphone-3gs-ofic.jpg");
                productJsonObject.put("userId", user.getId());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> request = new HttpEntity<String>(productJsonObject.toString(), headers);

                ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);
                JsonNode root = objectMapper.readTree(result.getBody());

                assertEquals(200, result.getStatusCode().value());
                assertEquals("Product registered successfully!", root.path("message").asText());
        }
}
