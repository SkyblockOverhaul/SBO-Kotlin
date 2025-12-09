plugins {
    id("dev.deftu.gradle.multiversion-root")
}

preprocess {
    "1.21.7-fabric"(1_21_08, "yarn") {
        "1.21.5-fabric"(1_21_05, "yarn")
    }
}