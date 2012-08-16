package code;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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
	
	public static String formatMoney(BigDecimal bd) {
		NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
		nf.setRoundingMode(RoundingMode.HALF_EVEN);
		return nf.format(bd.setScale(2, RoundingMode.HALF_EVEN));
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
	
	public static Map<String, String> getUrlParameters(String url) {
		Map<String, String> params = new HashMap<String, String>();
		String[] urlParts = url.split("\\?");
		String query = "";
		if (urlParts.length > 0) {
			query = urlParts[0];
		} else if (urlParts.length > 1) {
			query = urlParts[1];
		}
		if (!query.equals("")) {
			for (String param : query.split("&")) {
				String pair[] = param.split("=");
				try {
					String key = URLDecoder.decode(pair[0], "UTF-8");
					String value = "";
					if (pair.length > 1) {
						value = URLDecoder.decode(pair[1], "UTF-8");
					}
					params.put(key, value);				
				} catch (UnsupportedEncodingException ex) {
					//ignore, fuck you java
				}
			}
		}
		return params;
	}
	
	public static String stripHtml(String s) {
		return s.replaceAll("\\<.*?>","");
	}
	
	
	public static class SelectItem {
		public String name;
		public String value;
		public boolean selected;
	}
	
}
