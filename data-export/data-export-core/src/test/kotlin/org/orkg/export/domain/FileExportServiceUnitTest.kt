package org.orkg.export.domain

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission.GROUP_READ
import java.nio.file.attribute.PosixFilePermission.OTHERS_READ
import java.nio.file.attribute.PosixFilePermission.OWNER_READ
import java.nio.file.attribute.PosixFilePermission.OWNER_WRITE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orkg.export.testing.fixtures.verifyThatDirectoryExistsAndIsEmpty

internal class FileExportServiceUnitTest : DescribeSpec({
    val service = FileExportService()

    val tmpDir = tempdir()
    val targetDir = tempdir()

    context("resolving the path") {
        val defaultPath = "rdf-export-orkg.nt"
        context("path is not given") {
            val result = withContext(Dispatchers.IO) {
                service.resolveFilePath(null, defaultPath)
            }
            it("returns a path with the default filename") {
                result shouldBe Path.of(defaultPath)
            }
        }
        context("path is given") {
            verifyThatDirectoryExistsAndIsEmpty(targetDir)
            context("path is a directory") {
                val result = withContext(Dispatchers.IO) {
                    service.resolveFilePath(targetDir.absolutePath, defaultPath)
                }
                it("returns the given path with the default filename") {
                    result shouldBe Path.of("${targetDir.path}/rdf-export-orkg.nt")
                }
            }
            context("path is not a directory") {
                val subdir = targetDir.resolve("subdir")
                val filename = subdir.resolve("some--other_filename.txt").path
                val result = withContext(Dispatchers.IO) {
                    service.resolveFilePath(filename, defaultPath)
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
            val expectedContent = "test content"

            withContext(Dispatchers.IO) {
                service.writeToFile(null, targetFile.absolutePath) { writer ->
                    writer.write(expectedContent)
                }
            }

            it("should dump successfully") {
                targetFile.exists() shouldBe true
                if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
                    val view = Files.getFileAttributeView(targetFile.toPath(), PosixFileAttributeView::class.java)
                    view.readAttributes().permissions() shouldBe setOf(OWNER_WRITE, OWNER_READ, GROUP_READ, OTHERS_READ)
                }
            }
            it("contains the expected output") {
                targetFile.readText() shouldBe expectedContent
            }
        }
        context("target file already exists") {
            verifyThatDirectoryExistsAndIsEmpty(tmpDir)
            verifyThatDirectoryExistsAndIsEmpty(targetDir)

            val targetFile = targetDir.resolve("test-export.nt")
            targetFile.writer().use {
                it.write("Old Content")
            }
            targetFile.exists() shouldBe true
            val expectedContent = ""

            service.writeToFile(null, targetFile.absolutePath) { writer ->
                writer.write(expectedContent)
            }

            it("should overwrite existing content") {
                targetFile.exists() shouldBe true
                targetFile.length() shouldBe 0
            }
            it("contains the expected output") {
                targetFile.readText() shouldBe expectedContent
            }
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
            val defaultPath = "test-export.nt"

            it("should fail indicating that the parent directory does not exist") {
                val exception = shouldThrowExactly<NoSuchFileException> {
                    withContext(Dispatchers.IO) {
                        service.writeToFile(filename.absolutePath, defaultPath) { writer ->
                            writer.write("test content")
                        }
                    }
                }
                exception.message shouldBe "$filename: The directory ${filename.parent} does not exist! Make sure it was created, and that permissions are correct."
            }
        }
    }
}) {
    override fun isolationMode() = IsolationMode.InstancePerLeaf
}
