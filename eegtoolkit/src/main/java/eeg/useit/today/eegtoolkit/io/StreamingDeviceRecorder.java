package eeg.useit.today.eegtoolkit.io;

import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.MuseDataPacketType;

import java.util.Set;

import eeg.useit.today.eegtoolkit.vm.StreamingDeviceViewModel;

/**
 * Wraps a streaming device in an interface to allow recording its output to file.
 */
public class StreamingDeviceRecorder {
  private final StreamingDeviceViewModel device;
  private final Set<MuseDataPacketType> types;

  private boolean running = false;

  public StreamingDeviceRecorder(
      StreamingDeviceViewModel device, Set<MuseDataPacketType> types
  ) {
    this.device = device;
    this.types = types;
  }

  /** @return Whether the recording is currently taking place. */
  public boolean isRunning() {
    return this.running;
  }

  /** Start the recording, can only be called once. */
  public void start() {
    assert !running;
    assert this.device.getConnectionState() == ConnectionState.CONNECTED;
    running = true;
  }

  /**
   * Stops the recording, can only be called once.
   * @return File name where the recording was saved.
   */
  public String stopAndSave() {
    // TODO
    running = false;
    return "HACK.txt";
  }
}
