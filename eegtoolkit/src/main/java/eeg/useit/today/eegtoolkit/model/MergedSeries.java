package eeg.useit.today.eegtoolkit.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eeg.useit.today.eegtoolkit.Constants;

/**
 * Merges multiple separate double-valued timeseries into a single Double[] series.
 */
public class MergedSeries implements LiveSeries<Double[]> {
  private static final long NO_TIMESTAMP = -1L;

  private final List<Listener<Double[]>> listeners = new ArrayList<>();

  private final long[] lastTimestamp;
  private final Double[] lastValue;

  /** Create a merged series, listening to a list of 1D double series. */
  public MergedSeries(LiveSeries<Double> ...series) {
    int n = series.length;
    this.lastTimestamp = new long[n];
    this.lastValue = new Double[n];

    for (int i = 0; i < n; i++) {
      this.lastTimestamp[i] = NO_TIMESTAMP;
      this.lastValue[i] = 0.0;

      final int idx = i;
      series[i].addListener(new LiveSeries.Listener<Double>() {
        @Override public void valueAdded(long timestamp, Double value) {
          MergedSeries.this.handleNewValue(idx, timestamp, value);
        }
      });
    }
  }

  /**
   * Handle a value coming in, by storing it and firing the merged value if it's the last to arrive.
   */
  private void handleNewValue(int index, long timestamp, double value) {
    this.lastTimestamp[index] = timestamp;
    this.lastValue[index] = value;
    if (allHaveValues()) {
      Double[] valueCopy = Arrays.copyOf(this.lastValue, this.lastValue.length);
      for (Listener<Double[]> listener : this.listeners) {
        listener.valueAdded(timestamp, valueCopy);
      }
      // Clear all after fire, so we know when everything has arrived later.
      for (int i = 0; i < this.lastTimestamp.length; i++) {
        this.lastTimestamp[i] = NO_TIMESTAMP;
        this.lastValue[i] = 0.0;
      }
    }
  }

  /** @return Whether all wrapped series have recent values. */
  private boolean allHaveValues() {
    for (int i = 1; i < this.lastTimestamp.length; i++) {
      if (this.lastTimestamp[i] == NO_TIMESTAMP) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void addListener(Listener<Double[]> listener) {
    this.listeners.add(listener);
  }
}
