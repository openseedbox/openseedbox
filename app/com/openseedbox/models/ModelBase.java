package com.openseedbox.models;

import com.avaje.ebean.Transaction;

import javax.persistence.MappedSuperclass;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@MappedSuperclass
public abstract class ModelBase extends play.modules.ebean.Model {
	
	@Deprecated
	protected static ResultSet raw(String query) throws SQLException {
		Transaction transaction = ebean().beginTransaction();

		//ebean().createQuery(ModelBase.class).where().raw(query).
		Connection c = transaction.getConnection();
		c.setAutoCommit(true);
		return c.createStatement().executeQuery(query);
	}

	public static <T extends play.modules.ebean.EbeanSupport> void save(List<T> list) {
		ebean().saveAll(list);
	}
}
