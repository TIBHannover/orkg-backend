import org.orkg.gradle.plugins.PrintCoverageTask

plugins {
    id("com.diffplug.spotless") version "6.2.1" apply false
    id("com.diffplug.spotless-changelog") version "2.4.0"

    base // required, otherwise report aggregation plugins will not work
    id("test-report-aggregation")
    id("jacoco-report-aggregation")
    id("org.orkg.print-coverage")
}

dependencies {
    // Configure all projects for test and coverage aggregation. Ignore parent projects, or those without code.
    val ignoredProjects = setOf(":platform", ":common", ":graph", ":identity-management", ":testing")
    subprojects
        .filterNot { it.path in ignoredProjects || it.path.contains("sdn6") }
        .onEach(::jacocoAggregation)
        .onEach(::testReportAggregation)
}

// Test reports
reporting {
    reports {
        val testAggregateTestReport by creating(AggregateTestReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}

// JaCoCo Code Coverage Report
reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }

}

val testCodeCoverageReport by tasks.getting(JacocoReport::class) {
    reports {
        xml.required.set(true)
    }
}

tasks.check {
    dependsOn(tasks.named<TestReport>("testAggregateTestReport"))
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
    finalizedBy(tasks.named<PrintCoverageTask>("printCoverage"))
}
