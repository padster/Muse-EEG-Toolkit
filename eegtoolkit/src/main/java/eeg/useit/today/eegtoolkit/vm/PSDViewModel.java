package eeg.useit.today.eegtoolkit.vm;

import com.choosemuse.libmuse.Eeg;

import eeg.useit.today.eegtoolkit.model.Raw2Frequency;

/** Live series of the powers in each frequency band. */
public class PSDViewModel extends Raw2Frequency {

  public PSDViewModel(StreamingDeviceViewModel device) {
    super(device.createRawTimeSeries(Eeg.EEG1, 2));
  }
}
