dependencies {
    implementation(project(":libs:component-publisher"))
    implementation(project(":impls:resolver-drive"))

    implementation("com.google.api-client:google-api-client:2.9.2")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.39.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20261011-2.0.0")

    testImplementation(project(":libs:resolver-api"))
    testImplementation(project(":libs:dependency-resolver"))
}

tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}