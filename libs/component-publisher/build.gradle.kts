plugins {
    `java-library`
}

dependencies {
    api(project(":libs:repository-common"))
    implementation("com.google.code.gson:gson:2.15.0")
}