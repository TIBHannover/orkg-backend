package org.orkg.common.testing.fixtures

import org.hamcrest.Description
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.orkg.common.isValidDateTime

class IsIso8601DateTimeString : TypeSafeDiagnosingMatcher<String>() {
    override fun describeTo(description: Description) {
        description.appendValue("a string matching an ISO 8601 date time value")
    }

    override fun matchesSafely(actual: String?, mismatchDescription: Description): Boolean {
        if (actual?.isValidDateTime() != true) {
            mismatchDescription.appendText("the string was ").appendValue(actual)
            return false
        }
        return true
    }
}
