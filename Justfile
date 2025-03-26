[doc('Compile each measured JSON project with cold JVM (like on CI)')]
coldJvmCompileJSONs:
  sbt -error --no-server \
    testClasses/compile \
    testClasses3/compile \
    circeMagnolia/compile \
    circeMagnolia3/compile \
    update
  sbt --no-server circeGenericAuto/clean     "show circeGenericAuto/name"     circeGenericAuto/compile
  sbt --no-server circeGenericAuto3/clean    "show circeGenericAuto3/name"    circeGenericAuto3/compile
  sbt --no-server circeGenericSemi/clean     "show circeGenericSemi/name"     circeGenericSemi/compile
  sbt --no-server circeGenericSemi3/clean    "show circeGenericSemi3/name"    circeGenericSemi3/compile
  sbt --no-server circeMagnoliaAuto/clean    "show circeMagnoliaAuto/name"    circeMagnoliaAuto/compile
  sbt --no-server circeMagnoliaAuto3/clean   "show circeMagnoliaAuto3/name"   circeMagnoliaAuto3/compile
  sbt --no-server circeMagnoliaSemi/clean    "show circeMagnoliaSemi/name"    circeMagnoliaSemi/compile
  sbt --no-server circeMagnoliaSemi3/clean   "show circeMagnoliaSemi3/name"   circeMagnoliaSemi3/compile
  sbt --no-server jsoniterScalaSanely3/clean "show jsoniterScalaSanely3/name" jsoniterScalaSanely3/compile
  sbt --no-server jsoniterScalaSemi/clean    "show jsoniterScalaSemi/name"    jsoniterScalaSemi/compile
  sbt --no-server jsoniterScalaSemi3/clean   "show jsoniterScalaSemi3/name"   jsoniterScalaSemi3/compile
  sbt --no-server ziojsonGenericSemi/clean   "show ziojsonGenericSemi/name"   ziojsonGenericSemi/compile
  sbt --no-server ziojsonGenericSemi3/clean  "show ziojsonGenericSemi3/name"  ziojsonGenericSemi3/compile

[doc('Compile each measured JSON project with how JVM (like with development)')]
hotJvmCompileJSONs:
  sbt --no-server \
    "set logLevel := Level.Error" \
    circeGenericAuto/clean     circeGenericAuto/compile \
    circeGenericAuto3/clean    circeGenericAuto3/compile \
    circeGenericSemi/clean     circeGenericSemi/compile \
    circeGenericSemi3/clean    circeGenericSemi3/compile \
    circeMagnoliaAuto/clean    circeMagnoliaAuto/compile \
    circeMagnoliaAuto3/clean   circeMagnoliaAuto3/compile \
    circeMagnoliaSemi/clean    circeMagnoliaSemi/compile \
    circeMagnoliaSemi3/clean   circeMagnoliaSemi3/compile \
    jsoniterScalaSanely3/clean jsoniterScalaSanely3/compile \
    jsoniterScalaSemi/clean    jsoniterScalaSemi/compile \
    jsoniterScalaSemi3/clean   jsoniterScalaSemi3/compile \
    "set logLevel := Level.Info" \
    circeGenericAuto/clean     "show circeGenericAuto/name"     circeGenericAuto/compile \
    circeGenericAuto3/clean    "show circeGenericAuto3/name"    circeGenericAuto3/compile \
    circeGenericSemi/clean     "show circeGenericSemi/name"     circeGenericSemi/compile \
    circeGenericSemi3/clean    "show circeGenericSemi3/name"    circeGenericSemi3/compile \
    circeMagnoliaAuto/clean    "show circeMagnoliaAuto/name"    circeMagnoliaAuto/compile \
    circeMagnoliaAuto3/clean   "show circeMagnoliaAuto3/name"   circeMagnoliaAuto3/compile \
    circeMagnoliaSemi/clean    "show circeMagnoliaSemi/name"    circeMagnoliaSemi/compile  \
    circeMagnoliaSemi3/clean   "show circeMagnoliaSemi3/name"   circeMagnoliaSemi3/compile \
    jsoniterScalaSanely3/clean "show jsoniterScalaSanely3/name" jsoniterScalaSanely3/compile \
    jsoniterScalaSemi/clean    "show jsoniterScalaSemi/name"    jsoniterScalaSemi/compile \
    jsoniterScalaSemi3/clean   "show jsoniterScalaSemi3/name"   jsoniterScalaSemi3/compile \
    ziojsonGenericSemi/clean   "show ziojsonGenericSemi/name"   ziojsonGenericSemi/compile \
    ziojsonGenericSemi3/clean  "show ziojsonGenericSemi3/name"  ziojsonGenericSemi3/compile \
    projects

