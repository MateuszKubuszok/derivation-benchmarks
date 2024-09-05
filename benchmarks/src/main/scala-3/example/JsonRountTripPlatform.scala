package example

import org.openjdk.jmh.annotations.Benchmark

trait JsonRoundTripPlatform { this: JsonRoundTrips =>

  @Benchmark
  def jsoniterScalaSanely: Any = JsoniterScalaSanely.roundTrip(out)
}
