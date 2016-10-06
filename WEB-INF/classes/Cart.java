import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Cart class models the shopping cart, which contains CartItem.
 * It also provides method to add() and remove() a CartItem.
 */

public class Cart {
	private List<CartItem> cart; // List of CartItems

	public Cart() {
		cart = new ArrayList<CartItem>();
	}

	// Add a CartItem into this Cart
	public void add(int id, String title, String author, float price, int qtyOrdered) {
		// Check if the id is already in the shopping cart
		Iterator<CartItem> iter = cart.iterator();
		while (iter.hasNext()) {
			CartItem item = iter.next();
			if (item.getId() == id) {
				// id found, increase qtyOrdered
				item.setQtyOrdered(item.getQtyOrdered() + qtyOrdered);
				return;
			}
		}
		// id not found, create a new CartItem
		cart.add(new CartItem(id, title, author, price, qtyOrdered));
	}

	// Update the quantity for the given id
	public boolean update(int id, int newQty) {
		Iterator<CartItem> iter = cart.iterator();
		while (iter.hasNext()) {
			CartItem item = iter.next();
			if (item.getId() == id) {
				// id found, update qtyOrdered
				item.setQtyOrdered(newQty);
				return true;
			}
		}
		return false;
	}

	// Remove a CartItem given its id
	public void remove(int id) {
		Iterator<CartItem> iter = cart.iterator();
		while (iter.hasNext()) {
			CartItem item = iter.next();
			if (item.getId() == id) {
				cart.remove(item);
				return;
			}
		}
	}

	// Get the number of CartItems in this Cart
	public int size() {
		return cart.size();
	}

	// Check if this Cart is empty
	public boolean isEmpty() {
		return size() == 0;
	}

	// Return all the CartItems in a List<CartItem>
	public List<CartItem> getItems() {
		return cart;
	}

	// Remove all the items in this Cart
	public void clear() {
		cart.clear();
	}
}