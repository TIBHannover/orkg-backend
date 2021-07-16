plugins { id("org.orkg.kotlin-conventions") }

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.7.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
}
