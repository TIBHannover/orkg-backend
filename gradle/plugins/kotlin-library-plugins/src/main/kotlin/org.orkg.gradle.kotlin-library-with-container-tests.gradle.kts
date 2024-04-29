plugins {
    id("org.orkg.gradle.kotlin-library")
}

val test by testing.suites.getting(JvmTestSuite::class)

val containerTest by testing.suites.registering(JvmTestSuite::class) {
    testType.set(TestSuiteType.FUNCTIONAL_TEST)
    useJUnitJupiter("") // Configure "version-less" dependency, provided by platform
    targets {
        all {
            testTask.configure {
                shouldRunAfter(test)
            }
        }
    }
}

tasks.named("check") {
    dependsOn(containerTest)
}
