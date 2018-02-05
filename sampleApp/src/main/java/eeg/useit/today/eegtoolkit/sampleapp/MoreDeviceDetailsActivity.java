package eeg.useit.today.eegtoolkit.sampleapp;

import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseManagerAndroid;

import eeg.useit.today.eegtoolkit.Constants;
import eeg.useit.today.eegtoolkit.common.FrequencyBands;
import eeg.useit.today.eegtoolkit.common.MuseManagerUtil;
import eeg.useit.today.eegtoolkit.model.EpochCollector;
import eeg.useit.today.eegtoolkit.model.MergedSeries;
import eeg.useit.today.eegtoolkit.model.TimeSeries;
import eeg.useit.today.eegtoolkit.sampleapp.databinding.ActivityMoreDeviceDetailsBinding;
import eeg.useit.today.eegtoolkit.view.Plot2DView;
import eeg.useit.today.eegtoolkit.vm.FrequencyBandViewModel;
import eeg.useit.today.eegtoolkit.vm.StreamingDeviceViewModel;

/**
 * Activity containing more example views provided by the toolkit.
 */
public class MoreDeviceDetailsActivity extends AppCompatActivity {
  public static int DURATION_SEC = 4;

  /** The live device VM backing this view. */
  private final StreamingDeviceViewModel deviceVM = new StreamingDeviceViewModel();

  /** Epoch Model */
  private final EpochCollector epochCollector = new EpochCollector();

  /** Holder for the animated 2d plot. */
  private Plot2DView plot2DView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize Muse first up.
    MuseManagerAndroid.getInstance().setContext(this);

    // Bind viewmodel to the view.
    ActivityMoreDeviceDetailsBinding binding =
        DataBindingUtil.setContentView(this, R.layout.activity_more_device_details);
    binding.setDeviceVM(deviceVM);
    binding.setEpochs(epochCollector);
    binding.setPsdVM(deviceVM.createPSDLiveValue());

    // Bind action bar, seems like this can't be done in the layout :(
    deviceVM.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override
      public void onPropertyChanged(Observable sender, int propertyId) {
        MoreDeviceDetailsActivity.this.getSupportActionBar().setTitle(
            String.format("%s: %s", deviceVM.getName(), deviceVM.getConnectionState())
        );
      }
    });

    // Attach the desired muse to the VM once connected.
    final String macAddress = getIntent().getExtras().getString("mac");
    MuseManagerUtil.getByMacAddress(macAddress, new MuseManagerUtil.MuseCallback() {
      @Override
      public void museFound(Muse muse) {
        Log.i(Constants.TAG, "SETTING MUSE");
        MoreDeviceDetailsActivity.this.deviceVM.setMuse(muse);
      }
    });

    // Add frequency listeners to the 2D plot:
    FrequencyBandViewModel relativeTheta = deviceVM.createFrequencyLiveValue(
        FrequencyBands.Band.THETA, FrequencyBands.ValueType.SCORE);
    FrequencyBandViewModel relativeBeta = deviceVM.createFrequencyLiveValue(
        FrequencyBands.Band.BETA, FrequencyBands.ValueType.SCORE);
    TimeSeries<Double[]> plotTimeSeries = TimeSeries.fromLiveSeries(
        new MergedSeries(relativeTheta, relativeBeta), DURATION_SEC * 1000
    );
    plot2DView = (Plot2DView) findViewById(R.id.tbr2DPlot);
    plot2DView.setTimeSeries(plotTimeSeries);

    // Add raw listeners for the epoch collector:
    epochCollector.addSource("3", deviceVM.createRawTimeSeries(Eeg.EEG3, 2));
  }
}
