package com.ecommerce.backend.common.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
public class CacheConfig {

  @Bean
  public RedisCacheConfiguration cacheConfiguration() {
    BasicPolymorphicTypeValidator typeValidator =
        BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("com.ecommerce.backend")
            .allowIfSubType("java.util")
            .allowIfSubType("java.lang")
            .allowIfSubType("java.math")
            .allowIfSubType("java.time")
            .build();

    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                GenericJacksonJsonRedisSerializer.builder()
                    .enableDefaultTyping(typeValidator)
                    .build()));
  }
}
