package com.example.redisomcustomkeyspace;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRedisDocumentRepositories
public class RedisOmCustomKeyspaceApplication {

  public static void main(String[] args) {
    SpringApplication.run(RedisOmCustomKeyspaceApplication.class, args);
  }

}
