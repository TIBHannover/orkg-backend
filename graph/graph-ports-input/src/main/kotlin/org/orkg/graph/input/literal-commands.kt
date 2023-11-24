package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.graph.domain.Literal

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
