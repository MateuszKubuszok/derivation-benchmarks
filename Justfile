[doc('Compile each measured project with cold JVM (like on CI)')]
coldJvmCompile:
  sbt -error --no-server \
    testClasses/compile \
    testClasses3/compile \
    circeMagnolia/compile \
    circeMagnolia/compile \
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

[doc('Compile each measured project with how JVM (like with development)')]
hotJvmCompile:
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
    projects

[doc('Benchmark runtime')]
runtime:
  sbt --no-server \
    benchmarks/Jmh/run \
    benchmarks3/Jmh/run \
    projects