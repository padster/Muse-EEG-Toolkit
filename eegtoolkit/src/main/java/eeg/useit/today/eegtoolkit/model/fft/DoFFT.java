package eeg.useit.today.eegtoolkit.model.fft;

/**
 * Created by pat on 9/12/17.
 */

public class DoFFT {
  private static int WINDOW_SZ = 256;
  private double[] HAMMING_WINDOW = new double[WINDOW_SZ];

  public DoFFT() {
    double alpha = 0.53836;
    double beta = 1.0 - alpha;
    for (int i = 0; i < WINDOW_SZ; i++) {
      HAMMING_WINDOW[i] = alpha - beta * Math.cos(2 * Math.PI * i / (1.0 * WINDOW_SZ));
    }
  }

  public Double[] calc(Double[] rawSignal) {
    assert rawSignal.length == WINDOW_SZ;

    double[] windowed = new double[WINDOW_SZ];
    for (int i = 0; i < WINDOW_SZ; i++) {
      windowed[i] = rawSignal[i] * HAMMING_WINDOW[i];
    }

    FFT fft = new FFT(WINDOW_SZ);
    Complex1D result = fft.ft(windowed);
    Double[] powers = new Double[WINDOW_SZ/2 + 1];
    for (int i = 0; i < WINDOW_SZ/2 + 1; i++) {
      double x = result.real[i], y = result.imag[i];
      powers[i] = 10.0 * Math.log(Math.sqrt(x*x+y*y));
    }
    return powers;
  }
}
