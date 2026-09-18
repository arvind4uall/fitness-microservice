package com.fsdarvind.fitness.activityservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing // else createdAt and updatedAt field will be null
public class MongoConfig {
}
