package org.orkg

import java.io.FileReader
import java.net.URI
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import javax.validation.constraints.NotBlank
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Predicate
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@ComponentScan("org.orkg.configuration")
@Profile("development", "docker")
class DataInitializer(
    private val classRepository: ClassRepository,
    private val predicateRepository: PredicateRepository,
    private val clock: Clock
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Value("\${orkg.init.setup.entities-file}")
    @NotBlank
    private var entitiesFile: String? = null

    /**
     * Creating new classes and predicates only
     * if they don't exist
     */
    override fun run(args: ApplicationArguments?) {
        logger.info("Begin setting up initial data...")

        val fileParser = EntityConfigurationParser(
            FileReader(this::class.java.classLoader.getResource(entitiesFile)!!.file)
        )
        val mainCommand: CreateMainCommand = fileParser.parseInitialData()

        createClasses(mainCommand.classList)
        createPredicates(mainCommand.predicateList)

        logger.info("End of initial data setup...")
    }

    /**
     * Create Classes
     */
    private fun createClasses(classList: List<CreateClassCommand>) {
        classList.forEach { command ->
            val id = ThingId(command.id)
            val classByURI = command.uri?.let { classRepository.findByUri(it) } ?: Optional.empty()
            val classById = classRepository.findById(id)

            if (classById.isPresent && classByURI.isPresent && classById.get().id != classByURI.get().id)
                throw Exception("ID mismatch for class ID: ${classById.get().id}")

            if (classById.isEmpty) {
                classRepository.save(
                    Class(
                        id = id,
                        label = command.label,
                        uri = command.uri?.let(URI::create),
                        createdAt = OffsetDateTime.now(clock)
                    )
                )
            }
        }
    }

    /**
     * Create Predicates
     */
    private fun createPredicates(predicateList: List<CreatePredicatesCommand>) {
        predicateList.forEach { (id, label) ->
            val thingId = ThingId(id)
            if (predicateRepository.findById(thingId).isEmpty) {
                predicateRepository.save(
                    Predicate(
                        id = thingId,
                        label = label,
                        createdAt = OffsetDateTime.now(clock)
                    )
                )
            }
        }
    }
}

data class CreateClassCommand(val id: String, val label: String, val uri: String?)
data class CreatePredicatesCommand(val id: String, val label: String)
data class CreateMainCommand(val classList: List<CreateClassCommand>, val predicateList: List<CreatePredicatesCommand>)
