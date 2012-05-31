package validation;

import java.math.BigDecimal;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Check;

public class IsDecimalNumber extends Check {

	@Override
	public boolean isSatisfied(Object validatedObject, Object value) {
		try {
			String num = value.toString();
			if (!StringUtils.isEmpty(num)) {
				BigDecimal d = new BigDecimal(num);
			} else {
				throw new Exception();
			}
		} catch (Exception ex) {
			this.setMessage("validation.decimal_number");
		}
		return true;
	}
	
}
