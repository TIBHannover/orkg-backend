package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.configuration.InputInjection
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.CreatePredicateUseCase
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import java.io.FileReader
import java.net.URI
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@ComponentScan("eu.tib.orkg.prototype.configuration")
@Profile("development", "docker")
class DataInitializer(
    private val classService: ClassUseCases,
    private val predicateService: CreatePredicateUseCase,
    private val config: InputInjection
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    /**
     * Creating new classes and predicates only
     * if they don't exist
     */
    override fun run(args: ApplicationArguments?) {
        logger.info("Begin setting up initial data...")

        val fileParser =
            EntityConfigurationParser(FileReader(
                this::class.java.classLoader.getResource(config.entitiesFile).file))
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
                ClassId(createClassCommand.id),
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
                PredicateId(createPredicateCommand.id),
                createPredicateCommand.label)
        }
    }
}

data class CreateClassCommand(val id: String, val label: String, val uri: String?)
data class CreatePredicatesCommand(val id: String, val label: String)
data class CreateMainCommand(val classList: List<CreateClassCommand>, val predicateList: List<CreatePredicatesCommand>)
