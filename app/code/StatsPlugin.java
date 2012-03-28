package code;

import java.lang.reflect.Method;
import play.PlayPlugin;
import play.mvc.Http.Header;
import play.mvc.Http.Response;

public class StatsPlugin extends PlayPlugin {

    long start_time;

    @Override
    public void beforeActionInvocation(Method actionMethod) {
        start_time = System.currentTimeMillis();
    }

    @Override
    public void afterActionInvocation() {
        long time = System.currentTimeMillis() - start_time;
        Header h = new Header("X-Generated-In", "" + time);
        Response.current().headers.put(h.name, h);
    }
}
