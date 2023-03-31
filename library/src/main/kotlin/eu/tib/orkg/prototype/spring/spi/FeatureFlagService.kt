package eu.tib.orkg.prototype.spring.spi

/**
 * Interface to provide access to feature flags.
 *
 * When flags are tested, their default values should be the same as in the production configuration.
 *
 * Flags should not be removed to decrease the risk of accidental reuse.
 * Use the [Deprecated] annotation and set `level` to [DeprecationLevel.HIDDEN] instead.
 * To find (and remove) all usages before that, use [DeprecationLevel.ERROR] and follow the compiler messages.
 */
interface FeatureFlagService {
    /** Determine if the old (legacy) model of Papers With Code should be used. */
    @Deprecated("This flag is obsolete and will be removed.", level = DeprecationLevel.HIDDEN)
    fun isPapersWithCodeLegacyModelEnabled(): Boolean

    /** Determine if formatted labels are enabled and appended to resource representation. */
    fun isFormattedLabelsEnabled(): Boolean

    /** Determine if cache warmup is enabled **/
    fun isCacheWarmupEnabled(): Boolean
}
