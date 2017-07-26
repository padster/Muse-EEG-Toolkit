package eeg.useit.today.eegtoolkit.common;

import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseDataListener;

/**
 * Utility class for packet listeners that ignore artifact packets.
 * Subclasses extending this only need to provide the data packet handling logic.
 */
public abstract class BaseDataPacketListener extends MuseDataListener {
  @Override
  public void receiveMuseArtifactPacket(MuseArtifactPacket museArtifactPacket, Muse muse) {
    // NO-OP, artifact packet ignored.
  }
}
