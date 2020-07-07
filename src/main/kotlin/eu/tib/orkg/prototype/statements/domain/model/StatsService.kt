package eu.tib.orkg.prototype.statements.domain.model

interface StatsService {
    /**
     * Get stats
     *
     * @return the stats summary
     */
    fun getStats(): Stats

    fun getFieldsStats(): Map<String, Int>
}
