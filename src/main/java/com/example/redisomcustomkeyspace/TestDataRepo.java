package com.example.redisomcustomkeyspace;

import com.redis.om.spring.annotations.Query;
import com.redis.om.spring.repository.RedisDocumentRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface TestDataRepo extends RedisDocumentRepository<TestData, String> {

  @Query("@subDataSet_department:{$department} @subDataSet_country:{$country}")
  Optional<TestData> findByDepartmentAndCountry(String department, String country);

  @Query("@subDataSet_country:{$country}")
  Iterable<TestData> findAllBySubDataSet_Country(String country);

  Iterable<TestData> findAllByNumber(Integer number);
}
