package eu.tib.orkg.prototype

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigList
import java.io.Reader
import org.slf4j.LoggerFactory

/**
 * Class to read the file containing
 * classes and predicates
 */
class EntityConfigurationParser(private val reader: Reader) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    /**
     * Return a CreateMainCommand instance
     * comprising of a list of classes and
     * a list of predicates
     */
    fun parseInitialData(): CreateMainCommand {
        logger.info("Reading file content for initial data setup...")

        val fileContent = reader.readText()
        val configFactory = ConfigFactory.parseString(fileContent)

        val classes: ConfigList = configFactory.getList("classes")
        val predicates: ConfigList = configFactory.getList("predicates")

        return CreateMainCommand(
            createClassCommandList(classes),
            createPredicateCommandList(predicates)
        )
    }

    /**
     * Return a list of CreateClassCommand
     */
    @Suppress("UNCHECKED_CAST")
    private fun createClassCommandList(classes: ConfigList): List<CreateClassCommand> = classes.map {
        val classMap = it.unwrapped() as Map<String, String>

        val classId: String =
            classMap["id"] ?: throw Exception("A null value was found for id while importing classes")
        val classLabel: String =
            classMap["label"] ?: throw Exception("A null value was found for label while importing classes")

        CreateClassCommand(classId, classLabel, classMap["uri"])
    }

    /**
     * Return a list of CreatePredicatesCommand
     */
    @Suppress("UNCHECKED_CAST")
    private fun createPredicateCommandList(predicates: ConfigList): List<CreatePredicatesCommand> = predicates.map {
        val predicateMap = it.unwrapped() as Map<String, String>

        val predicateId: String =
            predicateMap["id"] ?: throw Exception("A null value was found for id while importing predicates")
        val predicateLabel: String =
            predicateMap["label"] ?: throw Exception("A null value was found for label while importing predicates")

        CreatePredicatesCommand(predicateId, predicateLabel)
    }
}
