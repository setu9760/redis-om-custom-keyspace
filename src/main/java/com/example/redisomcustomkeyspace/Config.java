package com.example.redisomcustomkeyspace;

import com.example.redisomcustomkeyspace.TestData.SubData;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.mapping.RedisMappingContext;

@Configuration
public class Config {

  @Bean
  @Primary
  @Profile("custom-keyspace")
  public RedisMappingContext keyValueMappingContext(
      @Value(value = "${redis.custom.keyspace.prefix}") String keyspacePrefix) {
    RedisMappingContext mappingContext = new RedisMappingContext();
    mappingContext.setFallbackKeySpaceResolver(type -> keyspacePrefix + ":" + type.getSimpleName());
    return mappingContext;
  }

  @Bean
  CommandLineRunner loadTestData(TestDataRepo repo) {
    return (args) -> {
      TestData td1 = testData("td1", 12);
      td1.setSubDataSet(
          Set.of(
              subData("sd1", "finance", "uk"),
              subData("sd2", "tech", "usa")
          )
      );

      TestData td2 = testData("td2", 45, "testValue");
      td2.setSubDataSet(
          Set.of(
              subData("sd23", "finance", "mexico"),
              subData("sd24", "tech", "uk")
          )
      );

      repo.save(td1);
      repo.save(td2);

      System.out.println(repo.findById(td1.getCode()));

      System.out.println(repo.findByDepartmentAndCountry("finance", "uk"));

      System.out.println(repo.findAllBySubDataSet_Country("mexico"));

      System.out.println(repo.findAllByNumber(12));
    };
  }

  private static TestData testData(String code, Integer number, String...values) {
    TestData td = TestData.of();
    td.setCode(code);
    td.setNumber(number);
    td.setValues(Set.of(values));
    return td;
  }

  private static SubData subData(String name, String department, String country) {
    return SubData.of(name, department, country);
  }
}
