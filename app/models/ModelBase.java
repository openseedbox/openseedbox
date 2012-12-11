package models;

import play.modules.siena.EnhancedModel;
import siena.Generator;
import siena.Id;

public abstract class ModelBase extends EnhancedModel {
	
	@Id(Generator.AUTO_INCREMENT)
	private long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
}
