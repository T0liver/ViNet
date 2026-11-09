plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("vinetDependencyGuard") {
            id = "vinet.dependency-guard"
            implementationClass = "hu.toliver.vinet.gradle.ViNetDependencyGuardPlugin"
        }
    }
}
