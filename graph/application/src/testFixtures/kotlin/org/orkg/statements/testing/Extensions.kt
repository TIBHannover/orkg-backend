package org.orkg.statements.testing

import dev.forkhandles.fabrikate.Fabrikate
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
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
        )
        // TODO: generate predicates and resources too?
    }
    return this
}
