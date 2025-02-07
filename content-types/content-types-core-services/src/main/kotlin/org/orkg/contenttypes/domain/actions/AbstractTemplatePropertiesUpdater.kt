package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class AbstractTemplatePropertiesUpdater(
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator,
    private val abstractTemplatePropertyUpdater: AbstractTemplatePropertyUpdater,
    private val abstractTemplatePropertyDeleter: AbstractTemplatePropertyDeleter
) {
    constructor(
        literalService: LiteralUseCases,
        resourceService: ResourceUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        AbstractTemplatePropertyCreator(unsafeResourceUseCases, literalService, unsafeStatementUseCases),
        AbstractTemplatePropertyUpdater(literalService, unsafeResourceUseCases, statementService, unsafeStatementUseCases),
        AbstractTemplatePropertyDeleter(resourceService, statementService)
    )

    internal fun update(
        contributorId: ContributorId,
        subjectId: ThingId,
        newProperties: List<TemplatePropertyDefinition>,
        oldProperties: List<TemplateProperty>,
        statements: Map<ThingId, List<GeneralStatement>>
    ) {
        val properties = oldProperties.toMutableList()
        val new2old = newProperties.associateWith { newProperty ->
            properties.firstOrNull { newProperty.matchesProperty(it) }?.also { properties.remove(it) }
        }
        newProperties.forEachIndexed { index, newProperty ->
            val oldProperty = new2old[newProperty]
            if (oldProperty != null) {
                if (oldProperty.order != index.toLong()) {
                    abstractTemplatePropertyUpdater.update(
                        statements = statements[oldProperty.id].orEmpty(),
                        contributorId = contributorId,
                        order = index,
                        newProperty = newProperty,
                        oldProperty = oldProperty
                    )
                }
            } else {
                abstractTemplatePropertyCreator.create(
                    contributorId = contributorId,
                    templateId = subjectId,
                    order = index,
                    property = newProperty
                )
            }
        }
        properties.forEach {
            abstractTemplatePropertyDeleter.delete(contributorId, subjectId, it.id)
        }
    }
}
