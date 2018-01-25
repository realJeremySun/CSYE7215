package sunweb.B;

//import java.util.LinkedList;
import java.util.*;

/**
 * This class runs <code>numThreads</code> instances of
 * <code>ParallelMaximizerWorker</code> in parallel to find the maximum
 * <code>Integer</code> in a <code>LinkedList</code>.
 */
public class ParallelInspector {
	
	int numThreads;
	ArrayList<ParallelInspectorWorker> workers; // = new ArrayList<ParallelMaximizerWorker>(numThreads);

	public ParallelInspector(int numThreads) {
		workers = new ArrayList<ParallelInspectorWorker>();
		this.numThreads=numThreads;
	}


	
	public static void main(String[] args) {
		int numThreads = 4; // number of threads for the maximizer
		int numElements = 1000; // number of integers in the list
		
		// populate the list
		// TODO: change this implementation to test accordingly
		
		//create a random linkedlist
		LinkedList<Integer> list = new LinkedList<Integer>();
		Random rand = new Random();
		for (int i=0; i<numElements; i++) {
			//let those int less than 100, so that Jack is easier to success
			int next = rand.nextInt(100);
			if(rand.nextBoolean()) next = -next;
			list.add(next);
		}

		//test 10 times
		for(int i = 0; i<10;i++) {
			System.out.println("test : "+i);
			//create a new temp linedlist, copy list to temp
			LinkedList<Integer> temp = new LinkedList<Integer>();
			for(int a : list) {
				temp.add(a);
			}
			
			//use the temp
			// run the maximizer
			ParallelInspector maximizer = new ParallelInspector(numThreads);
			try {
				maximizer.inspect(temp);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Finds the maximum by using <code>numThreads</code> instances of
	 * <code>ParallelMaximizerWorker</code> to find partial maximums and then
	 * combining the results.
	 * @param list <code>LinkedList</code> containing <code>Integers</code>
	 * @throws InterruptedException
	 */
	public void inspect(LinkedList<Integer> list) throws InterruptedException {
		// run numThreads instances of ParallelInspectorWorker
		for (int i=0; i < numThreads; i++) {
			workers.add(new ParallelInspectorWorker(list,i));
			workers.get(i).start();
		}
		System.out.println("There is "+workers.size()+" threads");
		
		// wait for threads to finish
		for (int i=0; i<workers.size(); i++)
			workers.get(i).join();
		
		//print the result of each inspector
		for (int i=0; i<workers.size(); i++) {
			switch(i) {
			case 0: System.out.print("Even: ");
					if(workers.get(i).collect.isEmpty()) {
						System.out.println("Task filed. No Odd number.");
						break;
					}
					for(int a: workers.get(i).collect) {
						System.out.print(a+" ");
					}
					System.out.println(" ");
					break;
					
			case 1: System.out.print("Odd: ");
					if(workers.get(i).collect.isEmpty()) {
						System.out.println("Task filed. No Odd number.");
						break;
					}
					for(int a: workers.get(i).collect) {
						System.out.print(a+" ");
					}
					System.out.println(" ");
					break;
					
			case 2: System.out.print("Order: ");
					for(int a: workers.get(i).collect) {
						System.out.print(a+" ");
					}
					System.out.println(" ");
					break;
					
			case 3: System.out.print("Jack: ");
					int sum=0;
					for(int a: workers.get(i).collect) {
						sum+=a;
						System.out.print(a+" ");
					}
					System.out.println(" ");
					if(sum==21) System.out.println("Jack Task success! The sum equal to 21");
					else System.out.println("Jack Task Failed! The linkedlist is empty");
					System.out.println(" ");
					break;
			}
		}
	}
	
}
