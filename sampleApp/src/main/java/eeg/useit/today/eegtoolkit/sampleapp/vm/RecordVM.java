package eeg.useit.today.eegtoolkit.sampleapp.vm;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.util.Log;

import com.choosemuse.libmuse.ConnectionState;

import eeg.useit.today.eegtoolkit.Constants;
import eeg.useit.today.eegtoolkit.vm.StreamingDeviceViewModel;

/**
 * ViewModel for options set before a recording, and performs the recording.
 */
public class RecordVM extends BaseObservable {
  private final StreamingDeviceViewModel device;

  private boolean useRaw = false;
  private boolean useAlpha = false;

  public RecordVM(StreamingDeviceViewModel device) {
    this.device = device;
    this.device.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
      @Override
      public void onPropertyChanged(Observable sender, int propertyId) {
        // TODO: Only propogate subset of properties?
        RecordVM.this.notifyChange();
      }
    });
  }

  public boolean canRecord() {
    Log.i(Constants.TAG, useRaw + " / " + useAlpha + " / " + device.getConnectionState());
    boolean somethingSelected = (useRaw || useAlpha);
    return somethingSelected && device.getConnectionState() == ConnectionState.CONNECTED;
  }

  // GET checkbox values

  public boolean hasUseRaw() {
    return this.useRaw;
  }

  public boolean hasUseAlpha() {
    return this.useAlpha;
  }

  // SET checkbox values

  public void toggleRaw() {
    this.useRaw = !this.useRaw;
    notifyChange();
  }

  public void toggleAlpha() {
    this.useAlpha = !this.useAlpha;
    notifyChange();
  }
}
