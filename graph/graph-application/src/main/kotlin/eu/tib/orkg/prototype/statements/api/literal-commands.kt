package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Literal

interface CreateLiteralUseCase {
    // legacy methods:
    fun create(label: String, datatype: String = "xsd:string"): Literal
    fun create(userId: ContributorId, label: String, datatype: String = "xsd:string"): Literal
}

interface UpdateLiteralUseCase {
    // legacy methods:
    fun update(literal: Literal)
}

interface DeleteLiteralUseCase {
    // legacy methods:
    fun removeAll()
}
