package eeg.useit.today.eegtoolkit.vm;

import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;

import eeg.useit.today.eegtoolkit.common.BaseDataPacketListener;

/**
 * Given a device and a raw channel, present that channel's data as a TimeSeries.
 */
public class RawChannelViewModel extends TimeSeries {
  public RawChannelViewModel(StreamingDeviceViewModel device, final Eeg channel, long maxAgeMS) {
    super(maxAgeMS);

    // Add listener to streaming device, use new values to update timeseries.
    // TODO: Unregister when device stops, or on disposal.
    device.registerDataListenerWhenConnected(new BaseDataPacketListener() {
      @Override public void receiveMuseDataPacket(MuseDataPacket museDataPacket, Muse muse) {
        double rawValue = museDataPacket.getEegChannelValue(channel);
        RawChannelViewModel.this.pushValue(museDataPacket.timestamp(), rawValue);
      }
    }, MuseDataPacketType.EEG);
  }
}
