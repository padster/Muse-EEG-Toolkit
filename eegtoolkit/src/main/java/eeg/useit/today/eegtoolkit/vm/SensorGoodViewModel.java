package eeg.useit.today.eegtoolkit.vm;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;

import eeg.useit.today.eegtoolkit.BR;
import eeg.useit.today.eegtoolkit.common.BaseDataPacketListener;

/**
 * Represents the most recent values of the is_good state for the four sensors.
 * See also {@link ConnectionStrengthViewModel} if intermediate connection is required.
 */
public class SensorGoodViewModel extends BaseObservable {
  /** Most recent connection status. */
  private final boolean[] connectionState = new boolean[4];

  /** Creates the VM by updating from the device's isGood status. */
  public SensorGoodViewModel(StreamingDeviceViewModel deviceVM) {
    deviceVM.registerDataListenerWhenConnected(new BaseDataPacketListener() {
      @Override
      public void receiveMuseDataPacket(MuseDataPacket museDataPacket, Muse muse) {
        updateConnectionState(
            museDataPacket.getEegChannelValue(Eeg.EEG1) > 0,
            museDataPacket.getEegChannelValue(Eeg.EEG2) > 0,
            museDataPacket.getEegChannelValue(Eeg.EEG3) > 0,
            museDataPacket.getEegChannelValue(Eeg.EEG4) > 0
        );
      }
    }, MuseDataPacketType.IS_GOOD);
  }

  @Bindable
  public boolean[] getConnected() {
    return this.connectionState;
  }

  // Update the connection status with the latest values.
  private void updateConnectionState(boolean ...newState) {
    assert newState.length <= 4;
    for (int i = 0; i < newState.length; i++) {
      connectionState[i] = newState[i];
    }
    notifyPropertyChanged(BR.connected);
  }
}
