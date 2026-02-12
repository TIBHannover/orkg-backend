package org.orkg.configuration

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
class TaskSchedulerConfiguration {
    @Bean
    fun taskScheduler(
        @Value("\${orkg.concurrent.task-scheduler-thread-pool-size:#{10}}") threadPoolSize: Int,
    ): ThreadPoolTaskScheduler =
        ThreadPoolTaskScheduler().apply { poolSize = threadPoolSize }

    @Bean
    fun registerThreadPoolTaskSchedulerMetrics(taskScheduler: ThreadPoolTaskScheduler, registry: MeterRegistry): List<Gauge> =
        listOf(
            Gauge.builder("orkg.concurrent.task-scheduler.active-thread-count") { taskScheduler.activeCount }
                .description("The count of currently active threads")
                .register(registry),
            Gauge.builder("orkg.concurrent.task-scheduler.thread-pool-size") { taskScheduler.poolSize }
                .description("The current size of the thread pool")
                .register(registry),
        )
}
