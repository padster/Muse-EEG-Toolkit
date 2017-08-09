package eeg.useit.today.eegtoolkit.io;

import com.choosemuse.libmuse.ConnectionState;

import eeg.useit.today.eegtoolkit.vm.StreamingDeviceViewModel;

/**
 * Wraps a streaming device in an interface to allow recording its output to file.
 */
public class StreamingDeviceRecorder {
  private final StreamingDeviceViewModel device;

  private boolean isRunning = false;

  public StreamingDeviceRecorder(StreamingDeviceViewModel device) {
    this.device = device;
  }

  /** Start the recording, can only be called once. */
  public void start() {
    assert !isRunning;
    assert this.device.getConnectionState() == ConnectionState.CONNECTED;
    isRunning = true;
  }

  /** Stops the recording, can only be called once. */
  public void stop() {
    // TODO
    isRunning = false;
  }
}
