package org.orkg.contenttypes.adapter.output.neo4j.configuration

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories

@Configuration
@EnableNeo4jRepositories("org.orkg.contenttypes.adapter.output.neo4j.internal")
@EntityScan("org.orkg.contenttypes.adapter.output.neo4j.internal")
class ContentTypesNeo4jConfiguration
