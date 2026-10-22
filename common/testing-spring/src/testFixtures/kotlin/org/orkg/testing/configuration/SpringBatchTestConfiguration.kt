package org.orkg.testing.configuration

import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration
import org.springframework.batch.core.repository.ExecutionContextSerializer
import org.springframework.batch.core.repository.dao.DefaultExecutionContextSerializer
import org.springframework.batch.core.repository.dao.DefaultExecutionContextSerializer.DEFAULT_FILTER_PATTERN
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.transaction.PlatformTransactionManager
import java.io.ObjectInputFilter

@TestConfiguration
class SpringBatchTestConfiguration(
    @param:Qualifier("jpaTransactionManager")
    private val jpaTransactionManager: PlatformTransactionManager,
) : JdbcDefaultBatchConfiguration() {
    override fun getTransactionManager(): PlatformTransactionManager = jpaTransactionManager

    override fun getExecutionContextSerializer(): ExecutionContextSerializer =
        DefaultExecutionContextSerializer().apply {
            setObjectInputFilter(ObjectInputFilter.Config.createFilter("org.orkg.**;$DEFAULT_FILTER_PATTERN"))
        }
}
