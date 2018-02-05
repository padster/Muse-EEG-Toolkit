package eeg.useit.today.eegtoolkit.model;

import java.io.Serializable;

/** Immutable snapshot of timeseries data. */
public class TimeSeriesSnapshot<T> implements Serializable {
  private static final long serialVersionUID = 1228347767823776957L;

  public static final TimeSeriesSnapshot EMPTY =
      new TimeSeriesSnapshot<>(new long[]{}, new Object[]{});

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
