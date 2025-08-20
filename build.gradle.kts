plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
}