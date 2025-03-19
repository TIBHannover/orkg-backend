package org.orkg.migrations.neo4j

import ac.simons.neo4j.migrations.core.JavaBasedMigration
import ac.simons.neo4j.migrations.core.MigrationContext
import org.springframework.core.io.ClassPathResource
import java.lang.Thread.sleep
import java.net.URI
import java.util.Optional

abstract class AbstractCustomProcedureMigration(
    private val migrationQueryFile: String,
    private val validationQuery: String,
    private val validationIntervalMs: Long = 100,
) : JavaBasedMigration {
    private val migrationQuery: String by lazy { loadQuery() }

    override fun apply(context: MigrationContext) {
        val systemDBContext = context.getSessionConfig { it.withDatabase("system") }
        context.driver.session(systemDBContext).executeWriteWithoutResult { tx ->
            tx.run(migrationQuery)
        }
        val orkgDBContext = context.getSessionConfig { it.withDatabase("orkg") }
        val validate = {
            try {
                context.driver.session(orkgDBContext).executeRead { tx ->
                    tx.run(validationQuery).list().size
                }
            } catch (e: Exception) {
                -1
            }
        }
        var result = validate()
        while (result == -1) {
            sleep(validationIntervalMs)
            result = validate()
        }
    }

    override fun getOptionalDescription(): Optional<String> = Optional.ofNullable(
        this::class.simpleName
            ?.replace(Regex("""^[RV]\d+__"""), "")
            ?.replace(Regex("""([A-Z])""")) { " " + it.value.lowercase() }
            ?.trim()
    )

    override fun getChecksum(): Optional<String> = Optional.of(migrationQuery.hashCode().toString())

    private fun loadQuery(): String =
        ClassPathResource(URI.create("classpath:/neo4j/migrations-system/$migrationQueryFile.cypher").path)
            .inputStream.use { String(it.readBytes()) }
}
