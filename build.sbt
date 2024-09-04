import commandmatrix.extra.*

val versions = new {
  val scala2 = "2.13.14"
  val scala3 = "3.3.3"

  // Which versions should be cross-compiled for publishing
  val scalas = List(scala2, scala3)
  val platforms = List(VirtualAxis.jvm)

  // Which version should be used in IntelliJ
  val ideScala = scala3
  val idePlatform = VirtualAxis.jvm
}

Global / excludeLintKeys += ideSkipProject
val only1VersionInIDE =
  MatrixAction
    .ForPlatform(versions.idePlatform)
    .Configure(
      _.settings(
        ideSkipProject := (scalaVersion.value != versions.ideScala),
        bspEnabled := (scalaVersion.value == versions.ideScala)
      )
    ) +:
    versions.platforms.filter(_ != versions.idePlatform).map { platform =>
      MatrixAction
        .ForPlatform(platform)
        .Configure(_.settings(ideSkipProject := true, bspEnabled := false))
    }

val dependencies = Seq(
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % "1.0.1" % Test
  ),
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        Seq(
          "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
        )
      case _ => Seq.empty
    }
  }
)

// all projects

lazy val root = project
  .in(file("."))
  .aggregate(testClasses.projectRefs *)
  .aggregate(circeGenericAuto.projectRefs *)
  .aggregate(circeGenericSemi.projectRefs *)
  .aggregate(circeMagnolia.projectRefs *)
  .aggregate(circeMagnoliaAuto.projectRefs *)
  .aggregate(circeMagnoliaSemi.projectRefs *)
  .aggregate(jsoniterScalaSemi.projectRefs *)

// classes for which we will derive things

lazy val testClasses = projectMatrix
  .in(file("test-classes"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(dependencies *)

// Circe-related experiments

lazy val circeGenericAuto = projectMatrix
  .in(file("circe-generic-auto"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(dependencies *)
  .settings(
    libraryDependencies += "io.circe" %%% "circe-generic" % "0.14.9",
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq("-Xmax-inlines", "64")
        case Some((2, _)) => Seq()
        case _            => ???
      }
    }
  )
  .dependsOn(testClasses)

lazy val circeGenericSemi = projectMatrix
  .in(file("circe-generic-semi"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(dependencies *)
  .settings(
    libraryDependencies += "io.circe" %% "circe-generic" % "0.14.9"
  )
  .dependsOn(testClasses)

// apparently all Magnolia-based Circe integrations are outdated(?)
lazy val circeMagnolia = projectMatrix
  .in(file("circe-magnolia"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(dependencies *)
  .settings(
    libraryDependencies += "io.circe" %%% "circe-core" % "0.14.9",
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq("com.softwaremill.magnolia1_3" %% "magnolia" % "1.3.7")
        case Some((2, _)) => Seq("com.softwaremill.magnolia1_2" %% "magnolia" % "1.1.10")
        case _            => ???
      }
    }
  )

lazy val circeMagnoliaAuto = projectMatrix
  .in(file("circe-magnolia-auto"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(dependencies *)
  .settings(
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq("-Xmax-inlines", "64")
        case Some((2, _)) => Seq()
        case _            => ???
      }
    }
  )
  .dependsOn(testClasses, circeMagnolia)

lazy val circeMagnoliaSemi = projectMatrix
  .in(file("circe-magnolia-semi"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(dependencies *)
  .dependsOn(testClasses, circeMagnolia)

// Jsoniter Scala-related experiments

lazy val jsoniterScalaSemi = projectMatrix
  .in(file("jsoniter-scala-semi"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(dependencies *)
  .settings(
    libraryDependencies += "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.30.9",
    libraryDependencies += "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.30.9"
  )
  .dependsOn(testClasses)

lazy val benchmarks = projectMatrix
  .in(file("benchmarks"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .dependsOn(circeGenericAuto, circeGenericSemi, circeMagnoliaAuto, circeMagnoliaSemi, jsoniterScalaSemi)
  .enablePlugins(JmhPlugin)
