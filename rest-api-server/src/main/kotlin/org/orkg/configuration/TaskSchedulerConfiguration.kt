package org.orkg.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
class TaskSchedulerConfiguration {
    @Bean
    fun taskScheduler(
        @Value("\${orkg.concurrent.task-scheduler-thread-pool-size:#{3}}") threadPoolSize: Int,
    ): ThreadPoolTaskScheduler =
        ThreadPoolTaskScheduler().apply { poolSize = threadPoolSize }
}
