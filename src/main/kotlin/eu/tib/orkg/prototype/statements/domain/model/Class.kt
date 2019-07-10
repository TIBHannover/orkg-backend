package eu.tib.orkg.prototype.statements.domain.model

import java.net.URI

data class Class(val id: ClassId, val label: String, val uri: URI?)
