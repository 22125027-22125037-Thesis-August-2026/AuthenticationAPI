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

    // Grant events are now consumed from dedicated, DLQ-backed queues bound to auth-service's topic
    // exchange — see GrantMessagingConfig. The shared default-exchange auth.grant.created queue is
    // no longer used by tracking (it would compete with other consumers for messages).
}
