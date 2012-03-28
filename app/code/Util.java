package code;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author erin
 */
public class Util {
	
	public static String getStackTrace(Throwable t) {
		if (t == null) { return ""; }
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);	
		return sw.toString();
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
	
	public static String formatDate(Date d) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		return df.format(d);				
	}
	
	public static String formatDateTime(Date d) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		return df.format(d);		
	}

}
