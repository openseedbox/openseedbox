package com.openseedbox.plugins.isohunt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.openseedbox.code.Util;
import com.openseedbox.plugins.OpenseedboxPlugin;
import java.util.ArrayList;
import java.util.List;
import com.openseedbox.models.User;
import play.libs.WS;
import play.libs.WS.HttpResponse;

public class IsohuntSearchPlugin extends OpenseedboxPlugin {

	public IsohuntSearchPlugin(User u) {
		super(u);
	}

	@Override
	public String getPluginName() {
		return "Isohunt Search";
	}

	@Override
	public boolean isSearchPlugin() {
		return true;
	}

	@Override
	public List<PluginSearchResult> doSearch(String terms) {
		List<PluginSearchResult> ret = new ArrayList<PluginSearchResult>();
		HttpResponse res = WS.url("http://ca.isohunt.com/js/json.php?ihq=%s&rows=20&sort=seeds", terms).get();
		if (res.getJson() != null) {
			JsonObject itemsObject = res.getJson().getAsJsonObject().getAsJsonObject("items");
			if (itemsObject != null) {
				JsonArray items = itemsObject.getAsJsonArray("list");
				for (JsonElement i : items) {
					JsonObject it = i.getAsJsonObject();
					PluginSearchResult psr = new PluginSearchResult();
					psr.setTorrentName(Util.stripHtml(it.get("title").getAsString()));
					psr.setTorrentUrl(it.get("enclosure_url").getAsString());
					psr.setCurrentPeers(it.get("leechers").getAsString());
					psr.setCurrentSeeders(it.get("Seeds").getAsString());
					psr.setFileSize(Util.getBestRate(it.get("length").getAsLong()));
					ret.add(psr);
				}
			}
		}
		return ret;
	}
	
}
