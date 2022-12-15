package com.example.redisomcustomkeyspace;

import com.example.redisomcustomkeyspace.TestData.SubData;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

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

      TestData td2 = testData("td2", 45);
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

      System.out.println(repo.findByCountry("uk"));
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
