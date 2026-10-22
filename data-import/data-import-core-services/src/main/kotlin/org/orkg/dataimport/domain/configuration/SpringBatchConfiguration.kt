package org.orkg.dataimport.domain.configuration

import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration
import org.springframework.batch.core.repository.ExecutionContextSerializer
import org.springframework.batch.core.repository.dao.DefaultExecutionContextSerializer
import org.springframework.batch.core.repository.dao.DefaultExecutionContextSerializer.DEFAULT_FILTER_PATTERN
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import java.io.ObjectInputFilter

@Configuration
class SpringBatchConfiguration(
    @param:Qualifier("jpaTransactionManager")
    private val jpaTransactionManager: PlatformTransactionManager,
    @param:Value($$"${job-executor-thread-pool-size:#{2}}")
    private val jobExecutorThreadPoolSize: Int,
) : JdbcDefaultBatchConfiguration() {
    override fun getTransactionManager(): PlatformTransactionManager = jpaTransactionManager

    override fun getExecutionContextSerializer(): ExecutionContextSerializer =
        DefaultExecutionContextSerializer().apply {
            setObjectInputFilter(ObjectInputFilter.Config.createFilter("org.orkg.**;$DEFAULT_FILTER_PATTERN"))
        }

    override fun getTaskExecutor(): TaskExecutor =
        ThreadPoolTaskExecutor().apply {
            maxPoolSize = jobExecutorThreadPoolSize
            initialize()
        }
}
