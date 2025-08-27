package org.orkg.dataimport.domain.csv.papers

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.graph.domain.ExtractionMethod
import java.io.Serial
import java.io.Serializable
import java.util.UUID

data class PaperCSVRecord(
    val id: UUID,
    val csvId: CSVID,
    val itemNumber: Long,
    val lineNumber: Long,
    val title: String,
    val authors: List<Author>,
    val publicationMonth: Int?,
    val publicationYear: Long?,
    val publishedIn: String?,
    val url: ParsedIRI?,
    val doi: String?,
    val researchFieldId: ThingId,
    val extractionMethod: ExtractionMethod,
    val statements: Set<ContributionStatement>,
) : Serializable {
    init {
        require(authors is Serializable)
        require(statements is Serializable)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4718879291625450618L
    }
}
