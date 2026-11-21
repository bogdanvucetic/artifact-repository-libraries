plugins {
    application
}

dependencies {
    implementation(project(":libs:component-publisher"))

    runtimeOnly(project(":impls:publisher-local"))
    runtimeOnly(project(":impls:publisher-raf"))
    runtimeOnly(project(":impls:publisher-drive"))
}

application {
    mainClass = "cli.publisher.Main"
    applicationName = "publisher"
}