package eeg.useit.today.eegtoolkit.model;

import android.databinding.BaseObservable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory storage for a collection of epoch snapshots taken from some time series.
 * Given some named time series, this allows taking a snapshot of a small history of time (epoch)
 * for each, and stores the full history of snapshots in memory for later evaluation.
 */
public class EpochCollector extends BaseObservable {
  /** History of epochs taken. */
  private final List<Map<String, TimeSeriesSnapshot<Double>>> epochs = new ArrayList<>();

  /** Live sources of the time series data. */
  private final Map<String, TimeSeries<Double>> timeSeries = new HashMap<>();

  /** Track another named source. */
  public void addSource(String name, TimeSeries<Double> series) {
    timeSeries.put(name, series);
  }

  /** For all the tracked sources, take a snapshot and save that epoch internally. */
  public void collectEpoch() {
    Map<String, TimeSeriesSnapshot<Double>> epoch = new HashMap<>();
    for (Map.Entry<String, TimeSeries<Double>> entry : timeSeries.entrySet()) {
      epoch.put(entry.getKey(), entry.getValue().getRecentSnapshot(Double.class));
    }
    epochs.add(epoch);
    notifyChange();
  }

  /** @return All the previously stored epochs. */
  public List<Map<String, TimeSeriesSnapshot<Double>>> getEpochs() {
    return epochs;
  }

  /** Resets epoch data history. */
  public void clear() {
    epochs.clear();
  }
}
