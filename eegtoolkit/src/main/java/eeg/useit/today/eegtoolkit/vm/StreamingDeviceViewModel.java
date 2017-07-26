package eeg.useit.today.eegtoolkit.vm;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.util.Log;

import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;

import java.util.ArrayList;
import java.util.List;

import eeg.useit.today.eegtoolkit.Constants;
import eeg.useit.today.eegtoolkit.common.BaseDataPacketListener;

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
    if (!this.connectionCallbacks.isEmpty()) {
      this.connectToMuse();
    }
  }

  @Bindable
  public ConnectionState getConnectionState() {
    return this.connectionState;
  }

  /** @return Build a new live VM for each sensor's isGood status. */
  public SensorGoodViewModel createSensorConnection() {
    return new SensorGoodViewModel(this);
  }

  /** @return Build a new live VM for a single time series from a raw data channel. */
  public RawChannelViewModel createRawTimeSeries(final Eeg channel, int durationSec) {
    return new RawChannelViewModel(this, channel, durationSec * 1000L);
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

  /** Call to force connection to the Muse device. */
  private void connectToMuse() {
    assert this.connectionState == ConnectionState.DISCONNECTED;
    Log.i(Constants.TAG, "Need connection, running async");
    this.connectionState = connectionState.CONNECTING;
    this.muse.registerConnectionListener(new MuseConnectionListener() {
      @Override public void receiveMuseConnectionPacket(MuseConnectionPacket packet, Muse muse) {
        StreamingDeviceViewModel.this.handleMuseConnection(packet);
      }
    });
    this.muse.runAsynchronously();
  }

  /** Handle connection change to the muse device. */
  private void handleMuseConnection(MuseConnectionPacket packet) {
    this.connectionState = packet.getCurrentConnectionState();
    if (connectionState == ConnectionState.CONNECTED) {
      Log.i(Constants.TAG, "Connected!");
      // Connected, update everything that was waiting for this...
      for(Runnable callback : this.connectionCallbacks) {
        callback.run();
      }
      this.connectionCallbacks.clear();
    }
    notifyChange();
  }
}
