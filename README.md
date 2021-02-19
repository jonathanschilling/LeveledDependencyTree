# LeveledDependecyTree

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
