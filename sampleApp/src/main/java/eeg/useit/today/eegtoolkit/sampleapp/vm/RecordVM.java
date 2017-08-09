package eeg.useit.today.eegtoolkit.sampleapp.vm;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Observable;

import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.MuseDataPacketType;

import java.util.HashSet;
import java.util.Set;

import eeg.useit.today.eegtoolkit.Constants;
import eeg.useit.today.eegtoolkit.io.StreamingDeviceRecorder;
import eeg.useit.today.eegtoolkit.vm.StreamingDeviceViewModel;

/**
 * ViewModel for options set before a recording, and performs the recording.
 */
public class RecordVM extends BaseObservable {
  private final Context context;
  private final StreamingDeviceViewModel device;

  private boolean useRaw = false;
  private boolean useAlpha = false;

  // Performs recording, null before recording has started.
  private StreamingDeviceRecorder recorder = null;

  // Location of the recording. set after stopping.
  private String fileName;

  public RecordVM(Context context, StreamingDeviceViewModel device) {
    this.context = context;
    this.device = device;
    this.device.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
      @Override
      public void onPropertyChanged(Observable sender, int propertyId) {
        // TODO: Only propogate subset of properties?
        RecordVM.this.notifyChange();
      }
    });
  }

  /** @return Caption to be displayed on record button. */
  public String getButtonText() {
    if (recorder == null) {
      return "Record";
    } else if (recorder.isRunning()) {
      return "Stop";
    } else {
      return "Saved to " + this.fileName;
    }
  }


  /** @return if recording is possible: requires a connected device, and something to record. */
  public boolean canRecord() {
    boolean somethingSelected = (useRaw || useAlpha);
    boolean recordingStopped = recorder != null && !recorder.isRunning();
    return somethingSelected && !recordingStopped
        && device.getConnectionState() == ConnectionState.CONNECTED;
  }

  /** Handles the record button being pressed. */
  public void handleRecord() {
    if (recorder == null) {
      this.startRecording();
    } else if (recorder.isRunning()) {
      this.stopRecording();
    } else {
      // Nothing to see here, shouldn't be pressing it.
    }
  }

  /** Starts recording the packets from the device. */
  private void startRecording() {
    assert this.canRecord();
    assert this.recorder == null;
    Set<MuseDataPacketType> types = new HashSet<>();
    if (useRaw) {
      types.add(MuseDataPacketType.EEG);
    }
    if (useAlpha) {
      types.add(MuseDataPacketType.ALPHA_RELATIVE);
    }
    this.recorder = new StreamingDeviceRecorder(context, Constants.RECORDING_PREFIX, this.device, types);
    this.recorder.start();
    notifyChange();
  }

  /** Stops recording, saving result to file. */
  private void stopRecording() {
    assert this.recorder != null && this.recorder.isRunning();
    this.fileName = this.recorder.stopAndSave();
    notifyChange();
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
