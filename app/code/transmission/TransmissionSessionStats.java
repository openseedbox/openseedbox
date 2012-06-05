package code.transmission;

import com.google.gson.annotations.SerializedName;

public class TransmissionSessionStats {

	public int activeTorrentCount;
	public long downloadSpeed;
	public int pausedTorrentCount;
	public int torrentCount;
	public long uploadSpeed;
	@SerializedName("cumulative-stats")
	public TransmissionStats cumulativeStats;
	@SerializedName("current-stats")
	public TransmissionStats currentStats;

	public class TransmissionStats {

		public long uploadedBytes;
		public long downloadedBytes;
		public int filesAdded;
		public int sessionCount;
		public long secondsActive;
	}
}
