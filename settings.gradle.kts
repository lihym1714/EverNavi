pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven (  "https://devrepo.kakao.com/nexus/repository/kakaomap-releases/")
        maven("https://naver.jfrog.io/artifactory/maven/")
    }
}

rootProject.name = "NaverNavi"
include(":app")
