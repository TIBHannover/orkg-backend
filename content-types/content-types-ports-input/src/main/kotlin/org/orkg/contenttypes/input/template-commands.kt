package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.FormattedLabel

interface CreateTemplateUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val label: String,
        val description: String?,
        val formattedLabel: FormattedLabel?,
        val targetClass: ThingId,
        val relations: Relations,
        val properties: List<TemplatePropertyDefinition>,
        val isClosed: Boolean,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>
    ) {
        data class Relations(
            val researchFields: List<ThingId>,
            val researchProblems: List<ThingId>,
            val predicate: ThingId?
        )

        data class LiteralPropertyDefinition(
            override val label: String,
            override val minCount: Int?,
            override val maxCount: Int?,
            override val pattern: String?,
            override val path: ThingId,
            override val datatype: ThingId
        ) : LiteralTemplatePropertyDefinition

        data class ResourcePropertyDefinition(
            override val label: String,
            override val minCount: Int?,
            override val maxCount: Int?,
            override val pattern: String?,
            override val path: ThingId,
            override val `class`: ThingId
        ) : ResourceTemplatePropertyDefinition
    }
}

interface CreateTemplatePropertyUseCase {
    fun createTemplateProperty(command: CreateCommand): ThingId

    sealed interface CreateCommand : TemplatePropertyDefinition {
        val contributorId: ContributorId
        val templateId: ThingId
    }

    data class CreateLiteralPropertyCommand(
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val pattern: String?,
        override val path: ThingId,
        override val datatype: ThingId
    ) : CreateCommand, LiteralTemplatePropertyDefinition

    data class CreateResourcePropertyCommand(
        override val contributorId: ContributorId,
        override val templateId: ThingId,
        override val label: String,
        override val minCount: Int?,
        override val maxCount: Int?,
        override val pattern: String?,
        override val path: ThingId,
        override val `class`: ThingId
    ) :  CreateCommand, ResourceTemplatePropertyDefinition
}

sealed interface TemplatePropertyDefinition {
    val label: String
    val minCount: Int?
    val maxCount: Int?
    val pattern: String?
    val path: ThingId
}

sealed interface LiteralTemplatePropertyDefinition : TemplatePropertyDefinition {
    val datatype: ThingId
}

sealed interface ResourceTemplatePropertyDefinition : TemplatePropertyDefinition {
    val `class`: ThingId
}
