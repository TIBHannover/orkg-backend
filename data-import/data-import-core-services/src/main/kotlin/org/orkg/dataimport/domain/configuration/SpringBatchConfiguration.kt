package org.orkg.dataimport.domain.configuration

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class SpringBatchConfiguration(
    @param:Qualifier("jpaTransactionManager")
    private val jpaTransactionManager: PlatformTransactionManager,
    @param:Value("\${job-executor-thread-pool-size:#{2}}")
    private val jobExecutorThreadPoolSize: Int,
) : DefaultBatchConfiguration() {
    override fun getTransactionManager(): PlatformTransactionManager = jpaTransactionManager

    override fun getTaskExecutor(): TaskExecutor =
        ThreadPoolTaskExecutor().apply {
            maxPoolSize = jobExecutorThreadPoolSize
            initialize()
        }
}
