package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase.*
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResearchFieldRepository {
    fun findById(id: ThingId): Optional<Resource>
    fun getResearchProblemsOfField(fieldId: ThingId, pageable: Pageable): Page<PaperCountPerResearchProblem>
    fun getContributorIdsFromResearchFieldAndIncludeSubfields(id: ThingId, pageable: Pageable): Page<ContributorId>
    fun getContributorIdsExcludingSubFields(id: ThingId, pageable: Pageable): Page<ContributorId>
    fun findResearchFieldsWithBenchmarks(pageable: Pageable): Page<Resource>

    fun findAllListedPapersByResearchField(
        id: ThingId,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllPapersByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllListedComparisonsByResearchField(
        id: ThingId,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllComparisonsByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllListedProblemsByResearchField(
        id: ThingId,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllProblemsByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllListedVisualizationsByResearchField(
        id: ThingId,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllVisualizationsByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllListedSmartReviewsByResearchField(
        id: ThingId,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllSmartReviewsByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllListedLiteratureListsByResearchField(
        id: ThingId,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>

    fun findAllLiteratureListsByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean = false,
        pageable: Pageable
    ): Page<Resource>
}
