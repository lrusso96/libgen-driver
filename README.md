# Libgen Driver 

Libgen driver is Java Libary to obtain infos from Libgen.

## Getting started

### Prerequisites
This application is written with JDK8 in mind. If you don't have a Java Development Kit installed you can download it from [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

### Compile from sources
- `git clone` or download this repo.
- Open a terminal in the directory where the sources are stored.
- Execute `mvn install -DskipTests` . The .jar file will be in the target folder.

### Add to your project

You can easily add to your existing project through Maven or Gradle.

**Maven**

1) Add the JitPack repository
```
<repositories>
	<repository>
	    <id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```
2) Add the dependency
```
<dependency>
    <groupId>com.gitlab.lrusso96</groupId>
    <artifactId>libgen_driver</artifactId>
    <version>0.1</version>
</dependency>
```

**Gradle**

1) Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
		maven { url 'https://jitpack.io' }
	}
}
```
2) Add the dependency
```
dependencies {
    implementation 'com.gitlab.lrusso96:openstud_driver:0.1
}
```


### Examples
```
Libgen lib = new Libgen();

//simple search
lib.search("Divina commedia");

//specific fields
lib.searchAuthor("Dante Alighieri");

 ```

 ## Dependencies
 - [Square OkHttp](https://github.com/square/okhttp)
 - [JUnit](https://github.com/junit-team/junit4)
 - [Jsoup](https://github.com/jhy/jsoup)
 - [org/Json](https://github.com/stleary/JSON-java)
 - [Apache Commons Lang](https://commons.apache.org/proper/commons-lang/)