package org.orkg

import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.configuration.FeatureFlags
import org.orkg.contenttypes.input.RetrieveResearchFieldUseCase
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.RetrieveLegacyStatisticsUseCase
import org.orkg.graph.input.StatementUseCases
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
@Profile("production")
class CacheWarmup(
    private val resourceService: ResourceUseCases,
    private val predicateService: PredicateUseCases,
    private val statementService: StatementUseCases,
    private val statsService: RetrieveLegacyStatisticsUseCase,
    private val researchFieldService: RetrieveResearchFieldUseCase,
    private val flags: FeatureFlags,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun run(args: ApplicationArguments?) {
        if (!flags.cacheWarmup) {
            logger.info("Skipping cache warmup")
            return
        }
        logger.info("Begin warming up caches...")
        warmupPredicates()
        warmupHome()
        warmupComparisonsList()
        warmupPaperList()
        warmupVisualizationsList()
        warmupReviewsList()
        warmupListsList()
        logger.info("End of cache warmup...")
    }

    private fun warmupPredicates() {
        statementService.countPredicateUsage(PageRequest.of(0, 200)).forEach {
            predicateService.findById(it.id)
        }
    }

    private fun warmupHome() {
        val featuredResearchField = statsService.getFieldsStats().toList().maxByOrNull { it.second }?.first!!
        statementService.findAll(subjectId = featuredResearchField, predicateId = Predicates.hasSubfield, pageable = PageRequests.ALL)
        listOf(VisibilityFilter.FEATURED, VisibilityFilter.ALL_LISTED).forEach { visibility ->
            researchFieldService.findAllEntitiesBasedOnClassesByResearchField(
                id = featuredResearchField,
                classesList = listOf("Comparison"),
                visibility = visibility,
                includeSubFields = true,
                pageable = PageRequest.of(0, 5)
            ).forEach {
                fetchComparison(it.id)
            }
            researchFieldService.findAllEntitiesBasedOnClassesByResearchField(
                id = featuredResearchField,
                classesList = listOf("Visualization"),
                visibility = visibility,
                includeSubFields = true,
                pageable = PageRequest.of(0, 5)
            ).forEach {
                fetchVisualization(it.id)
            }
            researchFieldService.findAllEntitiesBasedOnClassesByResearchField(
                id = featuredResearchField,
                classesList = listOf("Paper"),
                visibility = visibility,
                includeSubFields = true,
                pageable = PageRequest.of(0, 5)
            ).forEach {
                statementService.findAll(subjectId = it.id, pageable = PageRequests.ALL)
            }
        }
    }

    private fun warmupComparisonsList() {
        resourceService.findAll(
            includeClasses = setOf(Classes.comparison),
            pageable = PageRequest.of(0, 15)
        ).forEach {
            fetchComparison(it.id)
        }
    }

    private fun warmupPaperList() {
        resourceService.findAll(
            includeClasses = setOf(Classes.paper),
            pageable = PageRequest.of(0, 25)
        ).forEach {
            statementService.findAll(subjectId = it.id, pageable = PageRequests.ALL)
        }
    }

    private fun warmupVisualizationsList() {
        resourceService.findAll(
            includeClasses = setOf(Classes.visualization),
            pageable = PageRequest.of(0, 10)
        ).forEach {
            fetchVisualization(it.id)
        }
    }

    private fun warmupReviewsList() {
        resourceService.findAll(
            includeClasses = setOf(Classes.smartReviewPublished),
            pageable = PageRequest.of(0, 25)
        ).forEach {
            fetchAssociatedPapers(it.id)
        }
    }

    private fun warmupListsList() {
        resourceService.findAll(
            includeClasses = setOf(Classes.literatureListPublished),
            pageable = PageRequest.of(0, 25)
        ).forEach {
            fetchAssociatedPapers(it.id)
        }
    }

    private fun fetchComparison(id: ThingId) {
        statementService.findAll(subjectId = id, pageable = PageRequests.ALL).forEach {
            val `object` = it.`object`
            if (`object` is Resource && (Classes.comparisonRelatedFigure in `object`.classes ||
                    it.predicate.id == Predicates.hasPreviousVersion)
            ) {
                statementService.findAll(subjectId = `object`.id, pageable = PageRequests.ALL)
            }
        }
    }

    private fun fetchVisualization(id: ThingId) {
        statementService.findAll(
            objectId = id,
            predicateId = Predicates.hasVisualization,
            pageable = PageRequest.of(0, 5)
        )
        statementService.findAll(
            subjectId = id,
            predicateId = Predicates.hasSubject,
            pageable = PageRequest.of(0, 5)
        )
    }

    private fun fetchAssociatedPapers(id: ThingId) {
        statementService.findAll(
            subjectId = id,
            predicateId = Predicates.hasPaper,
            pageable = PageRequest.of(0, 9999)
        ).forEach { hasPaperStatement ->
            val paper = hasPaperStatement.`object`
            if (paper is Resource) {
                statementService.findAll(subjectId = paper.id, pageable = PageRequests.ALL)
            }
        }
    }
}
