plugins {
    application
}

dependencies {
    implementation(project(":libs:component-publisher"))

    runtimeOnly(project(":impls:local-storage"))
    runtimeOnly(project(":impls:raf-storage"))
    runtimeOnly(project(":impls:drive-storage"))
}

application {
    mainClass = "cli.publisher.Main"
    applicationName = "publisher"
}