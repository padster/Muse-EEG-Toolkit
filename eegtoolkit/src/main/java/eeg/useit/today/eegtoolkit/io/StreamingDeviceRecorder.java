package eeg.useit.today.eegtoolkit.io;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.Log;

import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.choosemuse.libmuse.MuseFileFactory;
import com.choosemuse.libmuse.MuseFileWriter;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

import eeg.useit.today.eegtoolkit.Constants;
import eeg.useit.today.eegtoolkit.vm.StreamingDeviceViewModel;

/**
 * Wraps a streaming device in an interface to allow recording its output to file.
 */
public class StreamingDeviceRecorder {
  // Currently only support recording from one device.
  private static final int SINGLE_DEVICE = 0;

  /** Prefix for file recorded. */
  private final String prefix;

  /** Device to record from. */
  private final StreamingDeviceViewModel device;

  /** All types to be recorded. */
  private final Set<MuseDataPacketType> types;

  /** Writer, set during recording. */
  private MuseFileWriter museFileWriter = null;

  /** File name location, set during recording. */
  private String fileName;

  public StreamingDeviceRecorder(
      Context context,
      String prefix,
      StreamingDeviceViewModel device,
      Set<MuseDataPacketType> types
  ) {
    if (PackageManager.PERMISSION_GRANTED !=
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      throw new UnsupportedOperationException("Don't have write permission");
    }

    this.prefix = prefix;
    this.device = device;
    this.types = types;
  }

  /** @return Whether the recording is currently taking place. */
  public boolean isRunning() {
    return this.museFileWriter != null;
  }

  /** Start the recording, can only be called once. */
  public void start() {
    assert this.museFileWriter == null;
    assert this.device.getConnectionState() == ConnectionState.CONNECTED;

    this.fileName = buildFileName();
    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    if (!dir.exists()) {
      dir.mkdir();
    }

    File child = new File(dir, this.fileName);
    if (!child.exists()) {
      try {
        child.createNewFile();
      } catch (IOException e) {
        Log.e(Constants.TAG, "Can't create file: " + e.getMessage());
        return;
      }
    }

    this.museFileWriter = MuseFileWriter.getFileWriter(MuseFileFactory.getMuseFile(child));
    this.museFileWriter.open();

    for (MuseDataPacketType type : types) {
      this.device.registerDataListenerWhenConnected(new MuseDataListener() {
        @Override
        public void receiveMuseDataPacket(MuseDataPacket packet, Muse muse) {
          if (StreamingDeviceRecorder.this.isRunning()) {
            museFileWriter.addDataPacket(SINGLE_DEVICE, packet);
          }
        }

        @Override
        public void receiveMuseArtifactPacket(MuseArtifactPacket packet, Muse muse) {
          if (StreamingDeviceRecorder.this.isRunning()) {
            museFileWriter.addArtifactPacket(SINGLE_DEVICE, packet);
          }
        }
      }, type);
    }
  }

  /**
   * Stops the recording, can only be called once.
   * @return File name where the recording was saved.
   */
  public String stopAndSave() {
    this.device.removeAllListeners();
    this.museFileWriter.flush();
    this.museFileWriter.close();
    this.museFileWriter = null;
    return this.fileName;
  }

  /** @return File name for a new recording. */
  private String buildFileName() {
    String prefix = this.prefix == null ? "" : this.prefix + "_";
    String date = (new DateFormat()).format("yyyy-MM-dd-hh-mm-ss", new Date()).toString();
    return prefix + date + ".muse";
  }
}
