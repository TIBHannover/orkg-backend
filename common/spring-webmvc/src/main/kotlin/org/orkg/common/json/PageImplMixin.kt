package org.orkg.common.json

import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonSerialize(converter = PageModelConverter::class)
abstract class PageImplMixin
