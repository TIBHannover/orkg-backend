package eu.tib.orkg.prototype.statements.domain.model

interface FieldService {

    fun getResearchProblemsOfField(fieldId: ResourceId): List<Any>
}
