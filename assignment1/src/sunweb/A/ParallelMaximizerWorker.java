package sunweb.A;

import java.util.LinkedList;

/**
 * Given a <code>LinkedList</code>, this class will find the maximum over a
 * subset of its <code>Integers</code>.
 */
public class ParallelMaximizerWorker extends Thread {

	protected LinkedList<Integer> list;
	protected int partialMax = Integer.MIN_VALUE; // initialize to lowest value
	
	public ParallelMaximizerWorker(LinkedList<Integer> list) {
		this.list = list;
	}
	
	/**
	 * Update <code>partialMax</code> until the list is exhausted.
	 */
	public void run() {
		while (true) {
			int number;
			// check if list is not empty and removes the head
			// synchronization needed to avoid atomicity violation
			synchronized(list) {
				if (list.isEmpty())
					return; // list is empty
				number = list.remove();
			}
			
			// update partialMax according to new value
			// TODO: IMPLEMENT CODE HERE
			if(number>partialMax) partialMax=number;
			
			//let the thread sleep for 5 ms to make sure that all workers participate
			try {
				sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public int getPartialMax() {
		System.out.println("max is " +partialMax);
		return partialMax;
		
	}

}
