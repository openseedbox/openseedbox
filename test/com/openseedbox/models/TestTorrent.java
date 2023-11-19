package com.openseedbox.models;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import play.test.UnitTest;

public class TestTorrent extends UnitTest {

	private String chartToAppend = "a";

	String longName(int length) {
		return longString("This name is a little longer than ", length);
	}

	String longLink(int length) {
		return longString("my-awesome-openseedbox-backend-app-127-0-0-1.nip.io:9001/download/zip/", length);
	}

	String longString(String start, int length) {
		StringBuilder sb = new StringBuilder(start);
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

	@Test
	public void testTorrentZipDownloadLink100() {
		Torrent t = new Torrent();
		t.setName("Longer");
		t.setHashString(DigestUtils.sha1Hex(t.getName()));
		t.setZipDownloadLink(longLink(100));
		t.insertOrUpdate();
	}

	@Test
	public void testTorrentZipDownloadLink200() {
		Torrent t = new Torrent();
		t.setName("Even longer");
		t.setHashString(DigestUtils.sha1Hex(t.getName()));
		t.setZipDownloadLink(longLink(200));
		t.insertOrUpdate();
	}

	@Test
	public void testTorrentZipDownloadLink5000() {
		Torrent t = new Torrent();
		t.setName("The longest");
		t.setHashString(DigestUtils.sha1Hex(t.getName()));
		t.setZipDownloadLink(longLink(5000));
		t.insertOrUpdate();
	}

	@Test
	public void testTorrentZipDownloadLink10000() {
		Torrent t = new Torrent();
		t.setName("It's over 9000!");
		t.setHashString(DigestUtils.sha1Hex(t.getName()));
		t.setZipDownloadLink(longLink(10000));
		t.insertOrUpdate();
	}
}
