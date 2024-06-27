package org.orkg.curation.adapter.output.neo4j

import org.neo4j.cypherdsl.core.Predicates.*
import org.orkg.curation.adapter.output.neo4j.internal.Neo4jClassCurationRepository
import org.orkg.curation.adapter.output.neo4j.internal.Neo4jPredicateCurationRepository
import org.orkg.curation.output.CurationRepository
import org.orkg.graph.adapter.output.neo4j.withDefaultSort
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jCurationAdapter(
    private val predicateRepository: Neo4jPredicateCurationRepository,
    private val classRepository: Neo4jClassCurationRepository
) : CurationRepository {
    override fun findAllPredicatesWithoutDescriptions(pageable: Pageable): Page<Predicate> =
        predicateRepository.findAllPredicatesWithoutDescriptions(pageable.withDefaultSort { Sort.by("created_at") })
            .map { it.toPredicate() }

    override fun findAllClassesWithoutDescriptions(pageable: Pageable): Page<Class> =
        classRepository.findAllClassesWithoutDescriptions(pageable.withDefaultSort { Sort.by("created_at") })
            .map{ it.toClass() }
}
