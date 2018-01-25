package sunweb.A;

//import java.util.LinkedList;
import java.util.*;

/**
 * This class runs <code>numThreads</code> instances of
 * <code>ParallelMaximizerWorker</code> in parallel to find the maximum
 * <code>Integer</code> in a <code>LinkedList</code>.
 */
public class ParallelMaximizer {
	
	int numThreads;
	ArrayList<ParallelMaximizerWorker> workers; // = new ArrayList<ParallelMaximizerWorker>(numThreads);

	public ParallelMaximizer(int numThreads) {
		workers = new ArrayList<ParallelMaximizerWorker>();
		this.numThreads=numThreads;
	}


	
	public static void main(String[] args) {
		int numThreads = 4; // number of threads for the maximizer
		int numElements = 100; // number of integers in the list
		
		// populate the list
		// TODO: change this implementation to test accordingly
		
		//create a random linkedlist
		LinkedList<Integer> list = new LinkedList<Integer>();
		Random rand = new Random();
		for (int i=0; i<numElements; i++) {
			int next = rand.nextInt();
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
			ParallelMaximizer maximizer = new ParallelMaximizer(numThreads);
			try {
				System.out.println("The final MAX is "+maximizer.max(temp));
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
	 * @return Maximum element in the <code>LinkedList</code>
	 * @throws InterruptedException
	 */
	public int max(LinkedList<Integer> list) throws InterruptedException {
		int max = Integer.MIN_VALUE; // initialize max as lowest value
		
		
		// run numThreads instances of ParallelMaximizerWorker
		for (int i=0; i < numThreads; i++) {
			workers.add(new ParallelMaximizerWorker(list));
			workers.get(i).start();
		}
		System.out.println("There is "+workers.size()+" threads");
		// wait for threads to finish
		for (int i=0; i<workers.size(); i++)
			workers.get(i).join();
		
		// take the highest of the partial maximums
		// TODO: IMPLEMENT CODE HERE
		for (int i=0; i<workers.size(); i++) {
			System.out.print("Thread "+ i+" ");
			int temp = workers.get(i).getPartialMax();
			if(temp>max) max = temp;
		}
		return max;
	}
	
}
