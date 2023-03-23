package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.export.rdf.api.ExportRDFUseCase
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private const val DEFAULT_FILE_NAME = "rdf-export-orkg.nt"

@Component
@Profile("production")
class RdfDumper(
    private val rdfService: ExportRDFUseCase
) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Value("\${orkg.rdf.dump.file-name:#{null}}")
    private val path: String? = null

    @Scheduled(cron = "\${orkg.rdf.dump.schedule}")
    fun dumpRdf() {
        logger.info("Starting rdf dump...")
        try {
            val filePath = resolveFilePath()
            val temp = Files.createTempFile("", "")
            OutputStreamWriter(FileOutputStream(temp.toFile()), Charsets.UTF_8).use {
                rdfService.dumpToNTriple(it)
            }
            if (temp.exists()) {
                Files.move(
                    temp,
                    filePath,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
            logger.info("Finished rdf dump")
        } catch (e: Exception) {
            logger.error("Error creating rdf dump", e)
        }
    }

    private fun resolveFilePath(): Path {
        if (path == null) {
            return Paths.get(DEFAULT_FILE_NAME)
        }
        val file = Paths.get(path)
        if (file.isDirectory()) {
            return file.resolve(DEFAULT_FILE_NAME)
        }
        return file
    }
}
