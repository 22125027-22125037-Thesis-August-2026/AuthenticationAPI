package com.mhsa.backend.tracking.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue trackingDiaryCreatedQueue() {
        return new Queue("tracking.diary.created", true);
    }

    @Bean
    public Queue trackingMoodLoggedQueue() {
        return new Queue("tracking.mood.logged", true);
    }

    @Bean
    public Queue trackingSleepLoggedQueue() {
        return new Queue("tracking.sleep.logged", true);
    }

    @Bean
    public Queue trackingFoodLoggedQueue() {
        return new Queue("tracking.food.logged", true);
    }

    @Bean
    public Queue trackingStreakUpdatedQueue() {
        return new Queue("tracking.streak.updated", true);
    }

    @Bean
    public Queue authUserDeletedQueue() {
        return new Queue("auth.user.deleted", true);
    }

    @Bean
    public Queue authUserUpdatedQueue() {
        return new Queue("auth.user.updated", true);
    }

    @Bean
    public Queue authGrantCreatedQueue() {
        return new Queue("auth.grant.created", true);
    }
}
