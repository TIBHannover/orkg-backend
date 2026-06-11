package org.orkg.testing.configuration

import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.transaction.PlatformTransactionManager

@TestConfiguration
class SpringBatchTestConfiguration(
    @param:Qualifier("jpaTransactionManager")
    private val jpaTransactionManager: PlatformTransactionManager,
) : JdbcDefaultBatchConfiguration() {
    override fun getTransactionManager(): PlatformTransactionManager = jpaTransactionManager
}
