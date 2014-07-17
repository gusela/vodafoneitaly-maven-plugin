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

As you can see, we suggest to use the `project.version` for the `vodafoneitaly.canvass.date`. Imagine you have a project with `groupId` valued as `myGroupId`, `artifactId` valued as `myArtifactId` and with `version` valued as `20140720` and `releasePhase` valued as `Eccezione2`, your distribution will be:

```
    myGroupId/myArtifactId/20140720/myArtifactId-20140720-Eccezione2.zip
```

Development
-----------

To write this plugin I'm following [this manual](http://books.sonatype.com/mvnref-book/reference/writing-plugins-sect-custom-plugin.html).

You can find project notes [here](http://goo.gl/usnglW).
