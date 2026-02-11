package org.orkg.common.json

import tools.jackson.databind.annotation.JsonSerialize

@JsonSerialize(converter = PageModelConverter::class)
abstract class PageImplMixin
