package eu.tib.orkg.prototype.events.analytics

import eu.tib.orkg.prototype.events.service.NotificationUpdatesService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ResourceAnalytics(
    private val service: NotificationUpdatesService
) {

    fun getTotalPapers(userId: UUID): Int{
        service.getTotalResourcesByGroup(userId).map {
            if (it.resource_type.equals("Paper")){
                return it.count
            }
        }
        return 0;
    }

    fun getTotalComparisons(userId: UUID): Int{
        service.getTotalResourcesByGroup(userId).map {
            if (it.resource_type.equals("Comparison")){
                return it.count
            }
        }
        return 0;
    }

    fun getTotalVisualizations(userId: UUID): Int{
        service.getTotalResourcesByGroup(userId).map {
            if (it.resource_type.equals("Visualization")){
                return it.count
            }
        }
        return 0;
    }

    fun getTotalSmartReviews(userId: UUID): Int{
        service.getTotalResourcesByGroup(userId).map {
            if (it.resource_type.equals("SmartReview")){
                return it.count
            }
        }
        return 0;
    }
}
