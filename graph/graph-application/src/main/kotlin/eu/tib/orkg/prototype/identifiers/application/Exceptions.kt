package eu.tib.orkg.prototype.identifiers.application

import eu.tib.orkg.prototype.shared.PropertyValidationException

class InvalidIdentifier(name: String, cause: IllegalArgumentException) :
    PropertyValidationException(name, cause.message!!)
