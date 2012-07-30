package code;

import com.google.checkout.sdk.commands.ApiContext;
import com.google.checkout.sdk.commands.Environment;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import models.User;
import notifiers.Mails;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Play;
import play.mvc.Http;

public class Util {
	
	public static String getStackTrace(Throwable t) {
		if (t instanceof MessageException) {
			return t.getMessage();
		}
		if (Play.mode == Play.Mode.DEV) {
			return ExceptionUtils.getStackTrace(t);
		} else {
			Mails.sendError(t, Http.Request.current.get());
			return "An unhandled exception occured! The developers have been notified.";
		}
	}
	
	public static String getRateKb(long rateInBytes) {
		double d = rateInBytes;
		return String.format("%.2f", (d / 1024));
	}
	
	public static String getRateMb(long rateInBytes) {
		double d = rateInBytes;
		return String.format("%.2f", (d / (1024 * 1000)));
	}
	
	public static String getRateGb(long rateInBytes) {
		double d = rateInBytes;
		return String.format("%.2f", (d / (1024 * 1000000)));
	}	
	
	public static String getBestRate(long rateInBytes) {
		if (rateInBytes > (1024 * 1000000)) {
			return getRateGb(rateInBytes) + "gb";
		} else if (rateInBytes > (1024 * 1000)) {
			return getRateMb(rateInBytes) + "mb";
		} else {
			return getRateKb(rateInBytes) + "kb";
		}
	}
	
	public static String formatDate(Date d) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		return df.format(d);				
	}
	
	public static String formatDateTime(Date d) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		return df.format(d);		
	}
	
	public static String formatDateTime(DateTime d) {
		DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
		return d.toString(dtf);
	}
	
	public static List<SelectItem> toSelectItems(List<String> items) {
		List<SelectItem> ret = new ArrayList<SelectItem>();
		for (String s : items) {
			SelectItem si = new SelectItem();
			si.name = s;
			si.value = s;
			ret.add(si);
		}
		return ret;
	}
	
	public static List<SelectItem> toSelectItems(Map<String, String> items) {
		//the key is unique, so is the <option> value. The value is not unique, so is the <option> name. Confusing? Good.
		List<SelectItem> ret = new ArrayList<SelectItem>();
		for (String s : items.keySet()) {
			SelectItem si = new SelectItem();
			si.name = items.get(s);
			si.value = s;
			ret.add(si);
		}
		return ret;		
	}
	
	public static DateTime getLocalDate(Date systemDate, User u) {
		DateTimeZone tz = DateTimeZone.forID(u.timeZone);
		return new DateTime(systemDate).toDateTime(tz);
	}
	
	public static class SelectItem {
		public String name;
		public String value;
		public boolean selected;
	}
	
	public static ApiContext getGoogleApiContext() {
		String env = Play.configuration.getProperty("googlecheckout.environment", "sandbox").toLowerCase();
		Environment environment = Environment.SANDBOX;
		if (env.equals("production")) {
			environment = Environment.PRODUCTION;
		}
        String merchantId = Play.configuration.getProperty("googlecheckout.merchantid");
        String merchantKey =  Play.configuration.getProperty("googlecheckout.merchantkey");
        String currency = Play.configuration.getProperty("googlecheckout.currency", "USD");
        return new ApiContext(environment, merchantId, merchantKey, currency);
    }
	
}
