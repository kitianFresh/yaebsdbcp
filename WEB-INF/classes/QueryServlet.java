import java.io.*;
import java.sql.*;
import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.*;

public class QueryServlet extends HttpServlet {
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
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		Connection conn = null;
		Statement stmt = null;
		try {
			// Retrieve and process request parameters: "author" and "search"
			String author = request.getParameter("author");
			boolean hasAuthorParam = author != null && !author.equals("Select...");
			String searchWord = request.getParameter("search");
			boolean hasSearchParam = searchWord != null && ((searchWord = searchWord.trim()).length() > 0);
			out.println("<html><head><title>Query Results</title></head><body>");
			out.println("<h2>YAEBS - Query Results<h2>");

			if (!hasAuthorParam && !hasSearchParam) {
				out.println("<h3>Please select an author or enter a search term!</h3>");
				out.println("<p><a href='start'>Back to Select Menu</a></p>");
			}
			else {
				conn = pool.getConnection();
				stmt = conn.createStatement();

				// Form a SQL command based on the param(s) present
				StringBuilder sqlstr = new StringBuilder(); // more efficient than String
				sqlstr.append("SELECT * FROM books WHERE qty > 0 AND (");
				if (hasAuthorParam) {
					sqlstr.append("author = '").append(author).append("'");
				}
				if (hasSearchParam) {
					if (hasAuthorParam) {
						sqlstr.append(" OR ");
					}
					sqlstr.append("author LIKE '%").append(searchWord)
						  .append("%' OR title LIKE '%").append(searchWord).append("%'");
				}
				sqlstr.append(") ORDER BY author, title");
				//System.out.println(sqlstr);
				ResultSet rset = stmt.executeQuery(sqlstr.toString());

				if (!rset.next()) { // Check for empty ResultSet (no book found)
					out.println("<h3>Please select an author or enter a search term!</h3>");
					out.println("<p><a href='start'>Back to Select Menu</a></p>");
				}
				else {
					// Print the result in an HTML from inside a table
					out.println("<form method='get' action='cart'>");
					out.println("<input type='hidden' name='todo' value='add' />");
					out.println("<table border='1' cellpadding='6'>");
					out.println("<tr>");
					out.println("<th>&nbsp;</th>");
	               	out.println("<th>AUTHOR</th>");
	               	out.println("<th>TITLE</th>");
	               	out.println("<th>PRICE</th>");
	               	out.println("<th>QTY</th>");
	               	out.println("</tr>");

	               	do {
	               		String id = rset.getString("id");
                  		out.println("<tr>");
                  		out.println("<td><input type='checkbox' name='id' value='" + id + "' /></td>");
                  		out.println("<td>" + rset.getString("author") + "</td>");
                  		out.println("<td>" + rset.getString("title") + "</td>");
		                out.println("<td>$" + rset.getString("price") + "</td>");
		                out.println("<td><input type='text' size='3' value='1' name='qty" + id + "' /></td>");
		                out.println("</tr>");
	               	} while (rset.next());
	               	out.println("</table><br />");
 
     
               		// Submit and reset buttons
               		out.println("<input type='submit' value='Add to My Shopping Cart' />");
               		out.println("<input type='reset' value='CLEAR' /></form>");
 
               		// Hyperlink to go back to search menu
               		out.println("<p><a href='start'>Back to Select Menu</a></p>");

               		// Show "View Shopping Cart" if the cart is not empty
		        	HttpSession session = request.getSession(false); //check if session exists
		        	if (session != null) {
		        		Cart cart;
		        		synchronized (session) {
		        			// Retrieve the shopping cart for this session, if any. Otherwise, create one.
		        			cart = (Cart) session.getAttribute("cart");
		        			if (cart != null && !cart.isEmpty()) {
		        				out.println("<P><a href='cart?todo=view'>View Shopping Cart</a></p>");
		        			}
		        		}
		        	}
				}
			} 
			out.println("</body></html>");
		} catch (SQLException ex) {
			out.println("<h3>Service not available. Please try again later!</h3></body></html>");
			Logger.getLogger(QueryServlet.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			out.close();
			try {
				if (stmt != null) stmt.close();
				if (conn != null) conn.close();
			} catch (SQLException ex) {
				Logger.getLogger(QueryServlet.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}