package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase
import eu.tib.orkg.prototype.statements.api.RetrieveStatisticsUseCase
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
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
    private val statsService: RetrieveStatisticsUseCase,
    private val researchFieldService: RetrieveResearchFieldUseCase,
    private val flags: FeatureFlagService
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun run(args: ApplicationArguments?) {
        if (!flags.isCacheWarmupEnabled()) {
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
        statementService.findAllBySubjectAndPredicate(featuredResearchField, ThingId("P36"), PageRequest.of(0, 9999))
        listOf(true, false).forEach { featured ->
            researchFieldService.getEntitiesBasedOnClassesIncludingSubfields(
                id = ThingId(featuredResearchField),
                classesList = listOf("Comparison"),
                featured = featured,
                unlisted = false,
                pageable = PageRequest.of(0, 5)
            ).forEach {
                fetchComparison(it.id)
            }
            researchFieldService.getEntitiesBasedOnClassesIncludingSubfields(
                id = ThingId(featuredResearchField),
                classesList = listOf("Visualization"),
                featured = featured,
                unlisted = false,
                pageable = PageRequest.of(0, 5)
            ).forEach {
                fetchVisualization(it.id)
            }
            researchFieldService.getEntitiesBasedOnClassesIncludingSubfields(
                id = ThingId(featuredResearchField),
                classesList = listOf("Paper"),
                featured = featured,
                unlisted = false,
                pageable = PageRequest.of(0, 5)
            ).forEach {
                statementService.findAllBySubject(it.id.value, PageRequest.of(0, 9999))
            }
        }
    }

    private fun warmupComparisonsList() {
        resourceService.findAllByClass(PageRequest.of(0, 15), ThingId("Comparison")).forEach {
            fetchComparison(it.id)
        }
    }

    private fun warmupPaperList() {
        resourceService.findAllByClass(PageRequest.of(0, 25), ThingId("Paper")).forEach {
            statementService.findAllBySubject(it.id.value, PageRequest.of(0, 9999))
        }
    }

    private fun warmupVisualizationsList() {
        resourceService.findAllByClass(PageRequest.of(0, 10), ThingId("Visualization")).forEach {
            fetchVisualization(it.id)
        }
    }

    private fun warmupReviewsList() {
        resourceService.findAllByClass(PageRequest.of(0, 25), ThingId("SmartReviewPublished")).forEach {
            fetchAssociatedPapers(it.id)
        }
    }

    private fun warmupListsList() {
        resourceService.findAllByClass(PageRequest.of(0, 25), ThingId("LiteratureListPublished")).forEach {
            fetchAssociatedPapers(it.id)
        }
    }

    private fun fetchComparison(id: ThingId) {
        statementService.findAllBySubject(id.value, PageRequest.of(0, 9999)).forEach {
            val `object` = it.`object`
            if (`object` is ResourceRepresentation && (ThingId("ComparisonRelatedFigure") in `object`.classes ||
                    it.predicate.id == ThingId("hasPreviousVersion"))
            ) {
                statementService.findAllBySubject(`object`.id.value, PageRequest.of(0, 9999))
            }
        }
    }

    private fun fetchVisualization(id: ThingId) {
        statementService.findAllByObjectAndPredicate(
            objectId = id.value,
            predicateId = ThingId("hasVisualization"),
            pagination = PageRequest.of(0, 5)
        )
        statementService.findAllBySubjectAndPredicate(
            subjectId = id.value,
            predicateId = ThingId("hasSubject"),
            pagination = PageRequest.of(0, 5)
        )
    }

    private fun fetchAssociatedPapers(id: ThingId) {
        statementService.findAllBySubjectAndPredicate(
            subjectId = id.value,
            predicateId = ThingId("HasPaper"),
            pagination = PageRequest.of(0, 9999)
        ).forEach { hasPaperStatement ->
            val paper = hasPaperStatement.`object`
            if (paper is ResourceRepresentation) {
                statementService.findAllBySubject(paper.id.value, PageRequest.of(0, 9999))
            }
        }
    }
}
