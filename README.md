# Derivation benchmarks

## JSON round trip

Compilation time of the module (with only 1 needed implicit)
```
                     Scala 2   Scala 3  Units
compilation of      cold hot  cold hot
circeGenericAuto      15   6    49  24      s
circeGenericSemi      14   7    11   3      s
circeMagnoliaAuto     15   5    56  36      s
circeMagnoliaSemi     14   9    14   6      s
jsoniterScalaSanely    -   -    12   3      s
jsoniterScalaSemi     11   4    10   2      s
```

Scala 2 runtime performance:
```
[info] Benchmark                          Mode  Cnt   Score   Error   Units
[info] JsonRoundTrips.circeGenericAuto   thrpt   10   6.778 ± 0.127  ops/ms
[info] JsonRoundTrips.circeGenericSemi   thrpt   10   7.160 ± 0.172  ops/ms
[info] JsonRoundTrips.circeMagnoliaAuto  thrpt   10   7.660 ± 0.132  ops/ms
[info] JsonRoundTrips.circeMagnoliaSemi  thrpt   10   8.081 ± 0.188  ops/ms
[info] JsonRoundTrips.jsoniterScalaSemi  thrpt   10  21.417 ± 0.140  ops/ms
```

Scala 3 runtime performance:
```
[info] Benchmark                            Mode  Cnt   Score   Error   Units
[info] JsonRoundTrips.circeGenericAuto     thrpt   10   0.403 ± 0.295  ops/ms
[info] JsonRoundTrips.circeGenericSemi     thrpt   10   4.528 ± 0.049  ops/ms
[info] JsonRoundTrips.circeMagnoliaAuto    thrpt   10   0.076 ± 0.040  ops/ms
[info] JsonRoundTrips.circeMagnoliaSemi    thrpt   10   5.476 ± 0.073  ops/ms
[info] JsonRoundTrips.jsoniterScalaSanely  thrpt   10  19.952 ± 0.159  ops/ms
[info] JsonRoundTrips.jsoniterScalaSemi    thrpt   10  21.414 ± 0.245  ops/ms
```
