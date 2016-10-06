/**
 * The class CartItem models an item in the Cart.
 * This class shall not be accessed by the controlling logic directly.
 * Instead Use Cart.add() or Cart.remove() to add or remove an item from the Cart.
 */

public class CartItem {
	private int id;
	private String title;
	private String author;
	private float price;
	private int qtyOrdered;

	// Constructor
   	public CartItem(int id, String title,
        	String author, float price, int qtyOrdered) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.price = price;
		this.qtyOrdered = qtyOrdered;
    }
 
	public int getId() {
		return id;
	}

	public String getAuthor() {
		return author;
	}

	public String getTitle() {
		return title;
	}

	public float getPrice() {
		return price;
	}

	public int getQtyOrdered() {
		return qtyOrdered;
	}

	public void setQtyOrdered(int qtyOrdered) {
		this.qtyOrdered = qtyOrdered;
	}
}