package cn.gorden.bloomfilter.examples;

import cn.gorden.bloomfilter.aspect.annotation.BFMightContain;
import cn.gorden.bloomfilter.aspect.annotation.BFPut;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.Assert.assertEquals;

/**
 * @author GordenTam
 **/
@SpringBootApplication
@RestController
public class BloomFilterSpringBootApplicationExample {

    public static void main(String[] args) {
        SpringApplication.run(BloomFilterSpringBootApplicationExample.class);
    }



    @Service
    public static class UserService {

        @BFPut(value = "test")
        public String createUser(String userId) {
            System.out.println("user with id " + userId + " has been created");
            return userId;
        }

        @BFMightContain(value = "test", fallback = "BFFallback")
        public String getUser(String userId) {
            return userId;
        }

        public String BFFallback(String userId) {
            System.out.println(userId);
            return "user with id " + userId + " not in bloom filter";
        }
    }


    public static class BloomFilterSpringBootApplicationTest {

    }

}
