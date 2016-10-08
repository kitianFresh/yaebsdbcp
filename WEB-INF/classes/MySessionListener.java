import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
 
public class MySessionListener implements HttpSessionListener {
 
	public void sessionCreated(HttpSessionEvent event) {
		// java.lang.IllegalStateException: Property max age can not be added to SessionCookieConfig for context /yaebsdbcp as the context has been initialised
		// event.getSession().getServletContext().getSessionCookieConfig().setMaxAge(Integer.MAX_VALUE);

		//event.getSession().set
		System.out.println("A new session is created session id = " + event.getSession().getId());
	}
 
	public void sessionDestroyed(HttpSessionEvent event) {
		System.out.println("session is destroyed");
	}
 
}