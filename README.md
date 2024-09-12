# Derivation benchmarks

Take a look at [_Slow Auto, Inconvenient Semi_ presentation](https://github.com/MateuszKubuszok/SlowAutoInconvenientSemi).

## JSON round trip

Projects:

 * `circeGenericAuto` - 1 JSON roundtrip with `import io.circe.generic.auto`
 * `circeGenericSemi` - 1 JSON roundtrip with `import io.circe.generic.semiauto`
 * `circeMagnoliaAuto` - 1 JSON roundtrip with automatic derivation implemented with Magnolia
 * `circeMagnoliaSemi` - 1 JSON roundtrip with semi-automatic derivation implemented with Magnolia
 * `jsoniterScalaSemi` - 1 JSON roundtrip with recursive semi-automatic derivation with Jsoniter Scala
 * `jsoniterScalaSanely` - 1 JSON roundtrip with sanely-automatic derivation using Jsoniter Scala under the hood (on Scala 3)

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

## Show output generation

Projects:

* `showGenericProgrammingAuto` - `Show` output of a value generated with automatic derivation
  (implemented with Shapeless on Scala 2, Mirrors on Scala 3)
* `showGenericProgrammingSemi` - `Show` output of a value generated with semiautomatic derivation
  (implemented with Shapeless on Scala 2, Mirrors on Scala 3)
* `showMagnoliaAuto` - `Show` output of a value generated with automatic derivation (implemented with Magnolia)
* `showMagnoliaSemi` - `Show` output of a value generated with semi-automatic derivation (implemented with Magnolia)
* `showSanely` - `Show` output of a value generated with sanely-automatic derivation
  (implemented with cross-compilable macros and Chimney-Macro-Commons)

Compilation time of the module (with only 1 needed implicit)
```
                            Scala 2   Scala 3  Units
compilation of             cold hot  cold hot
showGenericProgrammingAuto   14   4    53  31      s
showGenericProgrammingSemi   10   2    10   2      s
showMagnoliaAuto             10   1    43  17      s
showMagnoliaSemi             11   2    10   2      s
showSanely                   15   2    17   4      s
```

Scala 2 runtime performance:
```
[info] Benchmark                                Mode  Cnt  Score   Error   Units
[info] ShowOutputs.showGenericProgrammingAuto  thrpt   10  2.731 ± 0.034  ops/ms
[info] ShowOutputs.showGenericProgrammingSemi  thrpt   10  2.791 ± 0.017  ops/ms
[info] ShowOutputs.showMagnoliaAuto            thrpt   10  3.440 ± 0.093  ops/ms
[info] ShowOutputs.showMagnoliaSemi            thrpt   10  3.670 ± 0.018  ops/ms
[info] ShowOutputs.showSanely                  thrpt   10  2.194 ± 0.358  ops/ms
```

Scala 3 runtime performance:
```
[info] Benchmark                                Mode  Cnt  Score   Error   Units
[info] ShowOutputs.showGenericProgrammingAuto  thrpt   10  0.150 ± 0.011  ops/ms
[info] ShowOutputs.showGenericProgrammingSemi  thrpt   10  3.410 ± 0.026  ops/ms
[info] ShowOutputs.showMagnoliaAuto            thrpt   10  0.089 ± 0.024  ops/ms
[info] ShowOutputs.showMagnoliaSemi            thrpt   10  3.888 ± 0.066  ops/ms
[info] ShowOutputs.showSanely                  thrpt   10  2.159 ± 0.433  ops/ms
```
