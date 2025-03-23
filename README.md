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
circeGenericAuto      14   4    46  16      s
circeGenericSemi      12   3    10   1      s
circeMagnoliaAuto     13   2    65  32      s
circeMagnoliaSemi     12   7    12   2      s
jsoniterScalaSanely    -   -     9   1      s
jsoniterScalaSemi     10   4     8   1      s
```

Scala 2 runtime performance:
```
[info] Benchmark                          Mode  Cnt   Score   Error   Units
[info] JsonRoundTrips.circeGenericAuto    thrpt  10   7.319 ± 0.011  ops/ms
[info] JsonRoundTrips.circeGenericSemi    thrpt  10   6.775 ± 0.013  ops/ms
[info] JsonRoundTrips.circeMagnoliaAuto   thrpt  10   7.689 ± 0.013  ops/ms
[info] JsonRoundTrips.circeMagnoliaSemi   thrpt  10   7.838 ± 0.013  ops/ms
[info] JsonRoundTrips.jsoniterScalaSemi   thrpt  10  20.081 ± 0.151  ops/ms
```

Scala 3 runtime performance:
```
[info] Benchmark                            Mode  Cnt   Score   Error   Units
[info] JsonRoundTrips.circeGenericAuto     thrpt   10   0.490 ± 0.432  ops/ms
[info] JsonRoundTrips.circeGenericSemi     thrpt   10   4.607 ± 0.014  ops/ms
[info] JsonRoundTrips.circeMagnoliaAuto    thrpt   10   0.077 ± 0.039  ops/ms
[info] JsonRoundTrips.circeMagnoliaSemi    thrpt   10   5.590 ± 0.013  ops/ms
[info] JsonRoundTrips.jsoniterScalaSanely  thrpt   10  21.408 ± 0.070  ops/ms
[info] JsonRoundTrips.jsoniterScalaSemi    thrpt   10  21.480 ± 0.070  ops/ms
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
>  - since this ability is quite useful and the code implementing it looks scary this utility was added
>    to [Chimney Macro Commons](https://chimney.readthedocs.io/en/stable/cookbook/#chimney-macro-commons)
>    1.5.0 - [`cache-using-chimney`](https://github.com/MateuszKubuszok/derivation-benchmarks/tree/cache-using-chimney)
>    looks much less terrifying :)
>  - I turned that branch into a [GitHub template](https://github.com/scalalandio/chimney-macro-commons-template)
>    if you wanted to experiment with cross-compilable macros ;)

> Scala 3.7.0 changed the way implicits works, this breaks the flow of our sanely-automatic pattern,
> but in turn we have access to [`Expr.summonIgnoring`](https://github.com/scala/scala3/discussions/21909).
> This was implemented on [`summon-ignoring-3.7`](https://github.com/MateuszKubuszok/derivation-benchmarks/tree/cache)
> in `showMacros373`.

Compilation time of the module (with only 1 needed implicit)
```
                            Scala 2   Scala 3  Units
compilation of             cold hot  cold hot
showGenericProgrammingAuto   15   5    53  29      s
showGenericProgrammingSemi   10   2    10   2      s
showMagnoliaAuto             10   1    43  15      s
showMagnoliaSemi             10   2     9   1      s
showSanely                   14   4    16   5      s
```

Scala 2 runtime performance:
```
[info] Benchmark                                Mode  Cnt  Score   Error   Units
[info] ShowOutputs.showGenericProgrammingAuto  thrpt   10  2.651 ± 0.012  ops/ms
[info] ShowOutputs.showGenericProgrammingSemi  thrpt   10  2.829 ± 0.033  ops/ms
[info] ShowOutputs.showMagnoliaAuto            thrpt   10  3.621 ± 0.017  ops/ms
[info] ShowOutputs.showMagnoliaSemi            thrpt   10  3.745 ± 0.028  ops/ms
[info] ShowOutputs.showSanely                  thrpt   10  2.202 ± 0.359  ops/ms
```

Scala 3 runtime performance:
```
[info] Benchmark                                Mode  Cnt  Score   Error   Units
[info] ShowOutputs.showGenericProgrammingAuto  thrpt   10  0.156 ± 0.013  ops/ms
[info] ShowOutputs.showGenericProgrammingSemi  thrpt   10  3.492 ± 0.013  ops/ms
[info] ShowOutputs.showMagnoliaAuto            thrpt   10  0.090 ± 0.023  ops/ms
[info] ShowOutputs.showMagnoliaSemi            thrpt   10  3.918 ± 0.012  ops/ms
[info] ShowOutputs.showSanely                  thrpt   10  2.204 ± 0.396  ops/ms
```
