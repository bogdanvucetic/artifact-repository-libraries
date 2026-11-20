plugins {
    `java-library`
}

dependencies {
    api(project(":libs:resolver-api"))
    implementation("com.google.code.gson:gson:2.11.0")
}