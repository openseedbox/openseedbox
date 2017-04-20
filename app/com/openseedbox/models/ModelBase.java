package com.openseedbox.models;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import play.Logger;
import play.modules.siena.EnhancedModel;
import siena.Generator;
import siena.Id;
import siena.PersistenceManagerFactory;
import siena.jdbc.JdbcPersistenceManager;

public abstract class ModelBase extends EnhancedModel {
	
	@Id(Generator.AUTO_INCREMENT)
	protected long id = Long.MAX_VALUE;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public boolean isNew() {
		return id != Long.MAX_VALUE;
	}
	
	public void insertOrUpdate() {
		if (isNew()) {
			this.insert();
		} else {
			this.update();
		}
	}
	
	protected static ResultSet raw(String query) throws SQLException {
		JdbcPersistenceManager m = (JdbcPersistenceManager) PersistenceManagerFactory.getPersistenceManager(ModelBase.class);
		Method[] methods = m.getClass().getMethods();
		Connection c = null;
		//getConnection() is protected, so run it using reflection
		for (Method me : methods) {
			if (me.getName().equals("getConnection")) {
				try {
					c = (Connection) me.invoke(m);
				} catch (Exception ex) {
					if (ex instanceof SQLException) {
						throw (SQLException) ex;
					}
					Logger.error("Error: %s", ex);
				}
			}
		}
		return c.createStatement().executeQuery(query);		
	}

	/**
	 * siena.Model.save() is not auto increment safe with PostgreSQL! Use insertOrUpdate() instead!
	 */
	@Override
	@Deprecated
	public void save() {
		insertOrUpdate();
	}
}
