package eeg.useit.today.eegtoolkit;

/**
 * Collection of constants used by the library.
 */
public class Constants {
  public static final String TAG = "EEGToolkit";
  public static final String RECORDING_PREFIX = "data";

  // HACK: Should configure these elsewhere, perhaps attributes to renderers.
  public static final double VOLTAGE_MAX = 1000.0;
  public static final double VOLTAGE_MIN = 700.0;
}
