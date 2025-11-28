package org.orkg.contenttypes.input.testing.fixtures

import org.orkg.common.DOI
import org.orkg.common.GoogleScholarId
import org.orkg.common.Handle
import org.orkg.common.ISBN
import org.orkg.common.ISSN
import org.orkg.common.LinkedInId
import org.orkg.common.ORCID
import org.orkg.common.OpenAlexId
import org.orkg.common.ResearchGateId
import org.orkg.common.ResearcherId
import org.orkg.common.WikidataId
import org.orkg.common.testing.fixtures.doiConstraint
import org.orkg.common.testing.fixtures.googleScholarIdConstraint
import org.orkg.common.testing.fixtures.handleConstraint
import org.orkg.common.testing.fixtures.isbnConstraint
import org.orkg.common.testing.fixtures.issnConstraint
import org.orkg.common.testing.fixtures.linkedInIdConstraint
import org.orkg.common.testing.fixtures.openAlexIdConstraint
import org.orkg.common.testing.fixtures.orcidConstraint
import org.orkg.common.testing.fixtures.researchGateIdConstraint
import org.orkg.common.testing.fixtures.researcherIdConstraint
import org.orkg.common.testing.fixtures.wikidataIdConstraint
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.testing.spring.restdocs.DocumentationContextProvider
import org.springframework.boot.test.context.TestComponent
import org.springframework.restdocs.constraints.Constraint
import kotlin.reflect.KClass

@TestComponent
class ContentTypeDocumentationContextProvider : DocumentationContextProvider {
    override val typeMappings: Map<KClass<*>, String> get() = mapOf(
        SnapshotId::class to "string",
        DOI::class to "string",
        GoogleScholarId::class to "string",
        Handle::class to "string",
        ISBN::class to "string",
        ISSN::class to "string",
        LinkedInId::class to "string",
        OpenAlexId::class to "string",
        ORCID::class to "string",
        ResearcherId::class to "string",
        ResearchGateId::class to "string",
        WikidataId::class to "string",
    )

    override fun applyConstraints(constraints: MutableList<Constraint>, type: KClass<*>) {
        when (type) {
            DOI::class -> constraints.add(doiConstraint)
            GoogleScholarId::class -> constraints.add(googleScholarIdConstraint)
            Handle::class -> constraints.add(handleConstraint)
            ISBN::class -> constraints.add(isbnConstraint)
            ISSN::class -> constraints.add(issnConstraint)
            LinkedInId::class -> constraints.add(linkedInIdConstraint)
            OpenAlexId::class -> constraints.add(openAlexIdConstraint)
            ORCID::class -> constraints.add(orcidConstraint)
            ResearcherId::class -> constraints.add(researcherIdConstraint)
            ResearchGateId::class -> constraints.add(researchGateIdConstraint)
            WikidataId::class -> constraints.add(wikidataIdConstraint)
        }
    }
}
