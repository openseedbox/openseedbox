package code;

import java.util.ArrayList;
import java.util.List;
import models.Node;
import models.Torrent;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;


@Every("10s")
public class PopulateDatabaseJob extends Job {
	
	@Override
	public void doJob() {
		//get all nodes
		List<Node> nodes = Node.all().fetch();
		for(Node n : nodes) {
			try {
				Logger.debug("Populating database with data from node %s", n.name);
				List<Torrent> saveMe = new ArrayList<>();
				for (Torrent t : n.getTransmission().getTorrents()) {
					Logger.debug("Populating with torrent %s (%s)", t.hashString, t.name);
					Torrent inDb = Torrent.getByKey(t.hashString);
					inDb.merge(t);
					saveMe.add(inDb);
				}
				Torrent.batch().update(saveMe);
			} catch (MessageException ex) {
				Logger.error("Unable to populate database for node %s: %s", n.name, ex.getMessage());
			}
		}
	}

}
