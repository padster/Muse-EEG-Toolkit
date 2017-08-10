package eeg.useit.today.eegtoolkit.model;

/** A live timeseries of a given type, with callbacks for new data. */
public interface LiveSeries<T> {
  /** API for callback invoked when a live series has new data. */
  interface Listener<U> {
    void valueAdded(long timestampMicro, U data);
  }

  void addListener(Listener<T> listener);
}
