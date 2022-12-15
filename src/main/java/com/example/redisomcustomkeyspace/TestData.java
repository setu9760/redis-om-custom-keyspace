package com.example.redisomcustomkeyspace;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.annotations.TextIndexed;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Document
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TestData {

  @Id
  private String code;
  @NumericIndexed
  private Integer number;
  private Set<String> values = new HashSet<>();
  @Indexed
  @Searchable
  private Set<SubData> subDataSet = new HashSet<>();
  @Data
  @RequiredArgsConstructor(staticName = "of")
  public static class SubData {
    private final String name;
    @TagIndexed
    @Searchable
    private final String department;
    @TagIndexed
    @Searchable
    private final String country;
  }
}
