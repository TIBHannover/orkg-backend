package org.orkg.statements.testing

import dev.forkhandles.fabrikate.Fabrikate
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.Class
import kotlin.math.absoluteValue

fun Fabrikate.withCustomMappings(): Fabrikate {
    // register fabricator for ClassId because of id constraints
    config.register {
        ClassId(random<Long>().absoluteValue)
    }
    // register fabricator for PredicateId because of id constraints
    config.register {
        PredicateId(random<Long>().absoluteValue)
    }
    // register fabricator for StatementId because of id constraints
    config.register {
        StatementId(random<Long>().absoluteValue)
    }
    // register fabricator for Things because it's just an interface
    config.register<Thing> {
        createClass(
            id = random(),
            label = random(),
            createdAt = random(),
            uri = random(),
            createdBy = random()
        ).apply {
            description = random()
        }
        // TODO: generate predicates and resources too?
    }
    // register fabricator for Class because _class needs to be set correctly
    config.register {
        createClass(
            id = random(),
            label = random(),
            createdAt = random(),
            uri = random(),
            createdBy = random()
        ).apply {
            description = random()
        }
    }
    // register fabricator for Predicate because _class needs to be set correctly
    config.register {
        createPredicate(
            id = random(),
            label = random(),
            createdAt = random(),
            createdBy = random()
        ).apply {
            description = random()
        }
    }
    // register fabricator for Literal because _class needs to be set correctly
    config.register {
        createLiteral(
            id = random(),
            label = random(),
            datatype = random(),
            createdAt = random(),
            createdBy = random()
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
            featured = random(),
            unlisted = random(),
            verified = random()
        )
    }
    return this
}
