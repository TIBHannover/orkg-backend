package org.orkg.graph.testing.fixtures

import dev.forkhandles.fabrikate.Fabricator
import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import dev.forkhandles.fabrikate.StringFabricator
import dev.forkhandles.fabrikate.register
import kotlin.math.absoluteValue
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing

class StatementIdFabricator : Fabricator<StatementId> {
    override fun invoke(fabrikate: Fabrikate): StatementId {
        return StatementId(fabrikate.random<Long>().absoluteValue)
    }
}

class ThingIdFabricator : Fabricator<ThingId> {
    override fun invoke(fabrikate: Fabrikate): ThingId {
        return ThingId(fabrikate.random<Long>().absoluteValue.toString())
    }
}

class ParsedIRIFabricator : Fabricator<ParsedIRI> {
    private val stringFabricator = StringFabricator(length = IntRange(10, 20))

    override fun invoke(fabrikate: Fabrikate): ParsedIRI {
        return ParsedIRI("https://example.com/${stringFabricator(fabrikate)}")
    }
}

class ThingFabricator : Fabricator<Thing> {
    override fun invoke(fabrikate: Fabrikate): Thing {
        // TODO: generate predicates and resources too?
        return createClass(
            id = fabrikate.random(),
            label = fabrikate.random(),
            createdAt = fabrikate.random(),
            uri = fabrikate.random(),
            createdBy = fabrikate.random(),
            // Do not create a random description for tests, as it needs to be saved via statements
            // description = random(),
            modifiable = true
        )
    }
}

class ClassFabricator : Fabricator<Class> {
    override fun invoke(fabrikate: Fabrikate): Class {
        return createClass(
            id = fabrikate.random(),
            label = fabrikate.random(),
            createdAt = fabrikate.random(),
            uri = fabrikate.random(),
            createdBy = fabrikate.random(),
            // Do not create a random description for tests, as it needs to be saved via statements
            // description = random(),
            modifiable = true
        )
    }
}

class PredicateFabricator : Fabricator<Predicate> {
    override fun invoke(fabrikate: Fabrikate): Predicate {
        return createPredicate(
            id = fabrikate.random(),
            label = fabrikate.random(),
            createdAt = fabrikate.random(),
            createdBy = fabrikate.random(),
            // Do not create a random description for tests, as it needs to be saved via statements
            // description = random(),
            modifiable = true
        )
    }
}

class LiteralFabricator : Fabricator<Literal> {
    override fun invoke(fabrikate: Fabrikate): Literal {
        return createLiteral(
            id = fabrikate.random(),
            label = fabrikate.random(),
            datatype = fabrikate.random(),
            createdAt = fabrikate.random(),
            createdBy = fabrikate.random(),
            modifiable = true
        )
    }
}

class ResourceFabricator : Fabricator<Resource> {
    override fun invoke(fabrikate: Fabrikate): Resource {
        return createResource(
            id = fabrikate.random(),
            label = fabrikate.random(),
            createdAt = fabrikate.random(),
            classes = fabrikate.random(),
            createdBy = fabrikate.random(),
            observatoryId = fabrikate.random(),
            extractionMethod = fabrikate.random(),
            organizationId = fabrikate.random(),
            visibility = fabrikate.random(),
            verified = fabrikate.random(),
            modifiable = true
        )
    }
}

fun FabricatorConfig.withGraphMappings(): FabricatorConfig = withMappings {
    register(StatementIdFabricator())
    register(ThingIdFabricator())
    register(ParsedIRIFabricator())
    register(ThingFabricator())
    register(ClassFabricator())
    register(PredicateFabricator())
    register(LiteralFabricator())
    register(ResourceFabricator())
}

inline fun <reified T : Any> Fabrikate.random(count: Int): List<T> {
    return (0 until count).map { random() }
}
