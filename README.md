# 🐕 qq-benchmark

**qq-benchmark** is a Kotlin library that can benchmark your code snippets with beautiful console output.
- Just copy and paste 🟦 Single-File version [QBenchmark.kt](src-single/QBenchmark.kt) into your project. 
- Or you can use 🟩 Split-File Jar version. See [Maven Dependency Section](#-split-file-jar-version-maven-dependency).
- Feel free to fork or copy to your own codebase.



## 🟦 Single-File version Dependency

If you copy & paste [QBenchmark.kt](src-single/QBenchmark.kt).

Refer to [build.gradle.kts](build.gradle.kts) to directly check project settings.



```kotlin
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.20")
}
```

## 🟩 Split-File Jar version Maven Dependency

If you prefer a jar library. Add [jitpack.io](https://jitpack.io/#nyabkun/qq-benchmark) repository to the build script.

### build.gradle ( Groovy )
```groovy
repositories {
    ...
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.nyabkun:qq-benchmark:v2023-05-21'
}
```

### build.gradle.kts ( Kotlin )
```kotlin
repositories {
    ...
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.nyabkun:qq-benchmark:v2023-05-21")
}
```

### pom.xml
```xml
<repositories>
    ...
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    ...
    <dependency>
        <groupId>com.github.nyabkun</groupId>
        <artifactId>qq-benchmark</artifactId>
        <version>v2023-05-21</version>
    </dependency>
</dependencies>
```

## How did I create this library

I created this library by developing a program within my own codebase that automatically resolves dependencies at the method or property level, extracts necessary code elements, and generates a compact, self-contained, single-file library.

The program uses [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html) to resolve dependencies for function calls and references to classes.

Although my original repository is currently disorganized, I have been gradually extracting and publishing small libraries. I also plan to prepare the original repository for publication in the future

