package org.orkg.dataimport.domain.csv.papers

import org.orkg.common.ContributorId
import org.orkg.common.DOI
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.contenttypes.input.CreateContributionCommandPart
import org.orkg.contenttypes.input.CreateContributionUseCase
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.PaperUseCases
import org.orkg.contenttypes.input.PublicationInfoCommand
import org.orkg.dataimport.domain.extractContributorId
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.SearchString
import org.slf4j.LoggerFactory
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.item.ItemProcessor
import java.util.Optional
import java.util.UUID

open class PaperCSVRecordProcessor(
    private val paperUseCases: PaperUseCases,
    private val contributionUseCases: ContributionUseCases,
) : ItemProcessor<PaperCSVRecord, PaperCSVRecordImportResult>,
    StepExecutionListener {
    private val logger = LoggerFactory.getLogger(this::class.java.name)
    private lateinit var contributorId: ContributorId

    override fun beforeStep(stepExecution: StepExecution) {
        contributorId = extractContributorId(stepExecution)
    }

    override fun process(item: PaperCSVRecord): PaperCSVRecordImportResult {
        val paperId = findPaperByTitleOrDOI(item.title, item.doi)
        val contribution = CreateContributionCommandPart(
            label = "Contribution",
            statements = item.statements
                .map {
                    val predicateId = it.predicate.fold(
                        leftMapper = { it.value },
                        rightMapper = { throw IllegalStateException("""Predicate with label "$it" was not created during CSV processing! This is a bug!""") }
                    )
                    predicateId to it.`object`.value!!
                }
                .groupBy({ it.first }, { CreateContributionCommandPart.StatementObject(it.second) })
        )
        if (paperId.isPresent) {
            logger.info("""Importing contribution for paper {} ({}) from CSV ({})""", paperId, item.title, item.csvId)
            val id = contributionUseCases.create(
                CreateContributionUseCase.CreateCommand(
                    contributorId = contributorId,
                    paperId = paperId.get(),
                    extractionMethod = item.extractionMethod,
                    contribution = contribution
                )
            )
            return PaperCSVRecordImportResult(
                id = UUID.randomUUID(),
                importedEntityId = id,
                importedEntityType = PaperCSVRecordImportResult.Type.CONTRIBUTION,
                csvId = item.csvId,
                itemNumber = item.itemNumber,
                lineNumber = item.lineNumber,
            )
        } else {
            logger.info("""Importing paper "{}" from CSV ({})""", item.title, item.csvId)
            val id = paperUseCases.create(
                CreatePaperUseCase.CreateCommand(
                    contributorId = contributorId,
                    title = item.title,
                    researchFields = listOf(item.researchFieldId),
                    identifiers = item.doi?.let { doi -> mapOf("doi" to listOf(doi)) }.orEmpty(),
                    publicationInfo = PublicationInfoCommand(
                        publishedMonth = item.publicationMonth,
                        publishedYear = item.publicationYear,
                        publishedIn = item.publishedIn,
                        url = item.url
                    ),
                    authors = item.authors,
                    sustainableDevelopmentGoals = emptySet(),
                    mentionings = emptySet(),
                    observatories = emptyList(),
                    organizations = emptyList(),
                    contents = CreatePaperUseCase.CreateCommand.PaperContents(contributions = listOf(contribution)),
                    extractionMethod = item.extractionMethod,
                )
            )
            return PaperCSVRecordImportResult(
                id = UUID.randomUUID(),
                importedEntityId = id,
                importedEntityType = PaperCSVRecordImportResult.Type.PAPER,
                csvId = item.csvId,
                itemNumber = item.itemNumber,
                lineNumber = item.lineNumber,
            )
        }
    }

    private fun findPaperByTitleOrDOI(title: String, doi: String?): Optional<ThingId> {
        if (doi != null) {
            val result = paperUseCases.existsByDOI(DOI.of(doi))
            if (result.isPresent) {
                return result
            }
        }
        return paperUseCases.existsByTitle(SearchString.of(title, true) as ExactSearchString)
    }
}
