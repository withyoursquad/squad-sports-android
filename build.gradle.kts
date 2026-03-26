plugins {
    `maven-publish`
}
group = "com.github.withyoursquad"
version = "1.5.0"
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.github.withyoursquad"
            artifactId = "squad-sports-android"
            version = "1.5.0"
            artifact("squad-sports-android-1.5.0.aar")
        }
    }
}
