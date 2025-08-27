package org.orkg.testing.configuration

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager

@TestConfiguration
class JpaTransactionManagerConfiguration {
    fun jpaTransactionManager(): PlatformTransactionManager = JpaTransactionManager()
}
