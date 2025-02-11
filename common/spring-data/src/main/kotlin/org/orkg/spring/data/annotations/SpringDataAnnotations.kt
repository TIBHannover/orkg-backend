package org.orkg.spring.data.annotations

import org.springframework.core.annotation.AliasFor
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Transactional(transactionManager = "jpaTransactionManager")
annotation class TransactionalOnJPA(
    @get:AliasFor(annotation = Transactional::class, attribute = "readOnly") val readOnly: Boolean = false,
    @get:AliasFor(annotation = Transactional::class, attribute = "propagation") val propagation: Propagation = Propagation.REQUIRED,
    @get:AliasFor(annotation = Transactional::class, attribute = "isolation") val isolation: Isolation = Isolation.DEFAULT,
)

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Transactional(transactionManager = "neo4jTransactionManager")
annotation class TransactionalOnNeo4j(
    @get:AliasFor(annotation = Transactional::class, attribute = "readOnly") val readOnly: Boolean = false,
    @get:AliasFor(annotation = Transactional::class, attribute = "propagation") val propagation: Propagation = Propagation.REQUIRED,
    @get:AliasFor(annotation = Transactional::class, attribute = "isolation") val isolation: Isolation = Isolation.DEFAULT,
)
