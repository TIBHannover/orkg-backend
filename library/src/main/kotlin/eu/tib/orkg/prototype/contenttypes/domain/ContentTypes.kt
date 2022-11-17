package eu.tib.orkg.prototype.contenttypes.domain

sealed interface ContentType

class Contribution : ContentType
class Paper(
    val title: String
) : ContentType

class ResearchProblem : ContentType
class Visualization : ContentType
