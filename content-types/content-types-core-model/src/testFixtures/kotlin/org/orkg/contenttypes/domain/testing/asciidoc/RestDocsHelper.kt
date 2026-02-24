package org.orkg.contenttypes.domain.testing.asciidoc

import org.orkg.contenttypes.domain.Certainty
import org.orkg.contenttypes.domain.ComparisonDataSource
import org.orkg.contenttypes.domain.ComparisonPath
import org.orkg.contenttypes.domain.ContentTypeClass

val allowedContentTypeClassValues =
    ContentTypeClass.entries.sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")

val allowedCertaintyValues =
    Certainty.entries.sorted().joinToString(separator = ", ", prefix = "`", postfix = "`")

val allowedComparisonPathTypeValues =
    ComparisonPath.Type.entries.sorted().joinToString(separator = ", ", prefix = "`", postfix = "`")

val allowedComparisonDataSourceTypeValues =
    ComparisonDataSource.Type.entries.sorted().joinToString(separator = ", ", prefix = "`", postfix = "`")
