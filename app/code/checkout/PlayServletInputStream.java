package code.checkout;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletInputStream;


public class PlayServletInputStream extends ServletInputStream {
	
	private InputStream _is;
	
	public PlayServletInputStream(InputStream is) {
		_is = is;
	}

	@Override
	public int read() throws IOException {
		return _is.read();
	}
	
	
	
}
