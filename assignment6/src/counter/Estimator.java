//by XiaoCase
package counter;

import java.util.HashMap;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import counter.Messages.CountMessage;
import counter.Messages.ResultMessage;
import counter.Messages.AdjustMessage;

public class Estimator extends UntypedActor {
	//coefficient p1, initialized to 1
	private double p1 = 1;
	//coefficient p2, when estimate result is higher
	private final double p2 = 0.9;
	//coefficient p3, when estimate result is lower
	private final double p3 = 1.1;
	//reference of WordCountActor
	private ActorRef wordcountactor;
	//String = file name, int[0] = result, int[1] = estimate
	private HashMap<String, int[]> result;
	boolean firsttime = true;
	
	public Estimator(ActorRef wordcountactor) {
		result = new HashMap<String, int[]>();
		this.wordcountactor = wordcountactor;
	}
	
	@Override
	public void onReceive(Object msg) throws Throwable {
		if (msg instanceof CountMessage) {
			CountMessage countmessage = (CountMessage) msg;
			int count = countmessage.getCount();
			String filename =countmessage.toString();
			if(!result.containsKey(countmessage.toString())) {
				if(countmessage.getID()==1) {
					if(firsttime) {
						result.put(filename, new int[] {count, -1, (int) (count*p1*2)});
						firsttime = false;
					}
					else {
						result.put(filename, new int[] {count, -1, -1});
					}
				}
				else {
					result.put(filename.toString(), new int[] {-1, count, -1});
				}
			}
			else {
				if(countmessage.getID()==1){
					if(firsttime) {
						result.put(filename, new int[] {count, result.get(filename)[1], (int) (count*p1*2)});
						wordcountactor.tell(new ResultMessage(result.get(filename)[0]+result.get(filename)[1],result.get(filename)[2],filename), getSelf());
						result.remove(filename);
						firsttime = false;
					}
					else {
						result.put(filename, new int[] {count, result.get(filename)[1], -1});
					}
				}
				else {
					if(firsttime) {
						result.put(filename, new int[] {result.get(filename)[0], count, result.get(filename)[2]});
						wordcountactor.tell(new ResultMessage(result.get(filename)[0]+result.get(filename)[1],result.get(filename)[2],filename), getSelf());
						result.remove(filename);
						firsttime = false;
					}
					else {
						result.put(filename, new int[] {result.get(filename)[0], count, result.get(filename)[2]});
						if(result.get(filename)[2] != -1) {
							wordcountactor.tell(new ResultMessage(result.get(filename)[0]+result.get(filename)[1],result.get(filename)[2],filename), getSelf());
							result.remove(filename);
						}
					}
				}
			}
		}
		else if (msg instanceof AdjustMessage) {
			AdjustMessage adjustmessage = (AdjustMessage) msg;
			boolean bigger = adjustmessage.get();
			if(bigger) p1 = p1*p2;
			else p1 = p1*p3;
			for(String filename : result.keySet()) {
				wordcountactor.tell(new ResultMessage(result.get(filename)[0]+result.get(filename)[1],(int)(result.get(filename)[0]*p1*2),filename), getSelf());
				result.remove(filename);
				break;
			}
//			System.out.println(p1);
			Thread.sleep(100);
		}
		else {
			System.out.println("Unknow message");
			unhandled(msg);
		}
	}

}
