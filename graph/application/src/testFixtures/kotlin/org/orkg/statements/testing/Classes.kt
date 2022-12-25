package org.orkg.statements.testing

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import java.net.URI
import java.time.OffsetDateTime

fun createClass(
    id: ClassId = ClassId("OK"),
    label: String = "some label",
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    uri: URI? = URI.create("https://example.org/OK"),
    createdBy: ContributorId = ContributorId("dc8b2055-c14a-4e9f-9fcd-e0b79cf1f834"),
): Class = Class(id, label, uri, createdAt, createdBy)
