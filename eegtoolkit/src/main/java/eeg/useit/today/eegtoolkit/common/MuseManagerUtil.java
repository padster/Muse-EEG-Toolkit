package eeg.useit.today.eegtoolkit.common;

import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;

/** Collection of functionality added to MuseManagerAndroid. */
public class MuseManagerUtil {
  /** Called when a muse is discovered. */
  public interface MuseCallback {
    void museFound(Muse muse);
  }

  /** @return Muse device matching this address, or null. */
  public static void getByMacAddress(final String macAddress, final MuseCallback callback) {
    if (macAddress == null) {
      throw new IllegalArgumentException("Mac address can't be null.");
    }
    // See if already found:
    for (Muse muse : MuseManagerAndroid.getInstance().getMuses()) {
      if (macAddress.equals(muse.getMacAddress())) {
        callback.museFound(muse);
        return;
      }
    }
    // If not, schedule finding:
    MuseManagerAndroid.getInstance().setMuseListener(new MuseListener() {
      @Override
      public void museListChanged() {
        for (Muse muse : MuseManagerAndroid.getInstance().getMuses()) {
          if (macAddress.equals(muse.getMacAddress())) {
            callback.museFound(muse);
            MuseManagerAndroid.getInstance().stopListening();
            return;
          }
        }
      }
    });
    MuseManagerAndroid.getInstance().startListening();
  }
}
