vodafoneitaly-maven-plugin
==========================

## Summary

  1. [Description](#description)
  1. [Usage](#usage)
    1. [SQ and MD templates](#sq_and_md_templates)
  1. [Editing MS Word documents](#editing_ms_word_documents)
    1. [Placeholders](#placeholders)
    1. [Known issues](#known_issues)
  1. [SQ Documents](#sq_documents)
  1. [Where to get help](#where_to_get_help)
  1. [Contribution guidelines](#contribution_guidelines)
  1. [Contributor list](#contributor_list)
  1. [Credits, Inspiration, Alternatives](#credits_inspiration_alternatives)

## <a name="description"/>Description

A Maven plugin useful to handle [Vodafone Italy](http://vodafone.it) processes.   
Actually it is able to create a kit for Canvass.


## <a name="usage"/>Usage

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

```xml
	<vodafoneitaly.canvass.system>Merlino</vodafoneitaly.canvass.system>
	<vodafoneitaly.canvass.version>1.0.0</vodafoneitaly.canvass.version>
	<vodafoneitaly.canvass.date>${project.version}</vodafoneitaly.canvass.date>
	<vodafoneitaly.canvass.sgst>ST28633</vodafoneitaly.canvass.sgst>
	<vodafoneitaly.canvass.releasePhase>Eccezione2</vodafoneitaly.canvass.releasePhase>
```

As you can see, we suggest to use the `project.version` for the `vodafoneitaly.canvass.date`. To explain the reason of that look at this example.  
Imagine you have a project with this header:

```xml
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

Under `build` define a name with `releasePhase`:

```xml
    ...
    <build>
        <finalName>Kit${vodafoneitaly.canvass.releasePhase}</finalName>
    ...
```

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

where `yourSystem` is the name of your system known by Vodafone and `yourDatabase1` and others are your databases for which you release SQL scripts.

Copy softwares you need to release under `KitForOperations/yourSystem/SOFTWARE`.

Copy SQL scripts you need to release under databases directory found under `KitForOperations/SCRIPT/ORACLE/`.

Copy all your documents you need to release under `KitForOperations/yourSystem/DOCS/Delivery`. E.g. the `RN.docx` and the `HO.docx`. Do not copy `MD` and `SQ`(s) because they will be generated by the plugin. If you name your file without any number (e.g. `RN.docx`), your document name will be renamed with standard naming rule (e.g. `RN-system-Vversion-date.docx`). You can put in your documents placeholders like `canvassSystem`, `canvassDate`, etc... that will be replaced by the plugin on the destination folder.

Launch on your command line:
```
    mvn clean install
```

You should find under `target/vodafoneitaly/canvass-kit-exploded` an exploded version of the kit, and under `target/vodafoneitaly/canvass-kit` a compressed kit with expected name.  
In the exploded directory you'll find that the plugin:

  * has copied all SQL scripts updating line ending in DOS mode and changing the first row as required by Vodafone DBA (obsolete CVS Id)
  * has generated all `SQ` files needed for all SQL scripts found under `KitForOperations/yourSystem/DOCS/Delivery`
  * has generated `MD` file under `KitForOperations/yourSystem/DOCS/Delivery` listing all files found under `KitForOperations/yourSystem/SOFTWARE`, calculating `cksum`, etc...
  * has copied all MS Word files found in `kit` directory (recursive) updating all placeholders found in them
  * has copied all other files found in `kit` directory (recursive)
  * has generated `md5` files

### <a name="sq_and_md_templates"/>SQ and MD templates

The plugin has `SQ` and `MD` templates within itself. If you need to change them, set properties:

  * `vodafoneitaly.canvass.kit.sq.template`
  * `vodafoneitaly.canvass.kit.md.template`


## <a name="editing_ms_word_documents"/>Editing MS Word documents

### <a name="placeholders"/>Placeholders

You can use following placeholders in your MS Word documents:

  * `canvassSystem`: the value of the property `vodafoneitaly.canvass.system`
  * `canvassVersion`: the value of the property `vodafoneitaly.canvass.version` 
  * `canvassDate`: the value of the property `vodafoneitaly.canvass.date` 
  * `canvassIsoDate`: a version of `canvassDate` in ISO 8601 format (e.g. `2014-07-20`)
  * `canvassSgst`: the value of the property `vodafoneitaly.canvass.sgst` 
  * `canvassReleasePhase`: the value of the property `vodafoneitaly.canvass.releasePhase` 
  * `canvassKitSoftwaresSubdirectory`: the subdirectory of softwares (e.g. `KitForOperations/yourSystem/SOFTWARE`)
  * `canvassKitDocsSubdirectory`: the subdirectory of softwares (e.g. `KitForOperations/yourSystem/DOCS/Delivery`)
  * `canvassKitTargetFile`: the target file of the kit
  * `canvassKitTargetFileName`: the target filename of the kit
  * `canvassKitDoc1Filename`: the name of the first document (`docx`, `doc`, `xls` and `xlsx`) filename. This represents the first column of the table found in the section "List of documents" of the `RN` (Release Notes) document. You have also `canvassKitDoc1Document` for the second column, `canvassKitDoc1Version` for the third, `canvassKitDoc1Extension` for the fourth and `canvassKitDoc1Title` for the last one. You have these properties for a maximum of 10 documents. Simply change `1` with the desired number (e.g. `canvassKitDoc10Version`).
  * `canvassKitDocFilenames`: a comma separated list of all document filenames
  * `canvassKitDocumentsSq1Filename`: the first `SQ` filename generated. You have a maximum of 5 `SQ` placeholders so if you need the fifth use `canvassKitDocumentsSq5Filename`
  * `canvassKitDocumentsSqFilenames`: a comma separated list of all `SQ` filenames

and all other properties you define in specific section of your `pom`. E.g. if you define a property `java.version` like here:

```xml
    ...
    <properties>
        ...
        <java.version>1.6</java.version>
        ...
    </properties>
    ...
```

you could use a new placeholder called `javaVersion`.


### <a name="known_issues"/>Known issues

There are known issues with placeholders in MS Word:

  * if you edit a placeholder, it is possible that it won't be replaced by the plugin. Try to select the placeholder entirely, activate a style (e.g. bold) and deactivate it
  * if a placeholder is inside a table and it is on more than one page, it won't be replaced by the plugin. You need to change the table option that permit to broke rows between pages.


## <a name="sq_documents"/>SQ Documents

`SQ` documents are filled using all SQL scripts found.  
The plugin reads all "metadata" found in the beginning of the script and use them to fullfil the excel.  
There are some rules to follow:

  * if you need to specify a `duration` (default `1 minuto`), add it at the end of the `DESCRIPTION` metadata between square parenthesis. E.g. `DESCRIPTION   : my description [1 ora]`
  * the `version` and the `updatedDate` (used in the first row) are found in `VERSION` metadata. The more recent version has to be on the same line of the metadata and the format is `VERSION - UPDATE_DATE TEXT`.

Here an example:

```sql
REM $Id$

/*****************************************************************
****                                                          ****
****                     PACKAGE TEMPLATE                     ****
****                                                          ****
******************************************************************

SCRIPT NAME      : SYS00027.sql   

AUTHOR           : Rossi, Mario (02 99.32.221)

RESPONSIBLE      : Rossi, Mario (02 99.32.221)

SG/ST            : ST11111
  
BUG/OTHER        : 
  
SYSTEM           : Sysolator

MODULE           : 

VERSION          : 1.7 - 04/07/2014 This is a recent version
                   1.6 - 05/12/2013 This is an older version

DESCRIPTION      : My description [3 ore]

CONSTRAINT       :

WARNING          :

DATABASE         : SYSDB

SCHEMA           : SYSSCHEMA
         
*****************************************************************
*****************************************************************/

set time on
set timing on
set echo on
set linesize 132
...
```

## <a name="where_to_get_help"/>Where to get help

To get help, open an issue. In the future I hope to provide help using something
else...

## <a name="contribution_guidelines"/>Contribution guidelines

To write this plugin I'm following [this manual](http://books.sonatype.com/mvnref-book/reference/writing-plugins-sect-custom-plugin.html).
You can find project notes [here](http://goo.gl/usnglW).

All contributions are welcome. The project uses a MIT License (as you can see
in the root of the project).
All you need to do is fork the project and send me a pull-request.
Thanks!

## <a name="contributor_list"/>Contributor list

  * [Sixro](http://github.com/sixro)

## <a name="credits_inspiration_alternatives"/>Credits, Inspiration, Alternatives

The main reason I created this tool, is to create quickly kit for Vodafone Italy Canvass.   
Besides, there are so many things to remember in order to create a valid kit that I need
to recreate it at least twice.   
I thought that a lot of those things was the perfect food for a computer and this is the
result.
