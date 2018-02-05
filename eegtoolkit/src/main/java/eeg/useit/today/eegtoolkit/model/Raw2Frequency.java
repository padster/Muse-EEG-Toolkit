package eeg.useit.today.eegtoolkit.model;

import android.databinding.DataBindingUtil;

import java.util.ArrayList;
import java.util.List;

import eeg.useit.today.eegtoolkit.model.fft.DoFFT;

/**
 * Given a raw signal, convert it to frequency domain by performing a
 * windowed STFT every N samples.
 */
public class Raw2Frequency implements LiveSeries<Double[]>, TimeSeries.Listener<Double> {
  public static final int WINDOW_SIZE = 256;
  public static final int DOWNSAMPLE_RATE = 22;

  private final TimeSeries<Double> timeSeries;

  /** Listeners for new values that come in. */
  private List<Listener<Double[]>> listeners = new ArrayList<>();

  private int samplesLeftAtStart;
  private int samplesLeftToFFT;

  private DoFFT fft = new DoFFT();

  public Raw2Frequency(TimeSeries<Double> timeSeries) {
    this.timeSeries = timeSeries;
    samplesLeftAtStart = WINDOW_SIZE;
    samplesLeftToFFT = 0;
    timeSeries.addListener(this);
  }

  @Override
  public void valueAdded(long timestampMicro, Double data) {
    if (samplesLeftAtStart > 0) {
      samplesLeftAtStart--;
    }
    if (samplesLeftAtStart > 0) {
      return;
    }
    if (samplesLeftToFFT > 0) {
      samplesLeftToFFT--;
    } else {
      runSTFT(this.timeSeries.getRecentSnapshot(Double.class));
      samplesLeftToFFT = DOWNSAMPLE_RATE;
    }
  }

  private void runSTFT(TimeSeriesSnapshot<Double> snapshot) {
    if (listeners.isEmpty()) {
      return;
    }

    assert snapshot.values.length > WINDOW_SIZE;
    Double[] prevWindow = new Double[WINDOW_SIZE];
    for (int i = 0; i < WINDOW_SIZE; i++) {
      prevWindow[i] = snapshot.values[snapshot.values.length - WINDOW_SIZE + i];
    }

    Double[] values = fft.calc(prevWindow);
    for (Listener<Double[]> listener : listeners) {
      listener.valueAdded(
          snapshot.timestamps[snapshot.timestamps.length - 1],
          values);
    }
  }

  @Override
  public void addListener(Listener<Double[]> listener) {
    this.listeners.add(listener);
  }

//  private static double[] precalcWindow(int size) {
//    double a = 0.53836;
//    double[] w = new double[size];
//    for (int i = 0; i < size; i++) {
//      double t = (2.0 * Math.PI * i) / (size - 1);
//      w[i] = a - (1 - a) * Math.cos(t);
//    }
//    return w;
//  }
}
