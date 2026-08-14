package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.ComparisonInUse
import org.orkg.contenttypes.domain.actions.AbstractGraphDeleter
import org.orkg.contenttypes.domain.actions.DeleteComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.DeleteComparisonAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementSpecBuilder
import org.orkg.graph.domain.thing
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.output.StatementRepository

class ComparisonDeleter(
    private val abstractGraphDeleter: AbstractGraphDeleter,
) : DeleteComparisonAction {
    constructor(
        statementRepository: StatementRepository,
        resourceUseCases: ResourceUseCases,
        predicateUseCases: PredicateUseCases,
    ) : this(AbstractGraphDeleter(statementRepository, resourceUseCases, predicateUseCases))

    override fun invoke(command: DeleteComparisonCommand, state: State): State {
        if (state.comparison != null) {
            val success = abstractGraphDeleter.delete(
                thingSpec = comparisonGraph,
                root = state.comparison,
                statements = state.statements.values.flatten(),
                contributorId = command.contributorId,
            )
            if (!success) {
                throw ComparisonInUse(command.comparisonId)
            }
        }
        return state
    }
}

private val comparisonGraph = thing {
    classId(Classes.comparison)
    statementTo { authorList() }
    statementTo {
        predicateId(Predicates.hasRelatedResource)
        `object` {
            classId(Classes.comparisonRelatedResource)
            statementTo {
                predicateId(Predicates.hasImage)
                `object` { literal() }
            }
            statementTo {
                predicateId(Predicates.description)
                `object` { literal() }
            }
            statementTo {
                predicateId(Predicates.hasURL)
                `object` { literal() }
            }
        }
    }
    statementTo {
        predicateId(Predicates.monthPublished)
        `object` { literal() }
    }
    statementTo {
        predicateId(Predicates.yearPublished)
        `object` { literal() }
    }
    statementTo {
        predicateId(Predicates.hasVenue)
        `object` {
            classId(Classes.venue)
            shared()
        }
    }
    statementTo {
        predicateId(Predicates.hasURL)
        `object` { literal() }
    }
    statementTo {
        predicateId(Predicates.description)
        `object` { literal() }
    }
    statementTo {
        predicateId(Predicates.hasSubject)
        `object` {
            classId(Classes.researchField)
            shared()
        }
    }
    statementTo {
        predicateId(Predicates.hasDOI)
        `object` { literal() }
    }
    statementTo {
        predicateId(Predicates.hasVisualization)
        `object` {
            classId(Classes.visualization)
            statementTo { authorList() }
            statementTo {
                predicateId(Predicates.description)
                `object` { literal() }
            }
        }
    }
    statementTo {
        predicateId(Predicates.reference)
        `object` { literal() }
    }
    statementTo {
        predicateId(Predicates.isAnonymized)
        `object` { literal() }
    }
    statementTo {
        predicateId(Predicates.sustainableDevelopmentGoal)
        `object` {
            classId(Classes.sustainableDevelopmentGoal)
            shared()
        }
    }
    statementTo {
        predicateId(Predicates.hasRelatedFigure)
        `object` {
            classId(Classes.comparisonRelatedFigure)
            statementTo {
                predicateId(Predicates.hasImage)
                `object` { literal() }
            }
            statementTo {
                predicateId(Predicates.description)
                `object` { literal() }
            }
        }
    }
}

private fun StatementSpecBuilder.Outgoing.authorList() {
    predicateId(Predicates.hasAuthors)
    `object` {
        classId(Classes.list)
        statementTo {
            predicateId(Predicates.hasListElement)
            `object` { literal() }
        }
        statementTo {
            predicateId(Predicates.hasListElement)
            `object` {
                classId(Classes.author)
                shared()
            }
        }
    }
}
