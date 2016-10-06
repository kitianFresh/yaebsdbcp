import java.io.*;
import java.sql.*;
import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.*;

public class OrderServlet extends HttpServlet {
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
      	Statement  stmt = null;
      	ResultSet  rset = null;
      	String     sqlstr = null;
 
		try {
			out.println("<html><head><title>Order Confirmation</title></head><body>");
			out.println("<h2>YAEBS - Order Confirmation</h2>");

			// Retrieve and process request parameters: id(s), cust_name, cust_email, cust_phone
			String[] ids = request.getParameterValues("id");  // Possibly more than one values
			String custName = request.getParameter("cust_name");
			boolean hasCustName = custName != null &&
			((custName = InputFilter.htmlFilter(custName.trim())).length() > 0);
			String custEmail = request.getParameter("cust_email").trim();
			boolean hasCustEmail = custEmail != null &&
			((custEmail = InputFilter.htmlFilter(custEmail.trim())).length() > 0);
			String custPhone = request.getParameter("cust_phone").trim();
			boolean hasCustPhone = custPhone != null &&
			((custPhone = InputFilter.htmlFilter(custPhone.trim())).length() > 0);

			// Validate inputs
			if (ids == null || ids.length == 0) {
				out.println("<h3>Please Select a Book!</h3>");
			} else if (!hasCustName) {
				out.println("<h3>Please Enter Your Name!</h3>");
			} else if (!hasCustEmail || (custEmail.indexOf('@') == -1)) {
				out.println("<h3>Please Enter Your e-mail (user@host)!</h3>");
			} else if (!hasCustPhone || !InputFilter.isValidPhone(custPhone)) {
				out.println("<h3>Please Enter an 8-digit Phone Number!</h3>");
			} else {
				// We shall build our output in a buffer, so that it will not be interrupted
				//  by error messages.
				StringBuilder outBuf = new StringBuilder();
				// Display the name, email and phone (arranged in a table)
				outBuf.append("<table>");
				outBuf.append("<tr><td>Customer Name:</td><td>").append(custName).append("</td></tr>");
				outBuf.append("<tr><td>Customer Email:</td><td>").append(custEmail).append("</td></tr>");
				outBuf.append("<tr><td>Customer Phone Number:</td><td>").append(custPhone).append("</td></tr></table>");

				conn = pool.getConnection();
				stmt = conn.createStatement();
				// We shall manage our transaction (because multiple SQL statements issued)
				conn.setAutoCommit(false);

				// Print the book(s) ordered in a table
				outBuf.append("<br />");
				outBuf.append("<table border='1' cellpadding='6'>");
				outBuf.append("<tr><th>AUTHOR</th><th>TITLE</th><th>PRICE</th><th>QTY</th></tr>");

				boolean error = false;
				float totalPrice = 0f;
				for (String id : ids) {
					sqlstr = "SELECT * FROM books WHERE id = " + id;
					//System.out.println(sqlstr);  // for debugging
					rset = stmt.executeQuery(sqlstr);

					// Expect only one row in ResultSet
					rset.next();
					int qtyAvailable = rset.getInt("qty");
					String title = rset.getString("title");
					String author = rset.getString("author");
					float price = rset.getFloat("price");

					// Validate quantity ordered
					String qtyOrderedStr = request.getParameter("qty" + id);
					int qtyOrdered = InputFilter.parsePositiveInt(qtyOrderedStr);
					if (qtyOrdered == 0) {
						out.println("<h3>Please Enter a valid quantity for \"" + title + "\"!</h3>");
						error = true;
						break;
					} else if (qtyOrdered > qtyAvailable) {
						out.println("<h3>There are insufficient copies of \"" + title + "\" available!</h3>");
						error = true;
						break;
					} else {
						// Okay, update the books table and insert an order record
						sqlstr = "UPDATE books SET qty = qty - " + qtyOrdered + " WHERE id = " + id;
						//System.out.println(sqlstr);  // for debugging
						stmt.executeUpdate(sqlstr);

						sqlstr = "INSERT INTO order_records values ("
						  + id + ", " + qtyOrdered + ", '" + custName + "', '"
						  + custEmail + "', '" + custPhone + "')";
						//System.out.println(sqlstr);  // for debugging
						stmt.executeUpdate(sqlstr);

						// Display this book ordered
						outBuf.append("<tr>");
						outBuf.append("<td>").append(author).append("</td>");
						outBuf.append("<td>").append(title).append("</td>");
						outBuf.append("<td>").append(price).append("</td>");
						outBuf.append("<td>").append(qtyOrdered).append("</td></tr>");
						totalPrice += price * qtyOrdered;
					}
				}

				if (error) {
					conn.rollback();
				} else {
					// No error, print the output from the StringBuilder.
					out.println(outBuf.toString());
					out.println("<tr><td colspan='4' align='right'>Total Price: $");
					out.printf("%.2f</td></tr>", totalPrice);
					out.println("</table>");

					out.println("<h3>Thank you.</h3>");
					out.println("<p><a href='start'>Back to Select Menu</a></p>");
					// Commit for ALL the books ordered.
					conn.commit();
				}
			}
			out.println("</body></html>");
		} catch (SQLException ex) {
			try {
				conn.rollback();  // rollback the updates
				out.println("<h3>Service not available. Please try again later!</h3></body></html>");
			} catch (SQLException ex1) { }
			Logger.getLogger(OrderServlet.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			out.close();
			try {
				if (stmt != null) stmt.close();
				if (conn != null) conn.close();
			} catch (SQLException ex) {
				Logger.getLogger(OrderServlet.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}