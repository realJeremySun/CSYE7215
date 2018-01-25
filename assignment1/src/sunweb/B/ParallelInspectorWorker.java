package sunweb.B;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Given a <code>LinkedList</code>, this class will find the maximum over a
 * subset of its <code>Integers</code>.
 */
public class ParallelInspectorWorker extends Thread {

	protected LinkedList<Integer> list;
	ArrayList<Integer> collect;
	int inspector;
	
	public ParallelInspectorWorker(LinkedList<Integer> list,int inspector) {
		this.list = list;
		this.inspector=inspector;
		collect = new ArrayList<Integer>();
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
			
			//in different case, use different inspector
			switch(inspector) {
			case 0: even(number); break;
			case 1: odd(number); break;
			case 2: order(number); break;
			case 3: jack(number); break;
			}
			
			//let the thread sleep for 5 ms to make sure that all workers participate
			try {
				sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//if the num is even, add it to collection
	public void even(int number) {
		if(number%2==0) collect.add(number);
	}
	//if the num is odd, add it to collection
	public void odd(int number) {
		if(number%2!=0) collect.add(number);
	}
	//if the num is bigger than last one, add it to collection
	public void order(int number) {
		if(!collect.isEmpty()) {
			if(number>collect.get(collect.size()-1)) collect.add(number);
		}
		else collect.add(number);
	}
	//if the sum not equal to 21, add num to collection
	public void jack(int number) {
		int sum=0;
		for(int a : collect) {
			sum+=a;
		}
		if(sum!=21) collect.add(number);
	}

}
