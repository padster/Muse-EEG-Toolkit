package eeg.useit.today.eegtoolkit.sampleapp;

import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;

import eeg.useit.today.eegtoolkit.sampleapp.databinding.ActivityMoreDeviceDetailsBinding;
import eeg.useit.today.eegtoolkit.vm.StreamingDeviceViewModel;

/**
 * Activity containing more example views provided by the toolkit.
 */
public class MoreDeviceDetailsActivity extends AppCompatActivity {
  public static int DURATION_SEC = 5;

  /** The live device VM backing this view. */
  private final StreamingDeviceViewModel deviceVM = new StreamingDeviceViewModel();

  /** Holder for the animated 2d plot. *
  private DotPlotView dotPlotView; */

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize Muse first up.
    MuseManagerAndroid.getInstance().setContext(this);

    // Bind viewmodel to the view.
    ActivityMoreDeviceDetailsBinding binding =
        DataBindingUtil.setContentView(this, R.layout.activity_more_device_details);
    binding.setDeviceVM(deviceVM);

    // Bind action bar, seems like this can't be done in the layout :(
    deviceVM.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override
      public void onPropertyChanged(Observable sender, int propertyId) {
        MoreDeviceDetailsActivity.this.getSupportActionBar().setTitle(
            String.format("%s: %s", deviceVM.getName(), deviceVM.getConnectionState())
        );
      }
    });

    // And attach the desired muse to the VM once connected.
    final String macAddress = getIntent().getExtras().getString("mac");
    if (macAddress != null) {
      MuseManagerAndroid.getInstance().startListening();
      MuseManagerAndroid.getInstance().setMuseListener(new MuseListener() {
        @Override
        public void museListChanged() {
          for (Muse muse : MuseManagerAndroid.getInstance().getMuses()) {
            if (macAddress.equals(muse.getMacAddress())) {
              MoreDeviceDetailsActivity.this.deviceVM.setMuse(muse);
              MuseManagerAndroid.getInstance().stopListening();
              break;
            }
          }
        }
      });
    }

    // Make sure to refresh the graphs once the timeseries changes.
    /*
    rawSeries3.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override public void onPropertyChanged(Observable sender, int propertyId) {
        updateGraphs();
      }
    });
    */
  }

  // Updates the graphs by invalidating them, causing redraw with the new data.
  private void updateGraphs() {
//    dotPlotView.invalidate();
  }
}
