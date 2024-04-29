package org.orkg.contenttypes.domain.identifiers

import org.orkg.common.exceptions.PropertyValidationException

class InvalidIdentifier(name: String, cause: IllegalArgumentException) :
    PropertyValidationException(name, cause.message!!)
