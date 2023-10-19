package eu.tib.orkg.prototype.identifiers.domain

private val VALID_RESEARCH_GATE_ID_REGEX = Regex("""^[0-9A-Za-z_-]+$""")
private val VALID_RESEARCH_GATE_ID_URI_REGEX = Regex("""^https?://(?:www\.)?researchgate\.net/profile/([0-9A-Za-z_-]+)$""")

@JvmInline
value class ResearchGateId private constructor(override val value: String) : IdentifierValue {
    companion object : IdentifierValueFactory<ResearchGateId>(
        fn = ::ResearchGateId,
        validationRegex = VALID_RESEARCH_GATE_ID_REGEX,
        uriValidationRegex = VALID_RESEARCH_GATE_ID_URI_REGEX
    )

    override val uri: String get() = "https://researchgate.net/profile/$value"
}
