package org.orkg.dataimport.domain.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.Assets.csv
import org.orkg.contenttypes.domain.Author
import org.orkg.dataimport.domain.TypedValue
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSV.Format
import org.orkg.dataimport.domain.csv.CSV.State
import org.orkg.dataimport.domain.csv.CSV.Type
import org.orkg.dataimport.domain.csv.CSVHeader
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.TypedCSVRecord
import org.orkg.dataimport.domain.csv.papers.ContributionStatement
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecord
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordImportResult
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import java.time.OffsetDateTime
import java.util.UUID

fun createCSV() = CSV(
    id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837"),
    name = "papers.csv",
    type = Type.PAPER,
    format = Format.DEFAULT,
    state = State.UPLOADED,
    validationJobId = null,
    importJobId = null,
    data = csv("papers"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    createdAt = OffsetDateTime.parse("2024-04-30T16:22:58.959539600+02:00"),
)

fun createPaperCSVRecord() = PaperCSVRecord(
    id = UUID.fromString("cf871b4d-aa1a-4e62-b73a-1ff44e7c5a6f"),
    csvId = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837"),
    itemNumber = 1,
    lineNumber = 2,
    title = "Paper Title",
    authors = listOf(
        Author("Josiah Stinkney Carberry"),
        Author("Author 2"),
    ),
    publicationMonth = 4,
    publicationYear = 2023,
    publishedIn = "Fancy Conference",
    url = ParsedIRI("https://example.org"),
    doi = "10.1000/182",
    researchFieldId = ThingId("R456"),
    extractionMethod = ExtractionMethod.MANUAL,
    statements = setOf(
        ContributionStatement(
            predicate = Either.left(Predicates.employs),
            `object` = TypedValue(namespace = "resource", value = "DOI", type = Classes.resource)
        ),
        ContributionStatement(
            predicate = Either.right("result"),
            `object` = TypedValue(namespace = "resource", value = "Result", type = Classes.resource)
        )
    )
)

fun createPaperCSVRecordImportResult(
    id: UUID = UUID.fromString("e7de95a8-d1f5-4837-9a1f-a2eb8b45a254"),
    importedEntityId: ThingId = ThingId("R123"),
    importedEntityType: PaperCSVRecordImportResult.Type = PaperCSVRecordImportResult.Type.PAPER,
    csvId: CSVID = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837"),
    itemNumber: Long = 1,
    lineNumber: Long = 2,
) = PaperCSVRecordImportResult(id, importedEntityId, importedEntityType, csvId, itemNumber, lineNumber)

fun createPaperCSVHeaders() = listOf(
    CSVHeader(column = 1, name = "title", namespace = "paper", columnType = Classes.string),
    CSVHeader(column = 2, name = "doi", namespace = "paper", columnType = Classes.string),
    CSVHeader(column = 3, name = "authors", namespace = "paper", columnType = Classes.string),
    CSVHeader(column = 4, name = "publication_month", namespace = "paper", columnType = Classes.integer),
    CSVHeader(column = 5, name = "publication_year", namespace = "paper", columnType = Classes.integer),
    CSVHeader(column = 6, name = "research_field", namespace = "paper", columnType = Classes.researchField),
    CSVHeader(column = 7, name = "url", namespace = "paper", columnType = Classes.uri),
    CSVHeader(column = 8, name = "published_in", namespace = "paper", columnType = Classes.venue),
    CSVHeader(column = 9, name = "research_problem", namespace = "contribution", columnType = Classes.problem),
    CSVHeader(column = 10, name = "extraction_method", namespace = "contribution", columnType = Classes.string),
    CSVHeader(column = 11, name = "category", namespace = "resource", columnType = Classes.resource),
    CSVHeader(column = 12, name = "P2", namespace = "orkg", columnType = null),
    CSVHeader(column = 13, name = "result", namespace = null, columnType = null),
    CSVHeader(column = 14, name = "numericValue", namespace = null, columnType = Classes.integer),
    CSVHeader(column = 15, name = "description", namespace = "orkg", columnType = Classes.string),
)

fun createTypedCSVRecord() = TypedCSVRecord(
    id = UUID.fromString("e7de95a8-d1f5-4837-9a1f-a2eb8b45a254"),
    csvId = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837"),
    itemNumber = 1,
    lineNumber = 2,
    values = listOf(
        TypedValue(namespace = null, value = "Dummy Paper Title", type = Classes.string),
        TypedValue(namespace = null, value = "10.1000/182", type = Classes.string),
        TypedValue(namespace = null, value = "Josiah Stinkney Carberry;Author 2", type = Classes.string),
        TypedValue(namespace = null, value = "4", type = Classes.integer),
        TypedValue(namespace = null, value = "2023", type = Classes.integer),
        TypedValue(namespace = null, value = "R456", type = Classes.researchField),
        TypedValue(namespace = null, value = "https://example.org", type = Classes.uri),
        TypedValue(namespace = null, value = "Fancy Conference", type = Classes.venue),
        TypedValue(namespace = null, value = "Complicated Research Problem", type = Classes.problem),
        TypedValue(namespace = null, value = "MANUAL", type = Classes.string),
        TypedValue(namespace = null, value = "Handbook", type = Classes.resource),
        TypedValue(namespace = "resource", value = "DOI", type = Classes.resource),
        TypedValue(namespace = "resource", value = "Result", type = Classes.resource),
        TypedValue(namespace = null, value = "5", type = Classes.integer),
        TypedValue(namespace = null, value = "DOI Handbook", type = Classes.string)
    )
)

fun createCSVRecord() = listOf(
    "Dummy Paper Title",
    "10.1000/182",
    "Josiah Stinkney Carberry;Author 2",
    "4",
    "2023",
    "R456",
    "https://example.org",
    "Fancy Conference",
    "Complicated Research Problem",
    "MANUAL",
    "Handbook",
    "resource:DOI",
    "resource:Result",
    "5",
    "DOI Handbook",
)
