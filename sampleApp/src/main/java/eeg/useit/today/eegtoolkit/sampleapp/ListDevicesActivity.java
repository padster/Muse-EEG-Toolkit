package eeg.useit.today.eegtoolkit.sampleapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.choosemuse.libmuse.MuseManagerAndroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eeg.useit.today.eegtoolkit.sampleapp.databinding.ActivityListDevicesBinding;
import eeg.useit.today.eegtoolkit.vm.MuseListViewModel;


/**
 * Example activity to show the list of Muse devices found,
 * which also handles permissions and connecting to the Muse API.
 */
public class ListDevicesActivity extends AppCompatActivity {
  final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

  /** ViewModel for muse devices found. */
  public final MuseListViewModel viewModel = new MuseListViewModel();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Make sure permissions are granted, asking for ones that aren't.
    askPermissions();

    MuseManagerAndroid.getInstance().setContext(this);

    ActivityListDevicesBinding binding =
        DataBindingUtil.setContentView(this, R.layout.activity_list_devices);
    binding.setViewModel(viewModel);

    // Hook up the binding for the list of devices.
    RecyclerView listView = (RecyclerView) findViewById(R.id.deviceList);
    listView.setLayoutManager(new LinearLayoutManager(this));
    listView.setAdapter(new DeviceListAdapter(this, viewModel));
  }


  //
  // Permissions code below. See
  // https://stackoverflow.com/questions/32708374/bluetooth-le-scanfilters-dont-work-on-android-m
  //

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    switch (requestCode) {
      case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
        Map<String, Integer> perms = new HashMap<>();
        perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
        for (int i = 0; i < permissions.length; i++) {
          perms.put(permissions[i], grantResults[i]);
        }

        if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
          Toast.makeText(
              ListDevicesActivity.this,
              "All Permission GRANTED !! Thank You :)",
              Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(
              ListDevicesActivity.this,
              "One or More Permissions are DENIED Exiting App :(",
              Toast.LENGTH_SHORT).show();
          finish();
        }
        break;
      default:
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  @TargetApi(Build.VERSION_CODES.M)
  private void askPermissions() {
    List<String> permissionsNeeded = new ArrayList<>();
    final List<String> permissionsList = new ArrayList<>();
    if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION)) {
      permissionsNeeded.add("Show Location");
    }
    if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      permissionsNeeded.add("Write to file");
    }

    if (permissionsList.size() > 0) {
      if (permissionsNeeded.size() > 0) {
        String message = "App need access to " + permissionsNeeded.get(0);
        for (int i = 1; i < permissionsNeeded.size(); i++) {
          message = message + ", " + permissionsNeeded.get(i);
        }
        showMessageOKCancel(message, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            requestPermissions(
                permissionsList.toArray(new String[permissionsList.size()]),
                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
            );
          }
        });
        return;
      }
      requestPermissions(
          permissionsList.toArray(new String[permissionsList.size()]),
          REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
      );
      return;
    }
  }

  private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
    new AlertDialog.Builder(ListDevicesActivity.this)
        .setMessage(message)
        .setPositiveButton("OK", okListener)
        .setNegativeButton("Cancel", null)
        .create()
        .show();
  }

  @TargetApi(Build.VERSION_CODES.M)
  private boolean addPermission(List<String> permissionsList, String permission) {
    if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
      permissionsList.add(permission);
      // Check for Rationale Option
      if (!shouldShowRequestPermissionRationale(permission)) {
        return false;
      }
    }
    return true;
  }
}
