package eeg.useit.today.eegtoolkit.vm;

import android.databinding.BaseObservable;
import android.util.Log;

import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;

import java.util.ArrayList;
import java.util.List;

import eeg.useit.today.eegtoolkit.Constants;
import eeg.useit.today.eegtoolkit.common.BaseDataPacketListener;
import eeg.useit.today.eegtoolkit.model.LiveSeries;

/**
 * Represents the most recent connection strength status indicator values for the four sensors,
 * normalized to 0 = bad, 1 = perfect.
 */
public class ConnectionStrengthViewModel extends BaseObservable implements LiveSeries<Double[]> {
  private List<Listener<Double[]>> listeners = new ArrayList<>();

  /** Most recent snapshot of values. */
  private Double[] currentValue = new Double[]{0., 0., 0., 0.};

  /** Creates the VM by updating from the device's HSI values. */
  public ConnectionStrengthViewModel(StreamingDeviceViewModel deviceVM) {
    deviceVM.registerDataListenerWhenConnected(new BaseDataPacketListener() {
      @Override public void receiveMuseDataPacket(MuseDataPacket museDataPacket, Muse muse) {
        updateConnectionState(
            museDataPacket.timestamp(),
            museDataPacket.getEegChannelValue(Eeg.EEG1),
            museDataPacket.getEegChannelValue(Eeg.EEG2),
            museDataPacket.getEegChannelValue(Eeg.EEG3),
            museDataPacket.getEegChannelValue(Eeg.EEG4)
        );
      }
    }, MuseDataPacketType.HSI_PRECISION);
  }

  /** @return Connection value for a specific channel. */
  public double getChannelStatus(int i) {
    return currentValue[i];
  }

  @Override
  public void addListener(Listener<Double[]> listener) {
    this.listeners.add(listener);
  }

  /** Update state given new snapshot of HSI values from muse. */
  private void updateConnectionState(long ts, double... values) {
    assert values.length == 4;
    for (int i = 0; i < 4; i++) {
      currentValue[i] = normalizeValue(values[i]);
    }
    for (Listener<Double[]> listener : listeners) {
      listener.valueAdded(ts, currentValue);
    }
    notifyChange();
  }

  /** Convert the Muse value (1 = good, 2 = ok, 3+ = bad) to ours (0 = bad, 0.5 = ok, 1 = good) */
  private double normalizeValue(double input) {
    input = Math.max(1.0, Math.min(3.0, input)); // Snap to [1, 3]
    input = (input - 1.0) / 2.0; // [0, 1]
    return 1.0 - input; // [1, 0]
  }
}
