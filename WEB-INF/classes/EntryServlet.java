import java.io.*;
import java.sql.*;
import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.*;

public class EntryServlet extends HttpServlet {
	private DataSource pool;  // Database connection pool

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			// Create a JNDI Initial context to be able to lookup the DataSource
			InitialContext ctx = new InitialContext();
			// Lookup the DataSource, which will be backed by a pool
			//  that the application server provides.
			pool = (DataSource)ctx.lookup("java:comp/env/jdbc/mysql_ebookshop");
			if (pool == null) {
				throw new ServletException("Unknown DataSource 'jdbc/mysql_ebookshop'");
			}
		} catch (NamingException ex) {
			Logger.getLogger(EntryServlet.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		Connection conn = null;
		Statement stmt = null;
		try {
			//conn = DriverManager.getConnection(databaseURL, username, password);
			conn = pool.getConnection(); // get a connection pool
			stmt = conn.createStatement();
			String sqlstr = "SELECT DISTINCT author FROM books WHERE qty > 0";
			ResultSet rset = stmt.executeQuery(sqlstr);

			out.println("<html><head><title>Welcome to YaEshop</title></head><body>");
         	out.println("<h2>Welcome to Yet Another E-BookShop</h2>");
         	// Begin an HTML form
         	out.println("<form method='get' action='search'>");
 
         	// A pull-down menu of all the authors with a no-selection option
         	out.println("Choose an Author: <select name='author' size='1'>");
         	out.println("<option value=''>Select...</option>");  // no-selection
         	while (rset.next()) {
         		String author = rset.getString("author");
         		out.println("<option value='" + author + "'>" + author + "</option>");
         	}
         	out.println("</select><br />");
         	out.println("<p>OR</p>");

         	// A text field for entering search word for pattern matching
         	out.println("Search \"Title\" or \"Author\": <input type='text' name='search' />");
 
	        // Submit and reset buttons
	        out.println("<br /><br />");
	        out.println("<input type='submit' value='SEARCH' />");
	        out.println("<input type='reset' value='CLEAR' />");
        	out.println("</form>");

        	out.println("</body></html>");
		} catch (SQLException ex) {
			out.println("<h3>Service not available. Please try again later!</h3></body></html>");
			Logger.getLogger(EntryServlet.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			out.close();
			try {
				if (stmt != null) stmt.close();
				if (conn != null) conn.close(); // return the connection pool
			} catch (SQLException ex) {
				Logger.getLogger(EntryServlet.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

	}
}