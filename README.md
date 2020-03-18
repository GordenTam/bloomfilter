# bloom filter's java implements according to guava which support redis and spring boot.
基于guava实现的布隆过滤器,添加了redis实现和aop支持。并可以集成到spring boot中。

# Examples
## using bloom filter directly  

Example for Maven:
```xml
<dependency>
    <groupId>cn.gorden.bloomfilter</groupId>
    <artifactId>bloomfiter-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Example for save bloomfilter in redis:
```java
public class BloomFilterExample {

    @Test
    public void bloomFilterUsingExample() {
        SimpleJedisOperator jedisOperator = SimpleJedisOperator.getInstance();
        BloomFilter bf = RedisBloomFilter.builder()
                .withName("test")
                .withFpp(0.03)
                .withRedisOperator(jedisOperator)
                .withExpectedInsertions(1000000L)
                .withHashFunction(new Murmur3_128HashFunction(0))
                .withBloomFilterSerializer(new FastJsonBloomFilterSerializer())
                .build();

        bf.put("apple");
        assertTrue(bf.mightContain("apple"));
        assertFalse(bf.mightContain("banana"));
    }
}
```

## using annotation support  

Example for Maven:
```xml
<dependency>
    <groupId>cn.gorden.bloomfilter</groupId>
    <artifactId>bloomfiter-aspect</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

使用注解支持前，需要将切面添加到项目中，如果使用aspectJ，则需要将切面添加进aop.xml中，注意此处需要添加两个切面
```xml
  <aspect name="cn.gorden.bloomfilter.aspect.BloomFilterOperationAspect"/>
  <aspect name="cn.gorden.bloomfilter.core.aspect.BloomFilterCreatedAspect"/>
```

如果使用spring aop，则需要将BloomFilterOperationAspect切面添加到容器中
```java
public class BloomFilterConfiguration {
    @Bean
    public BloomFilterOperationAspect bloomFilterOperationAspect() {
        return new BloomFilterOperationAspect();
    }
}
```

使用注册之前，需要先创建布隆过滤器，被创建的布隆过滤器会被BloomFilterObserver观察到。之后便可以通过注解操作布隆过滤器了。
```java
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

public class BloomFilterOperationAspectExample {
   
    @Test
    public void testAspect() {
        BloomFilter concurrentBloomFilter = ConcurrentBloomFilter.create("test", 100000, 0.03, new JdkSerializationBloomFilterSerializer(), new Murmur3_128HashFunction(0));
        UserService userService = new UserService();
        userService.createUser("Tom");
        String result = userService.getUser("Jerry");
        assertEquals("user with id Jerry not in bloom filter", result);
    }

}
```

## using in spring boot  

Example for Maven:
```xml
<dependency>
    <groupId>cn.gorden.bloomfilter</groupId>
    <artifactId>spring-boot-starter-bloomfilter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>cn.gorden.bloomfilter</groupId>
    <artifactId>spring-boot-starter-bloomfilter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

在application.yml中定义布隆过滤器，可选的type有REDIS和JDK，分别对应存储在redis和jvm中
```yaml
bloom-filter:
  type: REDIS
  names: test
  expectedInsertions: 1000000L
  fpp: 0.03
```

```java
@RestController
public class controller {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/create/user", method = GET)
    public void createUser(@RequestParam(value = "userId")String userId) {
        userService.cUser(userId);
    }

    @RequestMapping(value = "/user", method = GET)
    public String getUser(@RequestParam(value = "userId")String userId) {
        return userService.gUser(userId);
    }
}

@Service
public class UserService {

    @BFPut("test")
    public String cUser(String userId) {
        return userId;
    }

    @BFMightContain(value = "test", fallback = "getUserFallBack")
    public String gUser(String userId) {
        return userId;
    }

    public String getUserFallBack(String userId) {
        return "1";
    }
}
```
