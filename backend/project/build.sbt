// Project-level build settings
// This file is evaluated during project definition loading

import sbt.util.Level

// Resolve scala-xml version conflicts
// Option 1: Allow eviction warnings instead of errors
ThisBuild / evictionErrorLevel := Level.Warn

// Option 2: Use libraryDependencySchemes to allow scala-xml version mismatches
// This tells SBT that scala-xml versions are compatible even if they differ
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "always"
