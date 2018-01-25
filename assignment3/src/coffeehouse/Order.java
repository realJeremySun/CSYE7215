//a new class for order, which take two params:
//List<Food> and order number
package coffeehouse;

import java.util.List;

public class Order {
	private int orderNum;
	private List<Food> order;
	
	public Order(int orderNum, List<Food> order) {
		this.orderNum = orderNum;
		this.order = order;
	}

	public int getOrderNum() {
		return orderNum;
	}

	public List<Food> getOrder() {
		return order;
	}	
}
