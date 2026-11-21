plugins {
    application
}

dependencies {
    implementation(project(":libs:resolver-api"))
    implementation(project(":libs:dependency-resolver"))

    runtimeOnly(project(":impls:resolver-local"))
    runtimeOnly(project(":impls:resolver-raf"))
    runtimeOnly(project(":impls:resolver-drive"))
}

application {
    mainClass = "cli.resolver.Main"
    applicationName = "resolver"
}