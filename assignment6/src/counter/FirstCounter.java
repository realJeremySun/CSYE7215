//by XiaoCase
package counter;

import java.util.LinkedList;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import counter.Messages.InputMessage;
import counter.Messages.CountMessage;

public class FirstCounter extends UntypedActor {
	//reference of Estimator
	ActorRef es;
	//list of vowels
	LinkedList<String> vowels;
	
	public FirstCounter(ActorRef es) {
		this.es = es;
		vowels = new LinkedList<String>();
		vowels.add("A");
		vowels.add("E");
		vowels.add("I");
		vowels.add("O");
		vowels.add("U");
		vowels.add("Y");
		vowels.add("a");
		vowels.add("e");
		vowels.add("i");
		vowels.add("o");
		vowels.add("u");
		vowels.add("y");
	}

	@Override
	public void onReceive(Object msg) throws Throwable {
		if (msg instanceof InputMessage) {
			InputMessage input = (InputMessage) msg;
			LinkedList<String> inputlist = input.get();
			int count = 0;
			for(String line: inputlist) {
				for(int i = 0; i<line.length(); i++) {
					if (vowels.contains( String.valueOf(line.charAt(i)) ) ) count++;
				}
			}
//			System.out.println(input.toString()+" : "+ count);
			es.tell(new CountMessage(1,count,input.toString()), getSelf());
		}
		else {
			System.out.println("Unknow message");
			unhandled(msg);
		}
	}
}
