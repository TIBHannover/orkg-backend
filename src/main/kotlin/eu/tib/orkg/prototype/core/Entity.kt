package eu.tib.orkg.prototype.core

abstract class Entity<out T : Identity<*>>(open val id: T)
