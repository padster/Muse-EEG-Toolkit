package eeg.useit.today.eegtoolkit.vm;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;

import eeg.useit.today.eegtoolkit.common.BaseDataPacketListener;
import eeg.useit.today.eegtoolkit.common.FrequencyBands;
import eeg.useit.today.eegtoolkit.common.FrequencyBands.Band;
import eeg.useit.today.eegtoolkit.common.FrequencyBands.ValueType;

/**
 * Represents the most recent values for a frequency band across the sensors.
 */
public class FrequencyBandViewModel extends BaseObservable {
  /** Most recent frequency values. */
  private final double[] frequencyState = new double[4];

  public FrequencyBandViewModel(StreamingDeviceViewModel device, Band band, ValueType type) {
    MuseDataPacketType museType = FrequencyBands.toMuseType(band, type);

    // Add listener to streaming device, use new values to update timeseries.
    // TODO: Unregister when device stops, or on disposal.
    device.registerDataListenerWhenConnected(new BaseDataPacketListener() {
      @Override public void receiveMuseDataPacket(MuseDataPacket museDataPacket, Muse muse) {
        FrequencyBandViewModel.this.updateValues(
            museDataPacket.getEegChannelValue(Eeg.EEG1),
            museDataPacket.getEegChannelValue(Eeg.EEG2),
            museDataPacket.getEegChannelValue(Eeg.EEG3),
            museDataPacket.getEegChannelValue(Eeg.EEG4)
        );
      }
    }, museType);
  }


  @Bindable
  public double[] getFrequencyValues() {
    return this.frequencyState;
  }

  @Bindable
  public double getAverage() {
    // Cross-sensor averaging, done by the VM.
    double sum = 0.0;
    for (int i = 0; i < frequencyState.length; i++) {
      sum += frequencyState[i];
    }
    return sum / frequencyState.length;
  }

  // Updates the new values given the most recent data packet.
  private void updateValues(double... newState) {
    assert newState.length <= 4;
    for (int i = 0; i < newState.length; i++) {
      frequencyState[i] = newState[i];
    }
    notifyChange();
  }
}
