package code;

import java.math.BigDecimal;

public class BigDecimalUtils {
	
	/**
	 * Returns true if one == two
	 * @param one The first number
	 * @param two The second number
	 * @return True if one == two, false otherwise
	 */	
	public static boolean Equals(BigDecimal one, BigDecimal two) {
		return (one.compareTo(two) == 0);
	}
	
	/**
	 * Returns true if one > two
	 * @param one The first number
	 * @param two The second number
	 * @return True if one > two, false otherwise
	 */
	public static boolean GreaterThan(BigDecimal one, BigDecimal two) {
		return (one.compareTo(two) > 0);
	}
	
	/**
	 * Returns true if one < two
	 * @param one The first number
	 * @param two The second number
	 * @return True if one < two, false otherwise
	 */
	public static boolean LessThan(BigDecimal one, BigDecimal two) {
		return (one.compareTo(two) < 0);
	}
	
	public static boolean GreaterThanOrEqual(BigDecimal one, BigDecimal two) {
		if (!Equals(one, two)) {
			return GreaterThan(one, two);
		}
		return true;
	}
	
	public static boolean LessThanOrEqual(BigDecimal one, BigDecimal two) {
		if (!Equals(one, two)) {
			return LessThan(one, two);
		}
		return true;
	}
	
}
