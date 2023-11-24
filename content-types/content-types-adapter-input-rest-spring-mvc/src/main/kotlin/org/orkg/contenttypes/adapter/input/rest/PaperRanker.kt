package org.orkg.contenttypes.adapter.input.rest

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.contenttypes.output.RankingService
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.ResourceRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val hasDoi = ThingId("P26")
private val hasContribution = ThingId("P31")
private val paperClass = ThingId("Paper")

@Component
@Profile("production")
class PaperRanker(
    @Autowired private val resourceRepository: ResourceRepository,
    @Autowired private val rankingService: RankingService,
    @Autowired private val listRepository: ListRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Value("\${orkg.ranking.papers.workers:#{10}}")
    private val workers: Int? = null

    @Value("\${orkg.ranking.papers.chunk-size:#{1000}}")
    private val chunkSize: Int? = null

    @Scheduled(cron = "\${orkg.ranking.papers.schedule}")
    fun run() {
        val startMillis = System.nanoTime()
        rankPapersAsync(workers!!) { paper, score ->
            if (paper.visibility == Visibility.UNLISTED && score.isSufficient) { // paper.unlistedBy is already checked by rankPapersAsync
                resourceRepository.save(paper.copy(
                    visibility = Visibility.DEFAULT,
                    unlistedBy = null
                ))
                logger.debug("Re-listing paper \"${paper.id}\".")
            } else if (paper.visibility == Visibility.DEFAULT && !score.isSufficient) {
                resourceRepository.save(paper.copy(
                    visibility = Visibility.UNLISTED,
                    unlistedBy = ContributorId.SYSTEM
                ))
                logger.debug("Unlisting paper \"${paper.id}\".")
            }
        }.thenRun {
            logger.info("Done ranking papers. Took ${(System.nanoTime() - startMillis).toMillis()}ms")
        }
    }

    private fun rankPapersAsync(
        workers: Int,
        block: (Resource, Score) -> Unit
    ): CompletableFuture<Void> {
        val total = resourceRepository.findAllByClass(paperClass, PageRequest.of(0, 1)).totalElements
        logger.info("Total papers: $total")
        val papersPerWorker = total / workers
        val workerThreads = (0 until workers).map { worker ->
            val start = papersPerWorker * worker
            val end = if (worker + 1 == workers) total else start + papersPerWorker
            CompletableFuture.runAsync {
                forEachPaper(start, end) { paper ->
                    if (paper.visibility == Visibility.UNLISTED && paper.unlistedBy != ContributorId.SYSTEM) return@forEachPaper
                    block(paper, rankPaper(paper))
                }
            }
        }
        return CompletableFuture.allOf(*workerThreads.toTypedArray())
    }

    private fun rankPaper(paper: Resource): Score {
        val stats = PaperStats()
        stats.hasTitle = paper.label.isNotBlank()
        stats.hasObservatory = paper.observatoryId != ObservatoryId.createUnknownObservatory()
        val contributions: MutableSet<ThingId> = mutableSetOf()
        val authors: MutableSet<ThingId> = mutableSetOf()
        rankingService.findAllStatementsAboutPaper(paper.id).forEach {
            when (it.first) {
                Predicates.hasAuthor -> authors += it.second
                Predicates.hasAuthors -> {
                    listRepository.findById(it.second).ifPresent { list ->
                        authors += list.elements
                    }
                }
                hasDoi -> stats.doi = it.second
                hasContribution -> contributions += it.second
            }
        }
        stats.authors = authors.size
        stats.contributions = contributions.size
        if (contributions.size > 0) {
            stats.properties = rankingService.countSumOfDistinctPredicatesForContributions(contributions)
        }
        stats.comparisons = rankingService.countComparisonsIncludingPaper(paper.id)
        stats.lists = rankingService.countLiteratureListsIncludingPaper(paper.id)
        return Score(stats)
    }

    private fun forEachPaper(start: Long, end: Long, action: (Resource) -> Unit) {
        var skip = start
        while (skip < end) {
            resourceRepository.findAllByClass(
                paperClass,
                PageRequest.of(
                    (skip / chunkSize!!).toInt(),
                    chunkSize.coerceAtMost((end - skip).toInt()),
                    Sort.by(Order.asc("id"))
                )
            ).forEach(action)
            skip += chunkSize
        }
    }

    class PaperStats {
        var hasTitle: Boolean = false
        var hasObservatory: Boolean = false
        var authors = 0
        var contributions = 0
        var properties: Long = 0
        var doi: ThingId? = null
        var comparisons: Long = 0
        var lists: Long = 0

        val hasDoi: Boolean
            get() = doi != null
    }

    data class Score private constructor(
        val isRejected: Boolean,
        val propertyScore: Double,
        val comparisonScore: Double,
        val listScore: Double,
        val doiScore: Int,
        val observatoryScore: Int
    ) {
        constructor(stats: PaperStats) : this(
            isRejected = !stats.hasTitle || stats.authors == 0 || stats.contributions == 0 || stats.properties == 0L,
            propertyScore = if (stats.properties > 0) (1 + log10(stats.properties.toDouble())) * 2 else 0.0,
            comparisonScore = if (stats.comparisons > 0) (1 + log10(stats.comparisons.toDouble())) * 2 else 0.0,
            listScore = if (stats.lists > 0) (1 + log10(stats.lists.toDouble())) else 0.0,
            doiScore = if (stats.hasDoi) 1 else 0,
            observatoryScore = if (stats.hasObservatory) 5 else 0
        )

        val isSufficient: Boolean = !isRejected

        val totalScore: Double
            get() = propertyScore + comparisonScore + listScore + doiScore + observatoryScore
    }
}

internal fun Long.toMillis() = TimeUnit.NANOSECONDS.toMillis(this)
