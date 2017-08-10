package eeg.useit.today.eegtoolkit.model;

/** Immutable snapshot of timeseries data. */
public class TimeSeriesSnapshot<T> {
  public static final TimeSeriesSnapshot EMPTY =
      new TimeSeriesSnapshot<Object>(new long[]{}, new Object[]{});

  /** Values in the timeseries. */
  public final T[] values;

  /** Timestamps of when the values were taken. */
  public final long[] timestamps;

  /** Number of values (and timestamps) in the snapshot. */
  public final int length;

  /** Creates a snapshot from (time, value) arrays. */
  public TimeSeriesSnapshot(long[] timestamps, T[] values) {
    assert timestamps.length == values.length;
    this.timestamps = timestamps;
    this.values = values;
    this.length = timestamps.length;
  }
}
