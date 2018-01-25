//by XiaoCase
package counter;

import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import counter.Messages.StartMessage;
import counter.Messages.InputMessage;
import counter.Messages.ResultMessage;
import counter.Messages.AdjustMessage;


public class WordCountActor extends UntypedActor {
	
	@Override
	public void onReceive(Object msg) throws Throwable {
		if (msg instanceof StartMessage) {
			StartMessage start = (StartMessage) msg;
			ActorContext context = getContext();
			
			//create Estimator
			Props esProps = Props.create(Estimator.class, getSelf());
			ActorRef estimator = context.actorOf(esProps, "Estimator");
			//create two counters
			Props fcProps = Props.create(FirstCounter.class, estimator);
			Props scProps = Props.create(SecoundCounter.class, estimator);
			ActorRef firstcounter = context.actorOf(fcProps, "FirstCounter");
			ActorRef secoundcounter = context.actorOf(scProps, "SecoundCounter");
			
			//pre-process data
			//read the file
			File folder = new File(start.get());
			for (File file : folder.listFiles()) {
			    if (file.isFile()) {
			    	Scanner  scanner = new Scanner(file);
			    	LinkedList<String> all = new LinkedList<String>();
			    	while(scanner.hasNextLine()){
			    		 all.add(scanner.nextLine());       
			    	}
			    	scanner.close();
			    	LinkedList<String> firsthalf = new LinkedList<String>();
					LinkedList<String> secondhalf = new LinkedList<String>();
					int half = all.size()/2;
					for(int i = 0; i < half; i++) {
						firsthalf.add(all.get(i));
					}
					for(int i = half; i < all.size(); i++) {
						secondhalf.add(all.get(i));
					}
					
					//sent data to counter
					firstcounter.tell(new InputMessage(firsthalf, file.getName()), getSelf());
					secoundcounter.tell(new InputMessage(secondhalf, file.getName()), getSelf());
					
			    }
			}
		}
		else if(msg instanceof ResultMessage) {
			ResultMessage resultmessage = (ResultMessage) msg;
			int result = resultmessage.getResult();
			int estimate = resultmessage.getEstimate();
			boolean bigger = (estimate > result);
			System.out.println(resultmessage.toString()+" Result:"  + result +" Estimate: " +estimate);
			getSender().tell(new AdjustMessage(bigger), getSelf());
		}
		else {
			System.out.println("Unknow message");
			unhandled(msg);
		}
	}

}
