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

val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % "1.0.1" % Test
  ),
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) => Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
        compilerPlugin("org.typelevel" % "kind-projector" % "0.13.3" cross CrossVersion.full)
      )
      case _            => Seq.empty
    }
  },
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) => Seq("-no-indent", "-Ykind-projector:underscores")
      case _            => Seq("-Xsource:3")
    }
  }
)

// all projects

lazy val root = project
  .in(file("."))
  .aggregate(testClasses.projectRefs *)
  .aggregate(showGenericProgramming.projectRefs *)
  .aggregate(showGenericProgrammingAuto.projectRefs *)
  .aggregate(showGenericProgrammingSemi.projectRefs *)
  .aggregate(showMagnolia.projectRefs *)
  .aggregate(showMagnoliaAuto.projectRefs *)
  .aggregate(showMagnoliaSemi.projectRefs *)
  .aggregate(showMacros.projectRefs *)
  .aggregate(showSanely.projectRefs *)
  .aggregate(circeGenericAuto.projectRefs *)
  .aggregate(circeGenericSemi.projectRefs *)
  .aggregate(circeMagnolia.projectRefs *)
  .aggregate(circeMagnoliaAuto.projectRefs *)
  .aggregate(circeMagnoliaSemi.projectRefs *)
  .aggregate(jsoniterScalaWrapper.projectRefs *)
  .aggregate(jsoniterScalaAuto.projectRefs *)
  .aggregate(jsoniterScalaSanely.projectRefs *)
  .aggregate(jsoniterScalaSemi.projectRefs *)

// classes for which we will derive things

lazy val testClasses = projectMatrix
  .in(file("test-classes"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)

// Show-related experiments:

lazy val showGenericProgramming = projectMatrix
  .in(file("show-generic-programming"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
  .settings(
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq() // Scala 3 has mirrors
        case Some((2, _)) => Seq("com.chuusai" %% "shapeless" % "2.3.12")
        case _            => ???
      }
    }
  )

lazy val showGenericProgrammingAuto = projectMatrix
  .in(file("show-generic-programming-auto"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
  .settings(
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq("-Xmax-inlines", "64")
        case Some((2, _)) => Seq()
        case _            => ???
      }
    }
  )
  .dependsOn(testClasses, showGenericProgramming)

lazy val showGenericProgrammingSemi = projectMatrix
  .in(file("show-generic-programming-semi"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
  .dependsOn(testClasses, showGenericProgramming)

lazy val showMagnolia = projectMatrix
  .in(file("show-magnolia"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
  .settings(
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq("com.softwaremill.magnolia1_3" %% "magnolia" % "1.3.7")
        case Some((2, _)) => Seq("com.softwaremill.magnolia1_2" %% "magnolia" % "1.1.10")
        case _            => ???
      }
    }
  )

lazy val showMagnoliaAuto = projectMatrix
  .in(file("show-magnolia-auto"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
  .settings(
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq("-Xmax-inlines", "64")
        case Some((2, _)) => Seq()
        case _            => ???
      }
    }
  )
  .dependsOn(testClasses, showMagnolia)

lazy val showMagnoliaSemi = projectMatrix
  .in(file("show-magnolia-semi"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
  .dependsOn(testClasses, showMagnolia)

lazy val showMacros = projectMatrix
  .in(file("show-macros"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
  .settings(
    libraryDependencies += "io.scalaland" %% "chimney-macro-commons" % "1.4.0"
  )

lazy val showSanely = projectMatrix
  .in(file("show-sanely"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
  .dependsOn(testClasses, showMacros)

// Circe-related experiments

lazy val circeGenericAuto = projectMatrix
  .in(file("circe-generic-auto"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
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
  .settings(commonSettings *)
  .settings(
    libraryDependencies += "io.circe" %% "circe-generic" % "0.14.9"
  )
  .dependsOn(testClasses)

// apparently all Magnolia-based Circe integrations are outdated(?)
lazy val circeMagnolia = projectMatrix
  .in(file("circe-magnolia"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
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
  .settings(commonSettings *)
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
  .settings(commonSettings *)
  .dependsOn(
    testClasses,
    circeMagnolia
  )

// Jsoniter Scala-related experiments

lazy val jsoniterScalaWrapper = projectMatrix
  .in(file("jsoniter-scala-wrapper"))
  .someVariations(List(versions.scala3), versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
  .settings(
    libraryDependencies += "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.30.9",
    libraryDependencies += "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.30.9"
  )

lazy val jsoniterScalaSanely = projectMatrix
  .in(file("jsoniter-scala-sanely"))
  .someVariations(List(versions.scala3), versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
  .dependsOn(testClasses, jsoniterScalaWrapper)

lazy val jsoniterScalaAuto = projectMatrix
  .in(file("jsoniter-scala-auto"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
  .settings(
    libraryDependencies += "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.30.11",
    libraryDependencies += "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.30.11"
  )
  .dependsOn(testClasses)

lazy val jsoniterScalaSemi = projectMatrix
  .in(file("jsoniter-scala-semi"))
  .someVariations(versions.scalas, versions.platforms)(only1VersionInIDE *)
  .settings(commonSettings *)
  .settings(
    libraryDependencies += "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.30.11",
    libraryDependencies += "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.30.11"
  )
  .dependsOn(testClasses)

lazy val benchmarks = projectMatrix
  .in(file("benchmarks"))
  .someVariations(versions.scalas, versions.platforms)(
    (MatrixAction
      .ForScala(_.isScala3)
      .Configure(_.dependsOn(jsoniterScalaSanely.jvm(versions.scala3))) +: only1VersionInIDE).toSeq *
  )
  .dependsOn(
    showGenericProgrammingAuto,
    showGenericProgrammingSemi,
    showMagnoliaAuto,
    showMagnoliaSemi,
    showSanely,
    circeGenericAuto,
    circeGenericSemi,
    circeMagnoliaAuto,
    circeMagnoliaSemi,
    jsoniterScalaAuto,
    jsoniterScalaSemi
  )
  .enablePlugins(JmhPlugin)
