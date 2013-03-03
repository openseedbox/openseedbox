package com.openseedbox.test.client;

import com.openseedbox.models.Node;
import com.openseedbox.models.Plan;
import com.openseedbox.models.Torrent;
import com.openseedbox.models.User;
import com.openseedbox.models.User.UserStats;
import com.openseedbox.models.UserTorrent;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Test logic-based things that can be easily tested
 * @author Erin Drummond
 */
public class TestLogic extends UnitTest {
	
	long bytes_in_190gb = 204010946560l;
	long bytes_in_85gb = 91268055040l;
	long bytes_in_60gb = 64424509440l;
	
	long ten_mbps_in_bs = 1310720l;
	long two_mbps_in_bs = 262144l;
	
	@Test
	public void testUserStats() {
		User u = new CustomUser();
		assertEquals(u.getPlan().getMaxDiskspaceBytes(), bytes_in_190gb);		
		UserStats us = u.getUserStats();
		assertEquals(us.getAvailableSpaceGb(), "190 GB");
		assertEquals(us.getUsedSpaceGb(), "85 GB");
		assertEquals(us.getPercentUsed(), "44.74%");
		assertEquals(us.getTotalDownloadRate(), "1.2 MB");
		assertEquals(us.getTotalUploadRate(), "256 KB");
	}
	
	class CustomUser extends User {

		@Override
		public Plan getPlan() {
			Plan p = new Plan();
			p.setMaxDiskspaceGb(190);
			return p;
		}

		@Override
		public List<UserTorrent> getTorrents() {
			UserTorrent ut = new UserTorrent();			
			Torrent nt = new Torrent();
			nt.setDownloadSpeedBytes(ten_mbps_in_bs);
			nt.setUploadSpeedBytes(two_mbps_in_bs);
			nt.setTotalSizeBytes(bytes_in_85gb);
			nt.setUploadedBytes(bytes_in_60gb);
			ut.setTorrent(nt);
			return Arrays.asList(new UserTorrent[] { ut });
		}				
		
	}
	
}
