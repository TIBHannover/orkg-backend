package org.orkg.dataimport.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ThingId
import org.orkg.common.deserializeToObject
import org.orkg.contenttypes.domain.Author
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.papers.ContributionStatement
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecord
import org.orkg.graph.domain.ExtractionMethod
import java.util.UUID

@Entity
@Table(name = "paper_csv_records")
class PaperCSVRecordEntity {
    @Id
    var id: UUID? = null

    @Column(name = "csv_id", nullable = false)
    var csvId: UUID? = null

    @Column(name = "item_number", nullable = false)
    var itemNumber: Long? = null

    @Column(name = "line_number", nullable = false)
    var lineNumber: Long? = null

    @Column(nullable = false)
    var title: String? = null

    @Column(nullable = false)
    var authors: ByteArray? = null

    @Column(name = "publication_month")
    var publicationMonth: Int? = null

    @Column(name = "publication_year")
    var publicationYear: Long? = null

    @Column(name = "published_in")
    var publishedIn: String? = null

    var url: String? = null

    var doi: String? = null

    @Column(name = "research_field_id")
    var researchFieldId: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "extraction_method", nullable = false)
    var extractionMethod: ExtractionMethod? = null

    @Column(nullable = false)
    var statements: ByteArray? = null

    fun toPaperCSVRecord() =
        PaperCSVRecord(
            id = id!!,
            csvId = CSVID(csvId!!),
            itemNumber = itemNumber!!,
            lineNumber = lineNumber!!,
            title = title!!,
            authors = authors?.deserializeToObject<List<Author>>().orEmpty(),
            publicationMonth = publicationMonth,
            publicationYear = publicationYear,
            publishedIn = publishedIn,
            url = url?.let(::ParsedIRI),
            doi = doi,
            researchFieldId = ThingId(researchFieldId!!),
            extractionMethod = extractionMethod!!,
            statements = statements?.deserializeToObject<Set<ContributionStatement>>().orEmpty(),
        )
}
