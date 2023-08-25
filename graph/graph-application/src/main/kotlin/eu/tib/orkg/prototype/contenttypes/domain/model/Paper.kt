package eu.tib.orkg.prototype.contenttypes.domain.model

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.services.objectIdsAndLabel
import eu.tib.orkg.prototype.contenttypes.services.wherePredicate
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.time.OffsetDateTime

typealias Contributions = List<ObjectIdAndLabel>

data class Paper(
    val id: ThingId,
    val title: String,
    val researchFields: List<ObjectIdAndLabel>,
    val identifiers: Map<String, String>,
    val publicationInfo: PublicationInfo,
    val authors: List<Author>,
    val contributions: Contributions,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val extractionMethod: ExtractionMethod,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val verified: Boolean,
    val visibility: Visibility
)

internal fun Iterable<GeneralStatement>.toContributions(): Contributions =
    this.wherePredicate(Predicates.hasContribution).objectIdsAndLabel().sortedBy { it.id }
