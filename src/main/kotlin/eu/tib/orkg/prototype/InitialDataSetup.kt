package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.configuration.OrkgConfiguration
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import java.io.FileReader
import java.net.URI
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("development", "docker")
class DataInitializer(
    private val classService: ClassService,
    private val predicateService: PredicateService
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    /**
     * Creating new classes and predicates only
     * if they don't exist
     */
    override fun run(args: ApplicationArguments?) {
        logger.info("Begin setting up initial data...")

        val fileParser =
            FileParser(FileReader(
                this::class.java.classLoader.getResource(OrkgConfiguration().Storage().InitialImportData().initialSetupFile).file))
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
            val classURI: URI? = URI.create(createClassCommand.uri) ?: null

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
