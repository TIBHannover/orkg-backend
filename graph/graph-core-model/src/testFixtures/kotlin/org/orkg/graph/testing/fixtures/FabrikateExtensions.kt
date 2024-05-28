package org.orkg.graph.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import dev.forkhandles.fabrikate.StringFabricator
import java.net.URI
import kotlin.math.absoluteValue
import org.orkg.common.ThingId
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing

fun Fabrikate.withCustomMappings(): Fabrikate {
    // register fabricator for StatementId because of id constraints
    config.register {
        StatementId(random<Long>().absoluteValue)
    }
    // register fabricator for ThingId because of id constraints
    config.register {
        ThingId(random<Long>().absoluteValue.toString())
    }
    // register fabricator for Things because it's just an interface
    config.register<Thing> {
        createClass(
            id = random(),
            label = random(),
            createdAt = random(),
            uri = random(),
            createdBy = random(),
            // Do not create a random description for tests as it is saved via statements
            // description = random(),
            modifiable = true
        )
        // TODO: generate predicates and resources too?
    }
    // register fabricator for Class because _class needs to be set correctly
    config.register {
        createClass(
            id = random(),
            label = random(),
            createdAt = random(),
            uri = random(),
            createdBy = random(),
            // Do not create a random description for tests as it is saved via statements
            // description = random(),
            modifiable = true
        )
    }
    // register fabricator for Predicate because _class needs to be set correctly
    config.register {
        createPredicate(
            id = random(),
            label = random(),
            createdAt = random(),
            createdBy = random(),
            // Do not create a random description for tests as it is saved via statements
            // description = random(),
            modifiable = true
        )
    }
    // register fabricator for Literal because _class needs to be set correctly
    config.register {
        createLiteral(
            id = random(),
            label = random(),
            datatype = random(),
            createdAt = random(),
            createdBy = random(),
            modifiable = true
        )
    }
    // register fabricator for Resource because _class needs to be set correctly
    config.register {
        createResource(
            id = random(),
            label = random(),
            createdAt = random(),
            classes = random(),
            createdBy = random(),
            observatoryId = random(),
            extractionMethod = random(),
            organizationId = random(),
            visibility = random(),
            verified = random(),
            modifiable = true
        )
    }
    return this
}

/** Helper method to generate a simple number (as string) for use in IDs, so that they look a bit nicer. */
private fun FabricatorConfig.simpleNumberString(maxLength: Int = 4): String =
    List(random.nextInt(1, maxLength + 1)) { random.nextInt(1, 10) }.joinToString("", transform = Int::toString)

/** Generate IDs that follow the literal ID convention, for use in documentation tests. */
fun FabricatorConfig.withLiteralIds(): FabricatorConfig = apply {
    register { ThingId("L${simpleNumberString()}") }
}

// Helper method to work with current withCustomMappings() implementation.
fun Fabrikate.withLiteralIds(): Fabrikate = apply {
    config.withLiteralIds()
}

inline fun <reified T : Any> Fabrikate.random(size: Int): List<T> {
    return (0 until size).map { random() }
}

fun Fabrikate.withLongerURIs(): Fabrikate = apply {
    config.register {
        URI.create("https://${StringFabricator(random = config.random, length = IntRange(10, 20))()}.com")
    }
}
