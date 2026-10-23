package org.orkg.dataimport.domain.testing.fixtures

import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.testing.fixtures.Assets.turtle
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.rdf.RdfClass
import org.orkg.dataimport.domain.rdf.RdfFile
import org.orkg.dataimport.domain.rdf.RdfFile.Format
import org.orkg.dataimport.domain.rdf.RdfFile.State
import org.orkg.dataimport.domain.rdf.RdfFileImportResult
import org.orkg.dataimport.domain.rdf.RdfPredicate
import org.orkg.dataimport.domain.rdf.RdfResource
import org.orkg.dataimport.domain.rdf.RdfTriple
import org.orkg.dataimport.domain.rdf.ResourceToTypeMapping
import java.time.OffsetDateTime
import java.util.UUID

fun createRdfFile(
    id: UUID = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837"),
    name: String = "aliceKnowsBob.ttl",
    baseUri: IRI = IRI.create("http://example.org"),
    format: Format = Format.TURTLE,
    state: State = State.UPLOADED,
    validationJobId: JobId? = null,
    importJobId: JobId? = null,
    data: String = turtle("aliceKnowsBob"),
    createdBy: ContributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    createdAt: OffsetDateTime = OffsetDateTime.parse("2024-04-30T16:22:58.959539600+02:00"),
) = RdfFile(id, name, format, baseUri, state, validationJobId, importJobId, data, createdBy, createdAt)

fun createRdfClass(
    id: UUID = UUID.fromString("5c4062d7-73c1-4bee-ad9f-5ec819bc77b2"),
    rdfFileId: UUID = UUID.fromString("4fc6976b-770b-49c0-ba64-4bf2b4201315"),
    iri: org.eclipse.rdf4j.model.IRI = SimpleValueFactory.getInstance().createIRI("https://example.com/class"),
    label: String? = "class",
): RdfClass =
    RdfClass(id, rdfFileId, iri, label)

fun createRdfPredicate(
    id: UUID = UUID.fromString("4da6c806-d65e-4b24-b63a-35e1734fc764"),
    rdfFileId: UUID = UUID.fromString("4fc6976b-770b-49c0-ba64-4bf2b4201315"),
    iri: org.eclipse.rdf4j.model.IRI = SimpleValueFactory.getInstance().createIRI("https://example.com/predicate"),
    label: String? = "predicate",
): RdfPredicate =
    RdfPredicate(id, rdfFileId, iri, label)

fun createRdfResource(
    id: UUID = UUID.fromString("a17683f0-11f6-4552-a099-cb10e4104af2"),
    rdfFileId: UUID = UUID.fromString("4fc6976b-770b-49c0-ba64-4bf2b4201315"),
    iri: Resource = SimpleValueFactory.getInstance().createIRI("https://example.com/resource"),
    label: String? = "resource",
    classes: Set<org.eclipse.rdf4j.model.IRI> = emptySet(),
): RdfResource =
    RdfResource(id, rdfFileId, iri, label, classes)

fun createRdfTriple(
    id: UUID = UUID.fromString("f50b4c66-a48f-4c5b-9076-a9c5d68fb71d"),
    rdfFileId: UUID = UUID.fromString("4fc6976b-770b-49c0-ba64-4bf2b4201315"),
    subject: Resource = SimpleValueFactory.getInstance().createIRI("https://example.com/subject"),
    predicate: org.eclipse.rdf4j.model.IRI = SimpleValueFactory.getInstance().createIRI("https://example.com/predicate"),
    `object`: Value = SimpleValueFactory.getInstance().createIRI("https://example.com/object"),
): RdfTriple =
    RdfTriple(id, rdfFileId, subject, predicate, `object`)

fun createResourceToTypeMapping(
    iri: IRI = IRI.create("https://example.com/"),
    type: ResourceToTypeMapping.OrkgEntityType = ResourceToTypeMapping.OrkgEntityType.RESOURCE,
): ResourceToTypeMapping =
    ResourceToTypeMapping(iri, type)

fun createRdfFileImportResult(
    classCount: Int = 1,
    predicateCount: Int = 2,
    resourceCount: Int = 3,
    statementCount: Int = 4,
): RdfFileImportResult =
    RdfFileImportResult(classCount, predicateCount, resourceCount, statementCount)
