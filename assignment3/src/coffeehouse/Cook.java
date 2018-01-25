package coffeehouse;


/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Eaters and process them.
 */
public class Cook implements Runnable {
	private final String name;

	/**
	 * You can feel free modify this constructor.  It must
	 * take at least the name, but may take other parameters
	 * if you would find adding them useful. 
	 *
	 * @param: the name of the cook
	 */
	public Cook(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	/**
	 * This method executes as follows.  The cook tries to retrieve
	 * orders placed by Customers.  For each order, a List<Food>, the
	 * cook submits each Food item in the List to an appropriate
	 * Machine, by calling makeFood().  Once all machines have
	 * produced the desired Food, the order is complete, and the Customer
	 * is notified.  The cook can then go to process the next order.
	 * If during its execution the cook is interrupted (i.e., some
	 * other thread calls the interrupt() method on it, which could
	 * raise InterruptedException if the cook is blocking), then it
	 * terminates.
	 */
	public void run() {
		//start the cook
		Simulation.logEvent(SimulationEvent.cookStarting(this));
		
		try {
			//the cook never stop unless some call interrupt()
			while(true) {
				//wait for order
				while(Simulation.orderList.isEmpty()) {
					synchronized(Simulation.cookLock) {
						Simulation.cookLock.wait();
					}
				}
				Order order;
				synchronized(Simulation.orderList) {
					//receiving order
					if(Simulation.orderList.isEmpty()) continue;
					order = Simulation.orderList.removeFirst();
					Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, order.getOrder(), order.getOrderNum()));
				}
				//submit order to machine
				for(Food food : order.getOrder()) {
					switch(food.toString()){
					case "burger" :
						Simulation.logEvent(SimulationEvent.cookStartedFood(this, food, order.getOrderNum()));
						Simulation.machineGrill.makeFood(food,order.getOrderNum());
						break;
					case "fries" :
						Simulation.logEvent(SimulationEvent.cookStartedFood(this, food, order.getOrderNum()));
						Simulation.fryer.makeFood(food,order.getOrderNum());
						break;
					case "coffee" : 
						Simulation.logEvent(SimulationEvent.cookStartedFood(this, food, order.getOrderNum()));
						Simulation.coffeeMaker2000.makeFood(food,order.getOrderNum());
						break;
					}
				}
				//wait for the foods to be done
				Simulation.machineGrill.waitFood(order);
				Simulation.logEvent(SimulationEvent.cookFinishedFood(this, FoodType.burger, order.getOrderNum()));
				Simulation.fryer.waitFood(order);
				Simulation.logEvent(SimulationEvent.cookFinishedFood(this, FoodType.fries, order.getOrderNum()));
				Simulation.coffeeMaker2000.waitFood(order);
				Simulation.logEvent(SimulationEvent.cookFinishedFood(this, FoodType.coffee, order.getOrderNum()));
				
				//notify the customer to pick the previous order
				synchronized(Simulation.orderLock) {
					Simulation.orderLock.notifyAll();
				}
				synchronized(Simulation.readyOrder) {
					//complete order
					Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, order.getOrderNum()));
					//notify the customer to pick the previous order
					synchronized(Simulation.orderLock) {
						Simulation.orderLock.notifyAll();
					}
					
					//wait for customer pick up order
					synchronized(Simulation.orderpickup) {
						while(Simulation.readyOrder.getOrderNum()!=-1) {
							Simulation.orderpickup.wait();
							//notify the customer to pick the previous order
							synchronized(Simulation.orderLock) {
								Simulation.orderLock.notifyAll();
							}
						}
					}
					Simulation.readyOrder = order;
				}
					//notify the customer to pick current order
					synchronized(Simulation.orderLock) {
						Simulation.orderLock.notifyAll();
					}
				
			}
		}
		catch(InterruptedException e) {
			// This code assumes the provided code in the Simulation class
			// that interrupts each cook thread when all customers are done.
			// You might need to change this if you change how things are
			// done in the Simulation class.
			Simulation.logEvent(SimulationEvent.cookEnding(this));
		}
	}
}