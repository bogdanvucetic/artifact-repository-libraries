plugins {
    application
}

dependencies {
    implementation(project(":libs:resolver-api"))
    implementation(project(":libs:dependency-resolver"))

    runtimeOnly(project(":impls:local-storage"))
    runtimeOnly(project(":impls:raf-storage"))
    runtimeOnly(project(":impls:drive-storage"))
}

application {
    mainClass = "cli.resolver.Main"
    applicationName = "resolver"
}