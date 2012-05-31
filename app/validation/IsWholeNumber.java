package validation;

import java.math.BigDecimal;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Check;

public class IsWholeNumber extends Check {

	@Override
	public boolean isSatisfied(Object validatedObject, Object value) {
		try {
			String num = value.toString();
			if (!StringUtils.isEmpty(num)) {
				int i = Integer.parseInt(num);
			} else {
				throw new Exception();
			}
		} catch (Exception ex) {
			this.setMessage("validation.whole_number");
			return false;
		}
		return true;
	}
	
}
