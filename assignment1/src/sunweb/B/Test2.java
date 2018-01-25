package sunweb.B;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import org.junit.Test;

public class Test2 {
	int numThreads = 4;
	ParallelInspector inspector = new ParallelInspector(numThreads);
	
	//test if all num in the list is even
    public boolean isEvenNumber(ArrayList<Integer> list){
        for(int a: list) {
	        if(a%2 != 0){
	            return false;
	        }
        }
        return true;
    }
    
    //test order
    public boolean isOrder(ArrayList<Integer> list) {
    	int size = list.size();
    	for(int i = 1; i<size; i++) {
    		if(list.get(i-1)>list.get(i)) return false;
    	}
		return true;	
    }
    
    //test jack
    public boolean isJack(ArrayList<Integer> list) {
    	int size = list.size();
    	for(int i = 0; i<size-1; i++) {
    		int sum = 0;
    		for(int j = 0 ; j<i;j++) {
    			sum+=list.get(j);
    		}
    		if(sum==21) return false;
    	}
    	return true;
    }
    
    @Test
    public void testAll(){
		int numThreads = 4; 
		int numElements = 1000; 
		//create a random linkedlist
		LinkedList<Integer> list = new LinkedList<Integer>();
		Random rand = new Random();
		for (int i=0; i<numElements; i++) {
			//let those int less than 100, so that Jack is easier to success
			int next = rand.nextInt(100);
			if(rand.nextBoolean()) next = -next;
			list.add(next);
		}
		
		// run those inspectors
		try {
			inspector.inspect(list);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("The test failed because the max procedure was interrupted unexpectedly.");
		} catch (Exception e) {
			e.printStackTrace();
			fail("The test failed because the max procedure encountered a runtime error: " + e.getMessage());
		}
		
		//test each collection
		for (int i=0; i<numThreads; i++) {
			switch(i) {
			//test number in even
			case 0: assertEquals("pass", true, isEvenNumber(inspector.workers.get(i).collect));
					break;
			//test number in odd		
			case 1: assertEquals("pass", false, isEvenNumber(inspector.workers.get(i).collect));
					break;
			//test number in odd		
			case 2: assertEquals("pass", true, isOrder(inspector.workers.get(i).collect));
					break;		
			//test number in Jack		
			case 3: assertEquals("pass", true, isJack(inspector.workers.get(i).collect));
					break;
			}
		}
    }
}