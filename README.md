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

Example for code:
```java

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

使用注解支持前，需要将切面添加到项目中，如果使用aspectJ，则需要将切面添加进aop.xml中
```xml
  <aspect name="cn.gorden.bloomfilter.aspect.BloomFilterOperationAspect"/>
```

如果使用spring aop，则需要将切面添加到容器中
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
public class BloomFilterOperationAspectExample {

    @BFPut(value = "test")
    public String bfPut(String str) {
        return str;
    }

    @BFMightContain(value = "test", fallback = "BFFallback")
    public String mightContain(String str) {
        return str;
    }

    public String BFFallback(String str) {
        return str + " not in bloom filter";
    }

    @Test
    public void testAspect() {
        BloomFilter concurrentBloomFilter = ConcurrentBloomFilter.create("test", 100000, 0.03, new JdkSerializationBloomFilterSerializer(), new Murmur3_128HashFunction(0));
        BloomFilterOperationAspectExample bloomFilterAspectExample = new BloomFilterOperationAspectExample();
        bloomFilterAspectExample.bfPut("apple");
        String result = bloomFilterAspectExample.mightContain("banana");
        assertEquals("banana not in bloom filter", result);
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
```

在application.yml中定义布隆过滤器
```yaml
bloom-filter:
  type: REDIS
  names: test
  expectedInsertions: 1000000L
  fpp: 0.03
```

