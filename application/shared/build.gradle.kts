plugins { id("org.orkg.kotlin-conventions") }

dependencies {
  api(platform(project(":platform")))

  testImplementation(platform("org.junit:junit-bom:5.7.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.assertj:assertj-core:3.16.1")
}
