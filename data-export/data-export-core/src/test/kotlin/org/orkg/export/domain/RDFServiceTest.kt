package org.orkg.export.domain

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.OffsetDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.export.testing.fixtures.verifyThatDirectoryExistsAndIsEmpty
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Resource
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.PageImpl

internal class RdfServiceIntegrationTest : DescribeSpec({
    val statementRepository: StatementRepository = mockk()
    val classRepository: ClassRepository = mockk()
    val predicateRepository: PredicateRepository = mockk()
    val resourceRepository: ResourceRepository = mockk()
    val classHierarchyRepository: ClassHierarchyRepository = mockk()
    val fileExportService = FileExportService()
    val service = RDFService(
        statementRepository,
        predicateRepository,
        resourceRepository,
        classRepository,
        fileExportService,
        classHierarchyRepository
    )

    val targetDir = tempdir()

    every { classRepository.findAll(any()) } returns PageImpl(emptyList())
    every { predicateRepository.findAll(any()) } returns PageImpl(emptyList())
    every { resourceRepository.findAll(any()) } returns PageImpl(emptyList())
    every { statementRepository.findAll(any()) } returns PageImpl(emptyList())

    context("dumping to default location") {
        verifyThatDirectoryExistsAndIsEmpty(targetDir)

        val targetFile = targetDir.resolve("test-export.nt")
        targetFile.exists() shouldBe false

        every { resourceRepository.findAll(any()) } returns PageImpl(
            listOf(
                Resource(
                    id = ThingId("R1234"),
                    label = "some label",
                    createdAt = OffsetDateTime.parse("2023-03-27T13:50:57.438324+02:00"),
                    classes = setOf(ThingId("SomeClass")),
                    createdBy = ContributorId("98311ad3-9db5-47c3-963e-79729e00bee4"),
                    observatoryId = ObservatoryId("bcfa8bf8-c22d-4ee4-b817-5161b3f8595f"),
                    extractionMethod = ExtractionMethod.UNKNOWN,
                    organizationId = OrganizationId("aa88aa24-9649-4e60-92c8-b0fe7258fbef"),
                )
            )
        )
        withContext(Dispatchers.IO) {
            service.dumpToNTriple(targetFile.absolutePath)
        }

        it("writes the correct result") {
            targetFile.exists() shouldBe true
            targetFile.readText() shouldBe """
                |<http://orkg.org/orkg/resource/R1234> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://orkg.org/orkg/class/Resource> .
                |<http://orkg.org/orkg/resource/R1234> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://orkg.org/orkg/class/SomeClass> .
                |<http://orkg.org/orkg/resource/R1234> <http://www.w3.org/2000/01/rdf-schema#label> "some label"^^<http://www.w3.org/2001/XMLSchema#string> .
                |
            """.trimMargin()
        }
    }
}) {
    override fun isolationMode() = IsolationMode.InstancePerLeaf
}
