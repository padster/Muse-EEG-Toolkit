package eeg.useit.today.eegtoolkit.vm;

/** Immutable snapshot of timeseries data. */
public class TimeSeriesSnapshot {
  public static final TimeSeriesSnapshot EMPTY =
      new TimeSeriesSnapshot(new long[]{}, new double[]{});

  /** Values in the timeseries. */
  public final double[] values;

  /** Timestamps of when the values were taken. */
  public final long[] timestamps;

  /** Number of values (and timestamps) in the snapshot. */
  public final int length;

  /** Creates a snapshot from (time, value) arrays. */
  public TimeSeriesSnapshot(long[] timestamps, double[] values) {
    assert timestamps.length == values.length;
    this.timestamps = timestamps;
    this.values = values;
    this.length = timestamps.length;
  }
}
