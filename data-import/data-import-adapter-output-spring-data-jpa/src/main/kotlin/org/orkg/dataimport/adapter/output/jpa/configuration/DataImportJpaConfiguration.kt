package org.orkg.dataimport.adapter.output.jpa.configuration

import jakarta.persistence.ColumnResult
import jakarta.persistence.ConstructorResult
import jakarta.persistence.SqlResultSetMapping
import org.orkg.dataimport.adapter.output.jpa.internal.ResourceToTypeMappingEntity
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@SqlResultSetMapping(
    name = "ResourceToTypeMapping",
    classes = [
        ConstructorResult(
            targetClass = ResourceToTypeMappingEntity::class,
            columns = [
                ColumnResult(name = "iri", type = String::class),
                ColumnResult(name = "type", type = String::class),
            ],
        ),
    ],
)
@EnableJpaRepositories("org.orkg.dataimport.adapter.output.jpa.internal", transactionManagerRef = "jpaTransactionManager")
@EntityScan("org.orkg.dataimport.adapter.output.jpa.internal")
class DataImportJpaConfiguration
