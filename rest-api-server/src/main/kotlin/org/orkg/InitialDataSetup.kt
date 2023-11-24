package org.orkg

import java.io.FileReader
import java.net.URI
import javax.validation.constraints.NotBlank
import org.orkg.common.ThingId
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreatePredicateUseCase
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
    private val classService: ClassUseCases,
    private val predicateService: CreatePredicateUseCase
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
        classList.forEach { createClassCommand ->
            val classURI: URI? = createClassCommand.uri?.let { URI.create(it) }

            classService.createIfNotExists(
                ThingId(createClassCommand.id),
                createClassCommand.label,
                classURI
            )
        }
    }

    /**
     * Create Predicates
     */
    private fun createPredicates(predicateList: List<CreatePredicatesCommand>) {
        predicateList.forEach { createPredicateCommand ->
            predicateService.createIfNotExists(
                ThingId(createPredicateCommand.id),
                createPredicateCommand.label)
        }
    }
}

data class CreateClassCommand(val id: String, val label: String, val uri: String?)
data class CreatePredicatesCommand(val id: String, val label: String)
data class CreateMainCommand(val classList: List<CreateClassCommand>, val predicateList: List<CreatePredicatesCommand>)
