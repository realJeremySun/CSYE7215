package coffeehouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order.  When running, an
 * customer attempts to enter the coffee shop (only successful if the
 * coffee shop has a free table), place its order, and then leave the 
 * coffee shop when the order is complete.
 */
public class Customer implements Runnable {
	//JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
	private final String name;
	private final List<Food> order;
	private final int orderNum;    
	
	private static int runningCounter = 0;

	/**
	 * You can feel free modify this constructor.  It must take at
	 * least the name and order but may take other parameters if you
	 * would find adding them useful.
	 */
	public Customer(String name, List<Food> order) {
		this.name = name;
		this.order = order;
		this.orderNum = ++runningCounter;
	}

	public String toString() {
		return name;
	}
	public int getOrderNum() {
		return orderNum;
	}

	/** 
	 * This method defines what an Customer does: The customer attempts to
	 * enter the coffee shop (only successful when the coffee shop has a
	 * free table), place its order, and then leave the coffee shop
	 * when the order is complete.
	 */
	public void run() {
		//YOUR CODE GOES HERE...
		//start the customer
		Simulation.logEvent(SimulationEvent.customerStarting(this));
		
		//wait for table;
		synchronized(Simulation.spaceLock) {
			try {
				while(Simulation.currentSpace<=0) Simulation.spaceLock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//enter the shop
			Simulation.currentSpace--;
			Simulation.logEvent(SimulationEvent.customerEnteredCoffeeShop(this));
		}
		
		//place the order
		synchronized(Simulation.orderList) {
			Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, order, orderNum));
			Simulation.orderList.add(new Order(orderNum,order));
		}
		
		//notify one cook
		synchronized(Simulation.cookLock) {
			Simulation.cookLock.notify();
		}
		
		//notify cook that customer is waiting
		synchronized(Simulation.orderpickup) {
			Simulation.orderpickup.notifyAll();
		}
		
		//wait for the order
		try {
			while(Simulation.readyOrder.getOrderNum()!=orderNum)
				synchronized(Simulation.orderLock) {
					Simulation.orderLock.wait();
				}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//customer receive the order
		Simulation.readyOrder=new Order(-1,new ArrayList<Food>());;
		Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, order, orderNum));
		
		//notify cook that customer has pick up order
		synchronized(Simulation.orderpickup) {
			Simulation.orderpickup.notifyAll();
		}
		//customer enjoy his food
		try {
			Thread.sleep(new Random().nextInt(800));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		synchronized(Simulation.spaceLock) {
			//customer leave
			Simulation.currentSpace++;
			Simulation.logEvent(SimulationEvent.customerLeavingCoffeeShop(this));
			Simulation.spaceLock.notifyAll();
		}
		//notify cook that customer has pick up order
		synchronized(Simulation.orderpickup) {
			Simulation.orderpickup.notifyAll();
		}
		//notify other customer to pick up order
		synchronized(Simulation.orderLock) {
			Simulation.orderLock.notifyAll();
		}
	}
}