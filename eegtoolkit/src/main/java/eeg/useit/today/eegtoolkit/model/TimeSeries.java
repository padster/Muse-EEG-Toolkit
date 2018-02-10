package eeg.useit.today.eegtoolkit.model;

import android.databinding.BaseObservable;
import android.support.v4.util.CircularArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/** Time Series stored as a circular buffer. Values too old are removed. */
public class TimeSeries<T> extends BaseObservable implements LiveSeries<T> {
  public static <T> TimeSeries<T> fromMaxAgeMS(long maxAgeMS) {
    return new TimeSeries<T>(maxAgeMS, -1);
  }
  public static <T> TimeSeries<T> fromMaxLength(int maxSampleCount) {
    return new TimeSeries<T>(-1, maxSampleCount);
  }

  /** Values within the timeseries. */
  private final CircularArray<T> values = new CircularArray<>();

  /** Times at which the values were taken. */
  private final CircularArray<Long> timestampsMicro = new CircularArray<>();

  /** Values older than this will be removed. */
  private final long maxAgeMicro;

  /** Only store this many values. */
  private final int maxSampleCount;

  /** Things interested on when this series changes. */
  private final List<Listener<T>> listeners = new ArrayList<>();

  /** Create a timeseries, giving it a maximum age (in ms) length to contain. */
  public TimeSeries(long maxAgeMS) {
    this(maxAgeMS, -1);
  }

  /** Create a timeseries, giving either maximum age (in ms) or maximum sample count. */
  public TimeSeries(long maxAgeMS, int maxSampleCount) {
    this.maxAgeMicro = maxAgeMS == -1 ? -1 : maxAgeMS * 1000L;
    this.maxSampleCount = maxSampleCount;
    assert this.maxAgeMicro == -1 ^ this.maxSampleCount == -1;
  }

  /** @return Whether the time series has any data. */
  public boolean isEmpty() {
    return values.isEmpty();
  }

  /** Pushes a value to the end of the array. */
  public void pushValue(long timestampMicro, T value) {
    synchronized (this) {
      timestampsMicro.addLast(timestampMicro);
      values.addLast(value);
      for (Listener<T> listener : listeners) {
        listener.valueAdded(timestampMicro, value);
      }
      notifyChange();
    }
  }

  /**
   * Take a snapshot of the current values, first culling any that are too old.
   */
  public TimeSeriesSnapshot<T> getRecentSnapshot(Class<T> clazz) {
    synchronized (this) {
      if (this.isEmpty()) {
        return TimeSeriesSnapshot.EMPTY;
      }

      // First, remove all the old values.
      long finalSnapshot = timestampsMicro.getLast();
      if (maxSampleCount == -1) {
        while (timestampsMicro.getFirst() + maxAgeMicro < finalSnapshot) {
          timestampsMicro.popFirst();
          values.popFirst();
        }
      } else {
        while (timestampsMicro.size() > maxSampleCount) {
          timestampsMicro.popFirst();
          values.popFirst();
        }
      }

      // Next, snapshot into arrays and return:
      int n = this.values.size();
      long[] timestamps = new long[n];
      T[] values = (T[]) Array.newInstance(clazz, n);
      for (int i = 0; i < n; i++) {
        timestamps[i] = this.timestampsMicro.get(i);
        values[i] = this.values.get(i);
      }
      return new TimeSeriesSnapshot(timestamps, values);
    }
  }

  /**
   * Given a live series, and duration, convert into a TimeSeries with history.
   */
  public static <V> TimeSeries<V> fromLiveSeries(LiveSeries<V> liveSeries, long maxAgeMS) {
    final TimeSeries<V> result = TimeSeries.fromMaxAgeMS(maxAgeMS);
    liveSeries.addListener(new LiveSeries.Listener<V>() {
      @Override public void valueAdded(long timestampMicro, V data) {
        result.pushValue(timestampMicro, data);
      }
    });
    return result;
  }

  /**
   * Given a live series, and a smaple count, convert into a TimeSeries with history.
   */
  public static <V> TimeSeries<V> fromLiveSeriesWithCount(LiveSeries<V> liveSeries, int maxLength) {
    final TimeSeries<V> result = TimeSeries.fromMaxLength(maxLength);
    liveSeries.addListener(new LiveSeries.Listener<V>() {
      @Override public void valueAdded(long timestampMicro, V data) {
        result.pushValue(timestampMicro, data);
      }
    });
    return result;
  }

  @Override
  public void addListener(Listener<T> listener) {
    this.listeners.add(listener);
  }
}
