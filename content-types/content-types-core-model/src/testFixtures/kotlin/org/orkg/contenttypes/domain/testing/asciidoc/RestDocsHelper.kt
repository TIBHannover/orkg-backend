package org.orkg.contenttypes.domain.testing.asciidoc

import org.orkg.contenttypes.domain.Certainty
import org.orkg.contenttypes.domain.ComparisonType
import org.orkg.contenttypes.domain.ContentTypeClass

val allowedContentTypeClassValues =
    ContentTypeClass.entries.sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")

val allowedComparisonTypeValues =
    ComparisonType.entries.sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")

val allowedCertaintyValues =
    Certainty.entries.sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")
