package eu.tib.orkg.prototype.contenttypes.services.actions

internal val String.isTempId: Boolean get() = startsWith('#') || startsWith('^')
