package eeg.useit.today.eegtoolkit.common;

import com.choosemuse.libmuse.MuseDataPacketType;

/**
 * Collection of utilities concerning the standard EEG frequency bands.
 * See e.g. https://en.wikipedia.org/wiki/Electroencephalography#Wave_patterns
 */
public class FrequencyBands {
  /** Which band we're looking at. */
  public enum Band {
    // Frequency cutoff values taken from Muse documentation:
    // http://developer.choosemuse.com/research-tools/available-data
    DELTA(  1,  4),
    THETA(  4,  8),
    ALPHA(7.5, 13),
     BETA( 13, 30),
    GAMMA( 30, 44);

    public double minHz;
    public double maxHz;

    Band(double minHz, double maxHz) {
      this.minHz = minHz;
      this.maxHz = maxHz;
    }
  }

  // What type of aggregation is done to the frequency value.
  public enum ValueType {
    // Log of the sum of the Power Spectral Density of the data over the frequency range.
    ABSOLUTE,
    // Relative = divide the absolute linear-scale power in one band
    //            over the sum of the absolute linear-scale powers in all bands. [0, 1]
    RELATIVE,
    // Compares current value to range of values seen in short history. [0, 1]
    SCORE,
  }

  /** Given a Band and ValueType, return the muse type that corresponds. */
  public static MuseDataPacketType toMuseType(Band band, ValueType type) {
    switch(band) {
      case ALPHA:
        switch(type) {
          case RELATIVE: return MuseDataPacketType.ALPHA_RELATIVE;
          case ABSOLUTE: return MuseDataPacketType.ALPHA_ABSOLUTE;
          case SCORE: return MuseDataPacketType.ALPHA_SCORE;
        }
        break;
      case BETA:
        switch(type) {
          case RELATIVE: return MuseDataPacketType.BETA_RELATIVE;
          case ABSOLUTE: return MuseDataPacketType.BETA_ABSOLUTE;
          case SCORE: return MuseDataPacketType.BETA_SCORE;
        }
        break;
      case GAMMA:
        switch(type) {
          case RELATIVE: return MuseDataPacketType.GAMMA_RELATIVE;
          case ABSOLUTE: return MuseDataPacketType.GAMMA_ABSOLUTE;
          case SCORE: return MuseDataPacketType.GAMMA_SCORE;
        }
        break;
      case DELTA:
        switch(type) {
          case RELATIVE: return MuseDataPacketType.DELTA_RELATIVE;
          case ABSOLUTE: return MuseDataPacketType.DELTA_ABSOLUTE;
          case SCORE: return MuseDataPacketType.DELTA_SCORE;
        }
        break;
      case THETA:
        switch(type) {
          case RELATIVE: return MuseDataPacketType.THETA_RELATIVE;
          case ABSOLUTE: return MuseDataPacketType.THETA_ABSOLUTE;
          case SCORE: return MuseDataPacketType.THETA_SCORE;
        }
    }
    throw new IllegalArgumentException("Bad band / value type combination.");
  }
}
