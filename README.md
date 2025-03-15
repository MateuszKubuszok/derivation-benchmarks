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

> `circeMagnolia` was based on https://github.com/vpavkin/circe-magnolia/ since I couldn't find
> any maintained up-to-date Magnolia-based derivation for Circe.

Compilation time of the module (with only 1 needed implicit)
```
                     Scala 2   Scala 3  Units
compilation of      cold hot  cold hot
circeGenericAuto       3   1    15   9      s
circeGenericSemi       3   1     2   0      s
circeMagnoliaAuto      3   1    23  17      s
circeMagnoliaSemi      3   0     3   0      s
jsoniterScalaSanely    -   -     3   0      s
jsoniterScalaSemi      2   0     2   0      s
```

Scala 2 runtime performance:
```
[info] Benchmark                          Mode  Cnt   Score   Error   Units
[info] JsonRoundTrips.circeGenericAuto   thrpt   10   8.392 ± 0.137  ops/ms
[info] JsonRoundTrips.circeGenericSemi   thrpt   10   8.815 ± 0.121  ops/ms
[info] JsonRoundTrips.circeMagnoliaAuto  thrpt   10   9.469 ± 0.165  ops/ms
[info] JsonRoundTrips.circeMagnoliaSemi  thrpt   10   9.443 ± 0.143  ops/ms
[info] JsonRoundTrips.jsoniterScalaSemi  thrpt   10  24.615 ± 0.101  ops/ms
```

Scala 3 runtime performance:
```
[info] Benchmark                            Mode  Cnt   Score   Error   Units
[info] JsonRoundTrips.circeGenericAuto     thrpt   10   3.394 ± 1.267  ops/ms
[info] JsonRoundTrips.circeGenericSemi     thrpt   10   8.047 ± 0.096  ops/ms
[info] JsonRoundTrips.circeMagnoliaAuto    thrpt   10   0.128 ± 0.062  ops/ms
[info] JsonRoundTrips.circeMagnoliaSemi    thrpt   10   6.176 ± 0.104  ops/ms
[info] JsonRoundTrips.jsoniterScalaSanely  thrpt   10  24.460 ± 0.149  ops/ms
[info] JsonRoundTrips.jsoniterScalaSemi    thrpt   10  24.396 ± 0.116  ops/ms
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

> `showMacros` (used by `showSanely`) were implemented in a naive way, just inlining everything:
>  - the first version generated code that didn't fit the method limit
>  - which is why the code on [`master`](https://github.com/MateuszKubuszok/derivation-benchmarks/tree/master)
>    uses `lazy val`s to split the code internally into smaller methods (but still in a naive way)
>  - meanwhile the code on [`cache`](https://github.com/MateuszKubuszok/derivation-benchmarks/tree/cache)
>    uses `def`s to reuse the derived code and save time both during compilation and in runtime

Compilation time of the module (with only 1 needed implicit)
```
                            Scala 2   Scala 3  Units
compilation of             cold hot  cold hot
showGenericProgrammingAuto    3   1     7   3      s
showGenericProgrammingSemi    2   0     2   0      s
showMagnoliaAuto              2   0    14   8      s
showMagnoliaSemi              2   0     2   0      s
showSanely                    1   0     2   0      s
```

Scala 2 runtime performance:
```
[info] Benchmark                                Mode  Cnt  Score   Error   Units
[info] ShowOutputs.showGenericProgrammingAuto  thrpt   10  5.584 ± 0.067  ops/ms
[info] ShowOutputs.showGenericProgrammingSemi  thrpt   10  5.953 ± 0.057  ops/ms
[info] ShowOutputs.showMagnoliaAuto            thrpt   10  6.702 ± 0.106  ops/ms
[info] ShowOutputs.showMagnoliaSemi            thrpt   10  6.605 ± 0.289  ops/ms
[info] ShowOutputs.showSanely                  thrpt   10  8.201 ± 0.078  ops/ms
```

Scala 3 runtime performance:
```
[info] Benchmark                                Mode  Cnt  Score   Error   Units
[info] ShowOutputs.showGenericProgrammingAuto  thrpt   10  3.687 ± 1.588  ops/ms
[info] ShowOutputs.showGenericProgrammingSemi  thrpt   10  6.291 ± 0.062  ops/ms
[info] ShowOutputs.showMagnoliaAuto            thrpt   10  0.265 ± 0.194  ops/ms
[info] ShowOutputs.showMagnoliaSemi            thrpt   10  8.316 ± 0.067  ops/ms
[info] ShowOutputs.showSanely                  thrpt   10  9.191 ± 0.081  ops/ms
```
