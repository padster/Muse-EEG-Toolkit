package eeg.useit.today.eegtoolkit.sampleapp;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.choosemuse.libmuse.Muse;

import eeg.useit.today.eegtoolkit.sampleapp.databinding.DeviceItemBinding;
import eeg.useit.today.eegtoolkit.view.CustomViewHolder;
import eeg.useit.today.eegtoolkit.vm.MuseListViewModel;

/**
 * Adapter that allows binding a list of devices to a list view,
 * by binding each device separately to its own single-device view.
 */
public class DeviceListAdapter extends RecyclerView.Adapter<CustomViewHolder<DeviceItemBinding>> {
  /** Context this adapter lives within. */
  private final Context ctx;

  /** Viewmodel of the list of devices that this is displaying. */
  private final MuseListViewModel viewModel;

  public DeviceListAdapter(final Context ctx, MuseListViewModel viewModel) {
    this.ctx = ctx;
    this.viewModel = viewModel;

    this.viewModel.setListener(new MuseListViewModel.MuseListListener() {
      @Override public void onScanForDevicesFinished() {
        // New devices, so we may need to rerender.
        DeviceListAdapter.this.notifyDataSetChanged();
      }

      @Override public void onDeviceSelected(Muse muse) {
        // Device selected, so open the activity for that single device.
        Intent intent = new Intent(ctx, DeviceDetailsActivity.class);
        intent.putExtra("mac", muse.getMacAddress());
        ctx.startActivity(intent);
      }
    });
  }

  @Override
  public CustomViewHolder<DeviceItemBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
    DeviceItemBinding binding = DataBindingUtil.inflate(
        LayoutInflater.from(this.ctx), R.layout.device_item, parent, false);
    return new CustomViewHolder<>(binding);
  }

  @Override
  public void onBindViewHolder(CustomViewHolder<DeviceItemBinding> holder, int position) {
    // View is created, so bind the view to the device it shows.
    holder.getBinding().setViewModel(this.viewModel.getDevices().get(position));
    holder.getBinding().setListViewModel(this.viewModel);
  }

  @Override
  public int getItemCount() {
    return this.viewModel.getDevices().size();
  }
}
