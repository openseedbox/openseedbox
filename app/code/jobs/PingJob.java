package code.jobs;

import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.WS;
import play.libs.WS.HttpResponse;

@Every("2mn")
public class PingJob extends Job {

	@Override
	public void doJob() throws Exception {
		//Ping the server to keep the Heroku dyno alive
		Logger.info("Pinging client.openseedbox.com to keep the server alive...");
		HttpResponse res = WS.url("http://client.openseedbox.com/ping").get();
		String response = res.getJson().getAsJsonObject().get("data").getAsString();
		Logger.info("Server responded: %s", response);
	}
	
}
