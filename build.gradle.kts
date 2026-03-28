plugins {
    `maven-publish`
}
group = "com.github.withyoursquad"
version = "1.6.0"
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.github.withyoursquad"
            artifactId = "squad-sports-android"
            version = "1.6.0"
            artifact("squad-sports-android-1.6.0.aar")
        }
    }
}
