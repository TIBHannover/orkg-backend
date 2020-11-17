package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.domain.model.neo4j.ObservatoryResources

interface StatsService {
    /**
     * Get stats
     *
     * @return the stats summary
     */
    fun getStats(): Stats

    fun getFieldsStats(): Map<String, Int>

    fun getObservatoryPapersCount(id: ObservatoryId): Long

    fun getObservatoryComparisonsCount(id: ObservatoryId): Long

    fun getObservatoriesPapersAndComparisonsCount(): Iterable<ObservatoryResources>
}
