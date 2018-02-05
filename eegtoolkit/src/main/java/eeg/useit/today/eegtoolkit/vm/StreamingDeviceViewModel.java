package eeg.useit.today.eegtoolkit.vm;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.util.Log;

import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;

import java.util.ArrayList;
import java.util.List;

import eeg.useit.today.eegtoolkit.Constants;
import eeg.useit.today.eegtoolkit.common.FrequencyBands.Band;
import eeg.useit.today.eegtoolkit.common.FrequencyBands.ValueType;

/**
 * ViewModel for a Muse device that is streaming data.
 * Handles all connection logic, and sending live data to listeners
 *   efficiently once connected.
 */
public class StreamingDeviceViewModel extends BaseObservable {
  /** Muse that this device is backed by. */
  private Muse muse = null;

  /** All things to be run on connection. */
  private final List<Runnable> connectionCallbacks = new ArrayList<>();

  /** Current status of connection. */
  private ConnectionState connectionState = ConnectionState.DISCONNECTED;

  /** Set the muse that is backing this device. If needed, try to connect straight away. */
  public void setMuse(Muse muse) {
    assert this.muse == null;
    assert this.connectionState == ConnectionState.DISCONNECTED;
    this.muse = muse;
    this.connectionState = this.muse.getConnectionState();
    if (!this.connectionCallbacks.isEmpty()) {
      this.connectToMuse();
    }
    notifyChange();
  }

  @Bindable
  public ConnectionState getConnectionState() {
    return this.connectionState;
  }

  /** @return Bluetooth device name, or default if not yet connected. */
  public String getName() {
    return this.muse == null ? "No device" : this.muse.getName();
  }

  /** @return Bluetooth mac address, or null if not yet connected. */
  public String getMacAddress() {
    return this.muse == null ? null : this.muse.getMacAddress();
  }

  /** @return A new live VM for a single time series from a raw data channel. */
  public RawChannelViewModel createRawTimeSeries(final Eeg channel, int durationSec) {
    int samples = (int)(durationSec * 220.0); // Muse sample rate.
    return new RawChannelViewModel(this, channel, -1, samples);
  }

  /** @return A new live VM for each sensor's frequency value for a given band/type combo. */
  public FrequencyBandViewModel createFrequencyLiveValue(Band band, ValueType type) {
    return new FrequencyBandViewModel(this, band, type);
  }

  /** @return A new live VM for the frequency band powers. */
  public PSDViewModel createPSDLiveValue() {
    return new PSDViewModel(this);
  }

  /**
   * Register a data listener to the device.
   *  - If already connected, registers it directly.
   *  - Otherwise, sets it up to register when conncted, and tries to connect if possible.
   */
  public void registerDataListenerWhenConnected(
      final MuseDataListener listener, final MuseDataPacketType type) {
    Log.i(Constants.TAG, "Connecting to " + type.name() + " in state " + connectionState.name());
    if (this.connectionState == ConnectionState.CONNECTED) {
      // Already connected, go for launch.
      this.muse.registerDataListener(listener, type);
    } else {
      Log.i(Constants.TAG, "Adding connection callback...");
      this.connectionCallbacks.add(new Runnable() {
        @Override
        public void run() {
          Log.i(Constants.TAG, "Connected! running callback...");
          StreamingDeviceViewModel.this.muse.registerDataListener(listener, type);
        }
      });
      if (this.muse != null && this.connectionState == ConnectionState.DISCONNECTED) {
        // We want a connection, we have a device, may as well connect.
        this.connectToMuse();
      }
    }
  }

  /** Remove all listeners that have been added. */
  public void removeAllListeners() {
    this.muse.unregisterAllListeners();
  }

  /** Call to force connection to the Muse device. */
  private void connectToMuse() {
    if (this.muse.getConnectionState() == ConnectionState.CONNECTED) {
      // Already connected:
      Log.i(Constants.TAG, "Have connection, triggering callbacks.");
      this.handleConnection();
    } else {
      assert this.connectionState == ConnectionState.DISCONNECTED;
      Log.i(Constants.TAG, "Need connection, running async");
      this.connectionState = connectionState.CONNECTING;
      this.muse.registerConnectionListener(new MuseConnectionListener() {
        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket packet, Muse muse) {
          connectionState = packet.getCurrentConnectionState();
          if (connectionState == ConnectionState.CONNECTED) {
            StreamingDeviceViewModel.this.handleConnection();
          }
        }
      });
      this.muse.runAsynchronously();
    }
  }

  /** Handle connection change to the muse device. */
  private void handleConnection() {
    Log.i(Constants.TAG, "Connected!");
    this.connectionState = ConnectionState.CONNECTED;
    // Connected, update everything that was waiting for this...
    for(Runnable callback : this.connectionCallbacks) {
      callback.run();
    }
    this.connectionCallbacks.clear();
    notifyChange();
  }
}
