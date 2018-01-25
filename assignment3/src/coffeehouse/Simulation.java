package coffeehouse;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Simulation is the main class used to run the simulation.  You may
 * add any fields (static or instance) or any methods you wish.
 */
public class Simulation {
	// List to track simulation events during simulation
	public static List<SimulationEvent> events;  
	
	//current space 
	public static int currentSpace;
	//lock for cook
	public static final Object cookLock = new Object(); 
	//lock for currentSpace
	public static final Object spaceLock = new Object(); 
	//lock for current order
	public static final Object orderLock = new Object();
	//lock for order pick up
	public static final Object orderpickup = new Object(); 
	//a list for current order
	public static LinkedList<Order> orderList;
	//a running filed for order ready for delivery
	public static Order readyOrder = new Order(-1,new ArrayList<Food>());
	
	//three machines
	public static Machine machineGrill;
	public static Machine fryer;
	public static Machine coffeeMaker2000;

	/**
	 * Used by other classes in the simulation to log events
	 * @param event
	 */
	public static void logEvent(SimulationEvent event) {
		events.add(event);
		System.out.println(event);
	}

	/**
	 * 	Function responsible for performing the simulation. Returns a List of 
	 *  SimulationEvent objects, constructed any way you see fit. This List will
	 *  be validated by a call to Validate.validateSimulation. This method is
	 *  called from Simulation.main(). We should be able to test your code by 
	 *  only calling runSimulation.
	 *  
	 *  Parameters:
	 *	@param numCustomers the number of customers wanting to enter the coffee shop
	 *	@param numCooks the number of cooks in the simulation
	 *	@param numTables the number of tables in the coffe shop (i.e. coffee shop capacity)
	 *	@param machineCapacity the capacity of all machines in the coffee shop
	 *  @param randomOrders a flag say whether or not to give each customer a random order
	 *
	 */
	public static List<SimulationEvent> runSimulation(
			int numCustomers, int numCooks,
			int numTables, 
			int machineCapacity,
			boolean randomOrders
			) {

		//This method's signature MUST NOT CHANGE.  

		//We are providing this events list object for you.  
		//  It is the ONLY PLACE where a concurrent collection object is 
		//  allowed to be used.
		events = Collections.synchronizedList(new ArrayList<SimulationEvent>());

		// Start the simulation
		logEvent(SimulationEvent.startSimulation(numCustomers,
				numCooks,
				numTables,
				machineCapacity));

		// Set things up you might need
		currentSpace=numTables;
		orderList= new LinkedList<Order>();
		
		// Start up machines
		machineGrill = new Machine("machineGrill", FoodType.burger, machineCapacity);
		Simulation.logEvent(SimulationEvent.machineStarting(machineGrill,  FoodType.burger, machineCapacity));
		fryer = new Machine("fryer", FoodType.fries, machineCapacity);
		Simulation.logEvent(SimulationEvent.machineStarting(fryer,  FoodType.fries, machineCapacity));
		coffeeMaker2000 = new Machine("coffeeMaker2000", FoodType.coffee, machineCapacity);
		Simulation.logEvent(SimulationEvent.machineStarting(coffeeMaker2000,  FoodType.coffee, machineCapacity));

		// Let cooks in
		Thread[] cooks = new Thread[numCooks];
		for(int i = 0; i < cooks.length; i++) {
			cooks[i] = new Thread(new Cook("Cook " + (i+1)));
		}
		for(int i = 0; i < cooks.length; i++) {
			cooks[i].start();
		}
		// Build the customers.
		Thread[] customers = new Thread[numCustomers];
		List<Food> order;
		Random rnd = new Random(27);
		if (!randomOrders) {
			order = new ArrayList<Food>();
			order.add(FoodType.burger);
			order.add(FoodType.fries);
			order.add(FoodType.fries);
			order.add(FoodType.coffee);
			for(int i = 0; i < customers.length; i++) {
				customers[i] = new Thread(
						new Customer("Customer " + (i+1), order)
						);
			}
		}
		else {
			for(int i = 0; i < customers.length; i++) {
				
				int burgerCount = rnd.nextInt(3);
				int friesCount = rnd.nextInt(3);
				int coffeeCount = rnd.nextInt(3);
				order = new ArrayList<Food>();
				for (int b = 0; b < burgerCount; b++) {
					order.add(FoodType.burger);
				}
				for (int f = 0; f < friesCount; f++) {
					order.add(FoodType.fries);
				}
				for (int c = 0; c < coffeeCount; c++) {
					order.add(FoodType.coffee);
				}
				customers[i] = new Thread(
						new Customer("Customer " + (i+1), order)
						);
			}
		}


		// Now "let the customers know the shop is open" by
		//    starting them running in their own thread.
		for(int i = 0; i < customers.length; i++) {
			customers[i].start();
			//NOTE: Starting the customer does NOT mean they get to go
			//      right into the shop.  There has to be a table for
			//      them.  The Customer class' run method has many jobs
			//      to do - one of these is waiting for an available
			//      table...
		
		}
		try {
			// Wait for customers to finish
			//   -- you need to add some code here...
			for(int i = 0; i < customers.length; i++) {
				customers[i].join();
			}
			// Then send cooks home...
			// The easiest way to do this might be the following, where
			// we interrupt their threads.  There are other approaches
			// though, so you can change this if you want to.
			for(int i = 0; i < cooks.length; i++)
				cooks[i].interrupt();
			for(int i = 0; i < cooks.length; i++)
				cooks[i].join();

		}
		catch(InterruptedException e) {
			System.out.println("Simulation thread interrupted.");
		}

		// Shut down machines
		Simulation.logEvent(SimulationEvent.machineEnding(Simulation.machineGrill));
		Simulation.logEvent(SimulationEvent.machineEnding(Simulation.fryer));
		Simulation.logEvent(SimulationEvent.machineEnding(Simulation.coffeeMaker2000));
		// Done with simulation		
		logEvent(SimulationEvent.endSimulation());

		return events;
	}

	/**
	 * Entry point for the simulation.
	 *
	 * @param args the command-line arguments for the simulation.  There
	 * should be exactly four arguments: the first is the number of customers,
	 * the second is the number of cooks, the third is the number of tables
	 * in the coffee shop, and the fourth is the number of items each cooking
	 * machine can make at the same time.  
	 */
	public static void main(String args[]) throws InterruptedException {
		// Parameters to the simulation
		/*
		if (args.length != 4) {
			System.err.println("usage: java Simulation <#customers> <#cooks> <#tables> <capacity> <randomorders");
			System.exit(1);
		}
		int numCustomers = new Integer(args[0]).intValue();
		int numCooks = new Integer(args[1]).intValue();
		int numTables = new Integer(args[2]).intValue();
		int machineCapacity = new Integer(args[3]).intValue();
		boolean randomOrders = new Boolean(args[4]);
		 */
		int numCustomers = 20;
		int numCooks =2;
		int numTables = 8;
		int machineCapacity = 5;
		boolean randomOrders = true;


		// Run the simulation and then 
		//   feed the result into the method to validate simulation.
		System.out.println("Did it work? " + 
				Validate.validateSimulation(
						runSimulation(numCustomers, numCooks, numTables, machineCapacity,randomOrders),
						numCustomers, 
						numCooks,
						numTables, 
						machineCapacity,
						randomOrders
						)
				);

	}

}



