package org.orkg.migrations.neo4j

import ac.simons.neo4j.migrations.core.JavaBasedMigration
import ac.simons.neo4j.migrations.core.MigrationContext
import java.lang.Thread.sleep
import java.util.*

abstract class AbstractCustomProcedureMigration(
    private val migrationQuery: String,
    private val validationQuery: String,
    private val version: Long,
    private val validationIntervalMs: Long = 100,
) : JavaBasedMigration {
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

    override fun getChecksum(): Optional<String> = Optional.of(version.toString())
}
