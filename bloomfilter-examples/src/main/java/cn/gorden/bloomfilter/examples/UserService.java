package cn.gorden.bloomfilter.examples;

import cn.gorden.bloomfilter.aspect.annotation.BFMightContain;
import cn.gorden.bloomfilter.aspect.annotation.BFPut;

/**
 * @author GordenTam
 **/
public class UserService {

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