[doc('Benchmark runtime')]
runtimeJSONs:
  sbt --no-server \
    "benchmarks/Jmh/run JsonRoundTrips" \
    "benchmarks3/Jmh/run JsonRoundTrips" \
    projects

[doc('Compile each measured Show project with cold JVM (like on CI)')]
coldJvmCompileShows:
  sbt  --no-server \
    testClasses/compile \
    testClasses3/compile \
    showGenericProgramming/compile \
    showGenericProgramming3/compile \
    showMagnolia/compile \
    showMagnolia3/compile \
    showMacros/compile \
    showMacros3/compile \
    update
  sbt --no-server showGenericProgrammingAuto/clean  "show showGenericProgrammingAuto/name"  showGenericProgrammingAuto/compile
  sbt --no-server showGenericProgrammingAuto3/clean "show showGenericProgrammingAuto3/name" showGenericProgrammingAuto3/compile
  sbt --no-server showGenericProgrammingSemi/clean  "show showGenericProgrammingSemi/name"  showGenericProgrammingSemi/compile
  sbt --no-server showGenericProgrammingSemi3/clean "show showGenericProgrammingSemi3/name" showGenericProgrammingSemi3/compile
  sbt --no-server showMagnoliaAuto/clean            "show showMagnoliaAuto/name"            showMagnoliaAuto/compile
  sbt --no-server showMagnoliaAuto3/clean           "show showMagnoliaAuto3/name"           showMagnoliaAuto3/compile
  sbt --no-server showMagnoliaSemi/clean            "show showMagnoliaSemi/name"            showMagnoliaSemi/compile
  sbt --no-server showMagnoliaSemi3/clean           "show showMagnoliaSemi3/name"           showMagnoliaSemi3/compile
  sbt --no-server showSanely/clean                  "show showSanely/name"                  showSanely/compile
  sbt --no-server showSanely3/clean                 "show showSanely3/name"                 showSanely3/compile

[doc('Compile each measured JSON project with how JVM (like with development)')]
hotJvmCompileShows:
  sbt --no-server \
    "set logLevel := Level.Error" \
    showGenericProgrammingAuto/clean  showGenericProgrammingAuto/compile \
    showGenericProgrammingAuto3/clean showGenericProgrammingAuto3/compile \
    showGenericProgrammingSemi/clean  showGenericProgrammingSemi/compile \
    showGenericProgrammingSemi3/clean showGenericProgrammingSemi3/compile \
    showMagnoliaAuto/clean            showMagnoliaAuto/compile \
    showMagnoliaAuto3/clean           showMagnoliaAuto3/compile \
    showMagnoliaSemi/clean            showMagnoliaSemi/compile \
    showMagnoliaSemi3/clean           showMagnoliaSemi3/compile \
    showSanely/clean                  showSanely/compile \
    showSanely3/clean                 showSanely3/compile \
    "set logLevel := Level.Info" \
    showGenericProgrammingAuto/clean  "show showGenericProgrammingAuto/name"  showGenericProgrammingAuto/compile \
    showGenericProgrammingAuto3/clean "show showGenericProgrammingAuto3/name" showGenericProgrammingAuto3/compile \
    showGenericProgrammingSemi/clean  "show showGenericProgrammingSemi/name"  showGenericProgrammingSemi/compile \
    showGenericProgrammingSemi3/clean "show showGenericProgrammingSemi3/name" showGenericProgrammingSemi3/compile \
    showMagnoliaAuto/clean            "show showMagnoliaAuto/name"            showMagnoliaAuto/compile \
    showMagnoliaAuto3/clean           "show showMagnoliaAuto3/name"           showMagnoliaAuto3/compile \
    showMagnoliaSemi/clean            "show showMagnoliaSemi/name"            showMagnoliaSemi/compile \
    showMagnoliaSemi3/clean           "show showMagnoliaSemi3/name"           showMagnoliaSemi3/compile \
    showSanely/clean                  "show showSanely/name"                  showSanely/compile \
    showSanely3/clean                 "show showSanely3/name"                 showSanely3/compile \
    projects

[doc('Benchmark runtime')]
runtimeShows:
  sbt --no-server \
    "benchmarks/Jmh/run ShowOutputs" \
    "benchmarks3/Jmh/run ShowOutputs" \
    projects