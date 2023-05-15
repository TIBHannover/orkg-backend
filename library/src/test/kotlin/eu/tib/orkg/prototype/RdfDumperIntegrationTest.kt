package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.export.rdf.domain.RDFService
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.testing.kotest.LinuxOnly
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestScope
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission.GROUP_READ
import java.nio.file.attribute.PosixFilePermission.OTHERS_READ
import java.nio.file.attribute.PosixFilePermission.OWNER_READ
import java.nio.file.attribute.PosixFilePermission.OWNER_WRITE
import java.time.OffsetDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.PageImpl

@EnabledIf(LinuxOnly::class)
internal class RdfServiceIntegrationTest : DescribeSpec({

    val statementRepository: StatementRepository = mockk()
    val classRepository: ClassRepository = mockk()
    val predicateRepository: PredicateRepository = mockk()
    val resourceRepository: ResourceRepository = mockk()

    val service = RDFService(
        statementRepository,
        predicateRepository,
        resourceRepository,
        classRepository,
    )

    val tmpDir = tempdir()
    val targetDir = tempdir()

    every { classRepository.findAll(any()) } returns PageImpl(emptyList())
    every { predicateRepository.findAll(any()) } returns PageImpl(emptyList())
    every { resourceRepository.findAll(any()) } returns PageImpl(emptyList())
    every { statementRepository.findAll(any()) } returns PageImpl(emptyList())

    context("resolving the path") {
        context("path is not given") {
            val result = withContext(Dispatchers.IO) {
                service.resolveFilePath(null)
            }
            it("returns a path with the default filename") {
                result shouldBe Path.of("rdf-export-orkg.nt")
            }
        }
        context("path is given") {
            verifyThatDirectoryExistsAndIsEmpty(targetDir)
            context("path is a directory") {
                val result = withContext(Dispatchers.IO) {
                    service.resolveFilePath(targetDir.absolutePath)
                }
                it("returns the given path with the default filename") {
                    result shouldBe Path.of("${targetDir.path}/rdf-export-orkg.nt")
                }
            }
            context("path is not a directory") {
                val subdir = targetDir.resolve("subdir")
                val filename = subdir.resolve("some--other_filename.txt").path
                val result = withContext(Dispatchers.IO) {
                    service.resolveFilePath(filename)
                }
                it("returns the given path") {
                    result shouldBe Path.of("${targetDir.path}/subdir/some--other_filename.txt")
                }
            }
        }
    }
    context("exporting to the default location") {
        context("target file does not exist") {
            verifyThatDirectoryExistsAndIsEmpty(tmpDir)
            verifyThatDirectoryExistsAndIsEmpty(targetDir)

            val targetFile = targetDir.resolve("test-export.nt")
            targetFile.exists() shouldBe false

            it("should dump successfully") {
                withContext(Dispatchers.IO) {
                    service.dumpToNTriple(targetFile.absolutePath)
                }
                targetFile.exists() shouldBe true
                val view = Files.getFileAttributeView(targetFile.toPath(), PosixFileAttributeView::class.java)
                view.readAttributes().permissions() shouldBe setOf(OWNER_WRITE, OWNER_READ, GROUP_READ, OTHERS_READ)
            }
            it("contains the expected output", verifyOutput(targetFile, service, resourceRepository))
        }
        context("target file already exists") {
            verifyThatDirectoryExistsAndIsEmpty(tmpDir)
            verifyThatDirectoryExistsAndIsEmpty(targetDir)

            val targetFile = targetDir.resolve("test-export.nt")
            targetFile.writer().use {
                it.write("Old Content")
            }
            targetFile.exists() shouldBe true

            it("should overwrite exising content") {
                withContext(Dispatchers.IO) {
                    service.dumpToNTriple(targetFile.absolutePath)
                }
                targetFile.exists() shouldBe true
                targetFile.length() shouldBe 0
            }
            it("contains the expected output", verifyOutput(targetFile, service, resourceRepository))
        }
    }
    context("exporting to a custom location") {
        context("target directory does not exist") {
            verifyThatDirectoryExistsAndIsEmpty(tmpDir)
            verifyThatDirectoryExistsAndIsEmpty(targetDir)

            val subdir = targetDir.resolve("subdir")
            subdir.exists() shouldBe false
            val filename = subdir.resolve("my-output.nt")
            filename.exists() shouldBe false

            it("should fail indicating that the parent directory does not exist") {
                val exception = shouldThrowExactly<NoSuchFileException> {
                    withContext(Dispatchers.IO) {
                        service.dumpToNTriple(filename.absolutePath)
                    }
                }
                exception.message shouldBe "$filename: The directory ${filename.parent} does not exist! Make sure it was created, and that permissions are correct."
            }
        }
    }
}) {
    override fun isolationMode() = IsolationMode.InstancePerLeaf
}

private fun verifyThatDirectoryExistsAndIsEmpty(dir: File): Unit = with(dir) {
    isDirectory shouldBe true
    exists() shouldBe true
    listFiles() shouldBe emptyArray()
}

private fun verifyOutput(
    targetFile: File,
    service: RDFService,
    resourceRepository: ResourceRepository
): suspend TestScope.() -> Unit = {
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
    targetFile.readText() shouldBe """
                |<http://orkg.org/orkg/resource/R1234> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://orkg.org/orkg/class/Resource> .
                |<http://orkg.org/orkg/resource/R1234> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://orkg.org/orkg/class/SomeClass> .
                |<http://orkg.org/orkg/resource/R1234> <http://www.w3.org/2000/01/rdf-schema#label> "some label"^^<http://www.w3.org/2001/XMLSchema#string> .
                |
            """.trimMargin()
}
