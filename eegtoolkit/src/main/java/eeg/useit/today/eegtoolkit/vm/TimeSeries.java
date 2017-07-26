package eeg.useit.today.eegtoolkit.vm;

import android.databinding.BaseObservable;
import android.support.v4.util.CircularArray;

/** Time Series stored as a circular buffer. Values too old are removed. */
public class TimeSeries extends BaseObservable {
  /** Values within the timeseries. */
  private final CircularArray<Double> values = new CircularArray<>();

  /** Times at which the values were taken. */
  private final CircularArray<Long> timestampsMicro = new CircularArray<>();

  /** Values older than this will be removed. */
  private final long maxAgeMicro;

  /** Create a timeseries, giving it a maximum age (in ms) length to contain. */
  public TimeSeries(long maxAgeMS) {
    this.maxAgeMicro = maxAgeMS * 1000L;
  }

  /** @return Whether the time series has any data. */
  public boolean isEmpty() {
    return values.isEmpty();
  }

  /** Pushes a value to the end of the array. */
  public void pushValue(long timestampMicro, double value) {
    synchronized (this) {
      timestampsMicro.addLast(timestampMicro);
      values.addLast(value);
      notifyChange();
    }
  }

  /**
   * Take a snapshot of the current values, first culling any that are too old.
   */
  public TimeSeriesSnapshot getRecentSnapshot() {
    synchronized (this) {
      if (this.isEmpty()) {
        return TimeSeriesSnapshot.EMPTY;
      }

      // First, remove all the old values.
      long finalSnapshot = timestampsMicro.getLast();
      while (timestampsMicro.getFirst() + maxAgeMicro < finalSnapshot) {
        timestampsMicro.popFirst();
        values.popFirst();
      }

      // Next, snapshot into arrays and return:
      int n = this.values.size();
      long[] timestamps = new long[n];
      double[] values = new double[n];
      for (int i = 0; i < n; i++) {
        timestamps[i] = this.timestampsMicro.get(i);
        values[i] = this.values.get(i);
      }
      return new TimeSeriesSnapshot(timestamps, values);
    }
  }
}
