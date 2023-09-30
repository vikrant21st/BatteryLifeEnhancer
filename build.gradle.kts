buildscript {
    extra.apply {
        set("compose_version", "1.4.3")
        set("work_version", "2.8.1")
        set("lifecycle_version", "2.6.1")
        set("nav_version", "2.6.0")
        set("activity_version", "1.7.2")
        set("mat3_version", "1.1.1")
        set("glance_version", "1.0.0-beta01")
        set("datastore_version", "1.0.0")
        set("immutable_collections_version", "0.3.5")
        set("serialization_version", "1.5.1")

        set("junit_version", "4.13.2")
        set("androidx_junit_version", "1.1.5")
        set("espresso_version", "3.5.1")
    }

    dependencies {
        val nav_version: String by rootProject.extra
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
    }
}// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application").version("8.0.2").apply( false)
    id("com.android.library").version("8.0.2").apply( false)
    id("org.jetbrains.kotlin.android").version("1.8.21").apply( false)
}