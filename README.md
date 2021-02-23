# LeveledDependencyTree

This repository contains a Java implementation of a transform of a general list of objects
depending onto another (and possibly other objects outside the scope you want to consider)
into a data structure called a "leveled dependency tree".
The key idea is to re-group the objects into groups, so called 'levels',
where all objects in a given level only depend on objects in lower levels.

## Applications
There are many situations in which a leveled dependency tree can save you a lot of headaches.

### Maven projects
Consider a big (Java) programming project which consists of several Maven artifacts depending on one another.
Probably you have some 'core' artifacts which are used by the other, more advanced, artifacts.
When updating the core artifact, you need to propagate the updated version number into
all artifacts that depend on the core artifact(s).
However, your project is so large that also the other artifacts depend on one another.
In order to update the whole project tree (rooted on the core artifacts) in an orderly manner,
you would need to consider next the artifacts which only depend on the core artifact
(and possibly some out-of-tree artifacts).
If your project tree is sufficiently large, the complexity of which projects depend on which other projects
prohibits keeping all these dependencies in your head at once.

This is where the leveled dependency tree comes in handy.
It sorts the artifacts that belong to your project into "levels", where the first level
only contains your core artifact(s) and successive levels only depend on artifacts on lower level(s).
Thus, when updating the artifacts sorted by levels, you need to touch every artifact only exactly once
to update it. Artifacts on the same level are mutually independent on each other,
so you can parallelize the work required for updating a given level.

An example program to print the level structure for the [`netlib-java`](https://github.com/fommil/netlib-java) artifact is provided
as [`MavenExample.java`](https://github.com/jonathanschilling/LeveledDependencyTree/blob/master/src/test/java/de/labathome/ldt/examples/maven/MavenExample.java) in this repository.
The output looks as follows:

```
out-of-tree dependencies:
  net.sourceforge.f2j:arpack_combined_all:jar:0.1
  junit:junit:jar:4.11
  org.projectlombok:lombok:jar:1.12.2
level 1
  com.github.fommil.netlib:core:jar:1.1.2
  com.github.fommil:jniloader:jar:1.1
level 2
  com.github.fommil.netlib:native_ref-java:jar:1.1
  com.github.fommil.netlib:native_system-java:jar:1.1
level 3
  com.github.fommil.netlib:netlib-native_ref-osx-x86_64:jar:natives:1.1
  com.github.fommil.netlib:netlib-native_ref-linux-x86_64:jar:natives:1.1
  com.github.fommil.netlib:netlib-native_ref-linux-i686:jar:natives:1.1
  com.github.fommil.netlib:netlib-native_ref-win-x86_64:jar:natives:1.1
  com.github.fommil.netlib:netlib-native_ref-win-i686:jar:natives:1.1
  com.github.fommil.netlib:netlib-native_ref-linux-armhf:jar:natives:1.1
  com.github.fommil.netlib:netlib-native_system-osx-x86_64:jar:natives:1.1
  com.github.fommil.netlib:netlib-native_system-linux-x86_64:jar:natives:1.1
  com.github.fommil.netlib:netlib-native_system-linux-i686:jar:natives:1.1
  com.github.fommil.netlib:netlib-native_system-linux-armhf:jar:natives:1.1
  com.github.fommil.netlib:netlib-native_system-win-x86_64:jar:natives:1.1
  com.github.fommil.netlib:netlib-native_system-win-i686:jar:natives:1.1
level 4
  com.github.fommil.netlib:all:pom:1.1.2
```

When updating this project, you need to start with the artifacts `core` and `jniloader` before you can progress
to `native_*-java`. Once these are done, you can go on with the `netlib-native_*-*` artifacts
and finally, the `all` artifact can be built.

The corresponding dot graph is here:
![dot graph of netlib-java LDT](https://github.com/jonathanschilling/LeveledDependencyTree/blob/master/doc/dot_ldt.png "dot graph of netlib-java LDT")

### Grouping of (Java) classes into subprojects
The [Xj3D](https://sourceforge.net/projects/xj3d/) framework is a big Java project, consisting of several thousands(?) of classes
which are currently hosted as a [monolithic source tree on SourceForge](https://sourceforge.net/p/xj3d/code/HEAD/tree/trunk/).
Splitting this project up into seveval subproject would allow more fine-grained development
and is [actually considered by the project team](https://www.web3d.org/wiki/index.php/Xj3D_Evolution#Maven).
Using a leveled dependecy tree, the Xj3D classes could be split up into several Maven artifacts in an orderly manner.

## Availability
This project is available on Maven Central with the following Maven coordinates:

```xml
<dependency>
  <groupId>de.labathome</groupId>
	<artifactId>LeveledDependencyTree</artifactId>
	<version>1.0.0</version>
</dependency>
```

