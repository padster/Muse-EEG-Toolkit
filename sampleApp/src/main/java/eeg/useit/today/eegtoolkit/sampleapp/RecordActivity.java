package eeg.useit.today.eegtoolkit.sampleapp;

import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;

import eeg.useit.today.eegtoolkit.Constants;
import eeg.useit.today.eegtoolkit.sampleapp.databinding.ActivityRecordBinding;
import eeg.useit.today.eegtoolkit.sampleapp.vm.RecordVM;
import eeg.useit.today.eegtoolkit.vm.StreamingDeviceViewModel;

/**
 * Activity that shows options for recording, and allows users to start/stop
 * a recording and save results to file.
 */
public class RecordActivity extends AppCompatActivity {
  /** The live device VM backing this view. */
  private final StreamingDeviceViewModel deviceVM = new StreamingDeviceViewModel();
  private final RecordVM viewModel = new RecordVM(deviceVM);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize Muse first up.
    MuseManagerAndroid.getInstance().setContext(this);

    // Bind viewmodel to the view.
    ActivityRecordBinding binding =
        DataBindingUtil.setContentView(this, R.layout.activity_record);
    binding.setRecordVM(viewModel);
    binding.setDeviceVM(deviceVM);

    Log.i(Constants.TAG, "ADDING CALLBACK TO UPDATE TITLE");

    // Bind action bar, seems like this can't be done in the layout :(
    deviceVM.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override
      public void onPropertyChanged(Observable sender, int propertyId) {
        RecordActivity.this.getSupportActionBar().setTitle(
            String.format("%s: %s", deviceVM.getName(), deviceVM.getConnectionState())
        );
      }
    });

    // And attach the desired muse to the VM once connected.
    final String macAddress = getIntent().getExtras().getString("mac");
    Log.i(Constants.TAG, "CONNECTING TO MUSE: " + macAddress);
    if (macAddress != null) {
      boolean connected = false;
      for (Muse muse : MuseManagerAndroid.getInstance().getMuses()) {
        if (macAddress.equals(muse.getMacAddress())) {
          Log.i(Constants.TAG, "ALREADY CONNECTED!!");
          RecordActivity.this.deviceVM.setMuse(muse);
          RecordActivity.this.getSupportActionBar().setTitle(
              String.format("%s: %s", deviceVM.getName(), deviceVM.getConnectionState())
          );
          connected = true;
          break;
        }
      }
      if (!connected) {
        MuseManagerAndroid.getInstance().startListening();
        MuseManagerAndroid.getInstance().setMuseListener(new MuseListener() {
          @Override
          public void museListChanged() {
            Log.i(Constants.TAG, "Connected to " + MuseManagerAndroid.getInstance().getMuses().size());
            for (Muse muse : MuseManagerAndroid.getInstance().getMuses()) {
              if (macAddress.equals(muse.getMacAddress())) {
                Log.i(Constants.TAG, "CONNECTED!");
                RecordActivity.this.deviceVM.setMuse(muse);
                MuseManagerAndroid.getInstance().stopListening();
                break;
              }
            }
          }
        });
      }
    }
  }
}
