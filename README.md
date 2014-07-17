vodafoneitaly-maven-plugin
==========================

A Maven plugin useful to handle [Vodafone Italy](http://vodafone.it) processes.

Usage
-----

Add this to your `pom`:

```xml
	<plugin>
		<groupId>com.github.sixro</groupId>
		<artifactId>vodafoneitaly-maven-plugin</artifactId>
		<version>0.6-SNAPSHOT</version>
		<executions>
			<execution>
				<id>canvass-kit</id>
				<goals>
					<goal>canvass-kit</goal>
				</goals>
			</execution>
		</executions>
	</plugin>
```

then in your `properties` define plugin properties:

```
	<vodafoneitaly.canvass.system>Merlino</vodafoneitaly.canvass.system>
	<vodafoneitaly.canvass.version>1.0.0</vodafoneitaly.canvass.version>
	<vodafoneitaly.canvass.date>${project.version}</vodafoneitaly.canvass.date>
	<vodafoneitaly.canvass.sgst>ST28633</vodafoneitaly.canvass.sgst>
	<vodafoneitaly.canvass.releasePhase>Eccezione2</vodafoneitaly.canvass.releasePhase>
```

As you can see, we suggest to use the `project.version` for the `vodafoneitaly.canvass.date`. To explain the reason of that look at this example.  
Imagine you have a project with this header:

```
<project [...]>
        <modelVersion>4.0.0</modelVersion>

        <groupId>myGroupId</groupId> 
        <artifactId>myArtifactId</artifactId> 
        <version>20140720</version> 
        <packaging>pom</packaging>
	...
```

your deployment server will have:

```
    myGroupId/myArtifactId/20140720/myArtifactId-20140720-Eccezione2.zip
```

where the `version` is `20140720` and the `vodafoneitaly.canvass.releasePhase` is used as a `classifier`.

Create a `kit` directory under `src/main` and under it create following subdirectories:

```
KitForOperations
  |
  +- yourSystem
  |    |
  |    +- DOCS
  |    |    |
  |    |    +- Delivery
  |    |
  |    +- SOFTWARE
  |
  +- SCRIPT
       |
       +- ORACLE
            |
            +- yourDatabase1
            |
            +- yourDatabase2
            |
            [...]
```

where `yourSystem` is the name of your system known by Vodafone and `yourDatabase\*` are your databases for which you release SQL scripts.

Create a `templates` directory under `src/main` and copy the `SQ.xls` template and the `MD.xls` template (we cannot provide them to you for security reasons).

Development
-----------

To write this plugin I'm following [this manual](http://books.sonatype.com/mvnref-book/reference/writing-plugins-sect-custom-plugin.html).

You can find project notes [here](http://goo.gl/usnglW).
