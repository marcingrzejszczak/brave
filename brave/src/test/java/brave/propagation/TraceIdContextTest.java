package brave.propagation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TraceIdContextTest {
  TraceIdContext context = TraceIdContext.newBuilder().traceId(333L).build();

  @Test public void compareUnequalIds() {
    assertThat(context)
        .isNotEqualTo(context.toBuilder().traceIdHigh(222L).build());
  }

  @Test public void compareEqualIds() {
    assertThat(context)
        .isEqualTo(TraceIdContext.newBuilder().traceId(333L).build());
  }

  @Test public void testToString_lo() {
    assertThat(context.toString())
        .isEqualTo("000000000000014d");
  }

  @Test public void testToString() {
    assertThat(context.toBuilder().traceIdHigh(222L).build().toString())
        .isEqualTo("00000000000000de000000000000014d");
  }

  @Test public void canUsePrimitiveOverloads() {
    TraceIdContext primitives = context.toBuilder()
        .sampled(true)
        .debug(true)
        .build();

    TraceIdContext objects = context.toBuilder()
        .sampled(Boolean.TRUE)
        .debug(Boolean.TRUE)
        .build();

    assertThat(primitives)
        .isEqualToComparingFieldByField(objects);
  }

  @Test public void parseTraceId_128bit() {
    String traceIdString = "463ac35c9f6413ad48485a3953bb6124";

    TraceContext.Builder builder = parseGoodTraceID(traceIdString);

    assertThat(builder.build().traceIdString())
        .isEqualTo(traceIdString);
  }

  @Test public void parseTraceId_64bit() {
    String traceIdString = "48485a3953bb6124";

    TraceContext.Builder builder = parseGoodTraceID(traceIdString);

    assertThat(builder.build().traceIdString())
        .isEqualTo(traceIdString);
  }

  @Test public void parseTraceId_short128bit() {
    String traceIdString = "3ac35c9f6413ad48485a3953bb6124";

    TraceContext.Builder builder = parseGoodTraceID(traceIdString);

    assertThat(builder.build().traceIdString())
        .isEqualTo("00" + traceIdString);
  }

  @Test public void parseTraceId_short64bit() {
    String traceIdString = "6124";

    TraceContext.Builder builder = parseGoodTraceID(traceIdString);

    assertThat(builder.build().traceIdString())
        .isEqualTo("000000000000" + traceIdString);
  }

  /**
   * Trace ID is a required parameter, so it cannot be null empty malformed or other nonsense.
   *
   * <p>Notably, this shouldn't throw exception or allocate anything
   */
  @Test public void parseTraceId_malformedReturnsFalse() {
    parseBadTraceId("463acL$c9f6413ad48485a3953bb6124");
    parseBadTraceId("holy 💩");
    parseBadTraceId("-");
    parseBadTraceId("");
    parseBadTraceId(null);
  }

  static TraceContext.Builder parseGoodTraceID(String traceIdString) {
    TraceContext.Builder builder = TraceContext.newBuilder().spanId(1L);
    Propagation.Getter<String, String> getter = (c, k) -> traceIdString;
    assertThat(builder.parseTraceId(getter, "headers", "trace-id"))
        .isTrue();
    return builder;
  }

  void parseBadTraceId(String traceIdString) {
    TraceContext.Builder builder = TraceContext.newBuilder().spanId(1L);
    Propagation.Getter<String, String> getter = (c, k) -> traceIdString;
    assertThat(builder.parseTraceId(getter, "headers", "trace-id"))
        .isFalse();
    assertThat(builder.traceIdHigh).isZero();
    assertThat(builder.traceId).isZero();
  }
}
