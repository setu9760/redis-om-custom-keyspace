# redis-om-custom-keyspace

This repository describes and demonstrates a bug in [redis-om-spring](https://github.com/redis/redis-om-spring) library.

## Description

While exploring a requirement of adding a configurable prefix to redis keys for every operation I came across `fallbackKeySpaceResolver` in  `org.springframework.data.redis.core.mapping.RedisMappingContext` in spring-data. 

On the surface this looked to meet the bill for our requirement. None of our redis document/hash are annotated with custom keyspace value and this would allow us to prefix everykey with configurable value. As an example if we are trying to store `com.example.Company` entity instead of the key being;

`com.example.Company:ID1`

it would be 

`<prefix>:Company:ID1`


We fined the `RedisMappingContext` bean as below;

```java
  @Bean
  @Primary
  @Profile("custom-keyspace")
  public RedisMappingContext keyValueMappingContext(
      @Value(value = "${redis.custom.keyspace.prefix}") String keyspacePrefix) {
    RedisMappingContext mappingContext = new RedisMappingContext();
    mappingContext.setFallbackKeySpaceResolver(type -> keyspacePrefix + ":" + type.getSimpleName());
    return mappingContext;
  }
```

This did not work as intended; and any fields marked as indexed with it's own repository method were failing the redis rearch through redis-om repository. 

On further investigation identified that `RediSearchIndexer` is hardcoding the prefix as `class.getName() + ":"` in `createIndexFor(Class)` method. This causes all the FT.SEARCH calls to fail. 

For this given example FT.CREATE looks like this;

```
FT.CREATE"
  "com.example.redisomcustomkeyspace.TestDataIdx" "ON" "JSON" "PREFIX" "1" "com.example.redisomcustomkeyspace.TestData:"
"SCHEMA"
  "$.number" "AS" "number" "NUMERIC"
  "$.subDataSet[0:].department" "AS" "subDataSet_department" "TAG" "SEPARATOR" "|"
  "$.subDataSet[0:].country" "AS" "subDataSet_country" "TAG" "SEPARATOR" "|"
  "$.code" "AS" "code" "TAG" "SEPARATOR" ","
```

instead it should look like this;

```
"FT.CREATE"
  "com.example.redisomcustomkeyspace.TestDataIdx" "ON" "JSON" "PREFIX" "1" "DEV:TestData:"
"SCHEMA"
  "$.number" "AS" "number" "NUMERIC"
  "$.subDataSet[0:].department" "AS" "subDataSet_department" "TAG" "SEPARATOR" "|"
  "$.subDataSet[0:].country" "AS" "subDataSet_country" "TAG" "SEPARATOR" "|"
  "$.code" "AS" "code" "TAG" "SEPARATOR" "|"
```

## Reproduce bug

To see the working version first run the `RedisOmCustomKeyspaceApplication` as is and it should log this upon startup

```
2022-12-15 11:41:56.518  INFO 80396 --- [  restartedMain] c.e.r.RedisOmCustomKeyspaceApplication   : Started RedisOmCustomKeyspaceApplication in 25.062 seconds (JVM running for 25.825)
Optional[TestData(code=td1, number=12, values=[], subDataSet=[TestData.SubData(name=sd1, department=finance, country=uk), TestData.SubData(name=sd2, department=tech, country=usa)])]
Optional[TestData(code=td1, number=12, values=[], subDataSet=[TestData.SubData(name=sd1, department=finance, country=uk), TestData.SubData(name=sd2, department=tech, country=usa)])]
[TestData(code=td2, number=45, values=[testValue], subDataSet=[TestData.SubData(name=sd23, department=finance, country=mexico), TestData.SubData(name=sd24, department=tech, country=uk)])]
[TestData(code=td1, number=12, values=[], subDataSet=[TestData.SubData(name=sd1, department=finance, country=uk), TestData.SubData(name=sd2, department=tech, country=usa)])]
```

Now to reproduce the issue clear everything in redis by issuing `FLUSHALL` and then run the application with spring profile `custom-keyspace` and it would log this demonstrating the search methods fail;

```
2022-12-15 11:52:28.175  INFO 80695 --- [  restartedMain] c.e.r.RedisOmCustomKeyspaceApplication   : Started RedisOmCustomKeyspaceApplication in 3.558 seconds (JVM running for 4.569)
Optional[TestData(code=td1, number=12, values=[], subDataSet=[TestData.SubData(name=sd1, department=finance, country=uk), TestData.SubData(name=sd2, department=tech, country=usa)])]
Optional.empty
[]
[]
```

## Potential fix

This fix for this bug would be to update `RediSearchIndexer.createIndexFor(Class)` method with this;
```java
   String entityPrefix = cl.getName() + ":";
   if (mappingContext.hasPersistentEntityFor(cl)) {
     RedisPersistentEntity<?> persistentEntity = mappingContext.getRequiredPersistentEntity(cl);
     entityPrefix = persistentEntity.getKeySpace() != null ? persistentEntity.getKeySpace() + ":" : entityPrefix;
   }
```
