package eeg.useit.today.eegtoolkit.vm;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseManagerAndroid;

import java.util.ArrayList;
import java.util.List;

import eeg.useit.today.eegtoolkit.BR;
import eeg.useit.today.eegtoolkit.Constants;

/**
 * ViewModel for the list of muse devices discovered.
 *
 * Includes scanning for the devices and updating when new ones are found.
 */
public class MuseListViewModel extends BaseObservable {
  /**
   * Listener for semantic events fired by the viewmodel.
   */
  public interface MuseListListener {
    void onScanForDevicesFinished();
    void onDeviceSelected(Muse muse);
  }

  /** Whether we're currently scanning for devices. */
  private boolean scanning = false;

  /** List of Muse devices found. */
  private List<Muse> devices = new ArrayList<>();

  /** Listener to be sent events. */
  private MuseListListener museListListener = null;

  /** @return Whether the list is currently scanning for devices. */
  @Bindable
  public boolean isScanning() {
    return this.scanning;
  }

  /** @return The list of devices that have been found. */
  @Bindable
  public List<Muse> getDevices() {
    return this.devices;
  }

  /**
   * Scan for devices for a given amount of time, and store all devices found in that period.
   *
   * @param scanLengthSec How long to scan for, in seconds.
   */
  public void scan(int scanLengthSec) {
    // Start listening for devices...
    Log.i(Constants.TAG, "Starting scan...");
    this.scanning = true;
    MuseManagerAndroid.getInstance().startListening();

    // ...and after a while...
    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
      @Override public void run() {
        // ...stop listening.
        Log.i(Constants.TAG, "Stopping scan...");
        MuseListViewModel.this.scanning = false;
        MuseManagerAndroid.getInstance().stopListening();

        // Report the devices we have found:
        MuseListViewModel.this.devices = new ArrayList<>(MuseManagerAndroid.getInstance().getMuses());
        notifyChange();
        if (museListListener != null) {
          MuseListViewModel.this.museListListener.onScanForDevicesFinished();
        }
      }
    }, scanLengthSec * 1000);

    // Also need to let ppl know the scanning field has changed.
    notifyChange();
  }

  /** Attach a listener to the VM. */
  public void setListener(MuseListListener listener) {
    this.museListListener = listener;
  }

  /** Select a device, by forwarding the event to the listener. */
  public void onSelect(Muse selectedDevice) {
    Log.i(Constants.TAG, "Selected: " + selectedDevice.getMacAddress());
    if (this.museListListener != null) {
      museListListener.onDeviceSelected(selectedDevice);
    }
  }
}
