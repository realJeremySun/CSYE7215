package coffeehouse;

import java.util.HashMap;

/**
 * A Machine is used to make a particular Food.  Each Machine makes
 * just one kind of Food.  Each machine has a capacity: it can make
 * that many food items in parallel; if the machine is asked to
 * produce a food item beyond its capacity, the requester blocks.
 * Each food item takes at least item.cookTimeMS milliseconds to
 * produce.
 */
public class Machine {
	public final String machineName;
	public final Food machineFoodType;
	
	//YOUR CODE GOES HERE...
	public final int capacityIn;
	public int currentCapacity;
	public HashMap<Thread, CookAnItem> allFood; 
	//lock for machine capacity
	public final Object capacityLock; 

	/**
	 * The constructor takes at least the name of the machine,
	 * the Food item it makes, and its capacity.  You may extend
	 * it with other arguments, if you wish.  Notice that the
	 * constructor currently does nothing with the capacity; you
	 * must add code to make use of this field (and do whatever
	 * initialization etc. you need).
	 */
	public Machine(String nameIn, Food foodIn, int capacityIn) {
		this.machineName = nameIn;
		this.machineFoodType = foodIn;
		//YOUR CODE GOES HERE...
		this.allFood = new HashMap<Thread, CookAnItem>();
		this.capacityIn = capacityIn;
		this.currentCapacity = capacityIn;
		this.capacityLock = new Object();
	}
	

	/**
	 * This method is called by a Cook in order to make the Machine's
	 * food item.  You can extend this method however you like, e.g.,
	 * you can have it take extra parameters or return something other
	 * than Object.  It should block if the machine is currently at full
	 * capacity.  If not, the method should return, so the Cook making
	 * the call can proceed.  You will need to implement some means to
	 * notify the calling Cook when the food item is finished.
	 */
	public void makeFood(Food food,int orderNum) throws InterruptedException {
		//YOUR CODE GOES HERE...
		
		CookAnItem cookanitem = new CookAnItem(orderNum);
		Thread thread = new Thread(cookanitem);
		synchronized(allFood) {
			allFood.put(thread,cookanitem);
		}
		thread.start();	
	}
	
	public boolean waitFood(Order order) throws InterruptedException {
		synchronized(allFood) {
			if(allFood.isEmpty()) return true;
			else {
				for(Thread t: allFood.keySet()) {
					if(allFood.get(t).getOrderNum()==order.getOrderNum()) {
						t.join();
					}
				}
				return true;
			}
		}
	}

	//THIS MIGHT BE A USEFUL METHOD TO HAVE AND USE BUT IS JUST ONE IDEA
	private class CookAnItem implements Runnable {
		//remember which order this food come from 
		private int orderNum;
		
		public CookAnItem(int orderNum) {
			this.orderNum = orderNum;
		}

		public int getOrderNum() {
			return orderNum;
		}
		
		public void run() {
			try {
				//YOUR CODE GOES HERE...
				
				//start the machine
				synchronized(capacityLock) {
					//wait if the machine is in full capacity
					while(currentCapacity<=0) {
						synchronized(this) {
							capacityLock.wait();
						}
					}
					//processing the food
					Simulation.logEvent(SimulationEvent.machineCookingFood(Machine.this, machineFoodType));
					currentCapacity--;
				}
				
				Thread.sleep(machineFoodType.cookTimeMS);
				
				synchronized(capacityLock) {
					//food done
					Simulation.logEvent(SimulationEvent.machineDoneFood(Machine.this, machineFoodType));
					currentCapacity++;
					//notify other thread that this machine can cook more food
					synchronized(this) {
						capacityLock.notifyAll();
					}
					//notify other customer to pick up order
					synchronized(Simulation.orderLock) {
						Simulation.orderLock.notifyAll();
					}
				}
			} catch(InterruptedException e) { 
				Simulation.logEvent(SimulationEvent.machineEnding(Machine.this));
			}
		}
	}
 

	public String toString() {
		return machineName;
	}
}