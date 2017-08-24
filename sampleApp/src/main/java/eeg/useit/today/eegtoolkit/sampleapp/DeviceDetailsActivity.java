package eeg.useit.today.eegtoolkit.sampleapp;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;

import eeg.useit.today.eegtoolkit.common.FrequencyBands.Band;
import eeg.useit.today.eegtoolkit.common.FrequencyBands.ValueType;
import eeg.useit.today.eegtoolkit.sampleapp.databinding.ActivityDeviceDetailsBinding;
import eeg.useit.today.eegtoolkit.view.ConnectionStrengthView;
import eeg.useit.today.eegtoolkit.view.graph.GraphGLView;
import eeg.useit.today.eegtoolkit.view.graph.GraphSurfaceView;
import eeg.useit.today.eegtoolkit.vm.ConnectionStrengthViewModel;
import eeg.useit.today.eegtoolkit.vm.SensorGoodViewModel;
import eeg.useit.today.eegtoolkit.vm.StreamingDeviceViewModel;
import eeg.useit.today.eegtoolkit.model.TimeSeries;

/**
 * Example activity that displays live details for a connected device.
 * Includes isGood connection status, and scrolling line graphs using surface and GL views.
 */
public class DeviceDetailsActivity extends AppCompatActivity {
  public static int DURATION_SEC = 5;

  /** The live device VM backing this view. */
  private final StreamingDeviceViewModel deviceVM = new StreamingDeviceViewModel();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize Muse first up.
    MuseManagerAndroid.getInstance().setContext(this);

    // Bind viewmodel to the view.
    ActivityDeviceDetailsBinding binding =
        DataBindingUtil.setContentView(this, R.layout.activity_device_details);
    binding.setDeviceVM(deviceVM);
    binding.setIsGoodVM(new SensorGoodViewModel(deviceVM));
    binding.setConnectionVM(new ConnectionStrengthViewModel(deviceVM));
    binding.setRawVM(  deviceVM.createRawTimeSeries(Eeg.EEG3, DURATION_SEC));
    binding.setThetaVM(deviceVM.createFrequencyLiveValue(Band.THETA, ValueType.SCORE));
    binding.setDeltaVM(deviceVM.createFrequencyLiveValue(Band.DELTA, ValueType.SCORE));
    binding.setAlphaVM(deviceVM.createFrequencyLiveValue(Band.ALPHA, ValueType.SCORE));
    binding.setBetaVM( deviceVM.createFrequencyLiveValue(Band.BETA,  ValueType.SCORE));

    // Bind action bar, seems like this can't be done in the layout :(
    deviceVM.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override
      public void onPropertyChanged(Observable sender, int propertyId) {
        DeviceDetailsActivity.this.getSupportActionBar().setTitle(
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
              DeviceDetailsActivity.this.deviceVM.setMuse(muse);
              MuseManagerAndroid.getInstance().stopListening();
              break;
            }
          }
        }
      });
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.moreViews) {
      Intent intent = new Intent(this, MoreDeviceDetailsActivity.class);
      intent.putExtra("mac", this.deviceVM.getMacAddress());
      this.startActivity(intent);
      return true;
    } else if (item.getItemId() == R.id.recordOption) {
      Intent intent = new Intent(this, RecordActivity.class);
      intent.putExtra("mac", this.deviceVM.getMacAddress());
      this.startActivity(intent);
      return true;
    }
    return false;
  }
}
