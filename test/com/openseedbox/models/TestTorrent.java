package com.openseedbox.models;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import play.test.UnitTest;

public class TestTorrent extends UnitTest {

	private String chartToAppend = "a";

	String longName(int length) {
		StringBuilder sb = new StringBuilder("This name is a little longer than ");
		sb.append(length).append(StringUtils.repeat(chartToAppend, length));
		return sb.toString();
	}

	@Test
	public void testTorrentNameShort() {
		Torrent t = new Torrent();
		t.setName("This is just a short name");
		t.setHashString(DigestUtils.sha1Hex(t.getName()));
		t.insertOrUpdate();
	}

	@Test
	public void testTorrentName200() {
		Torrent t = new Torrent();
		t.setName(longName(200));
		t.setHashString(DigestUtils.sha1Hex(t.getName()));
		t.insertOrUpdate();
	}

	@Test
	public void testTorrentName5000() {
		Torrent t = new Torrent();
		t.setName(longName(5000));
		t.setHashString(DigestUtils.sha1Hex(t.getName()));
		t.insertOrUpdate();
	}

	@Test
	public void testTorrentName10000() {
		Torrent t = new Torrent();
		t.setName(longName(10000));
		t.setHashString(DigestUtils.sha1Hex(t.getName()));
		t.insertOrUpdate();
	}
}
