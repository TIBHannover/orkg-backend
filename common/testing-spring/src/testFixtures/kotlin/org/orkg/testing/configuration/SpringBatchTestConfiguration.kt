package org.orkg.testing.configuration

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.transaction.PlatformTransactionManager

@TestConfiguration
class SpringBatchTestConfiguration(
    @param:Qualifier("jpaTransactionManager")
    private val jpaTransactionManager: PlatformTransactionManager,
) : DefaultBatchConfiguration() {
    override fun getTransactionManager(): PlatformTransactionManager = jpaTransactionManager
}
