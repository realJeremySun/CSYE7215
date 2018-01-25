//by XiaoCase
package counter;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import counter.Messages.StartMessage;

public class User {

	public static void main(String[] args) throws Exception {
		//this is your input folder dir
		String dir = "C:/Users/XiaoCase/Desktop/CSYE7215 Parallel & Multithreaded Prog/week11/hw9/Akka_Text";
		
		ActorSystem system = ActorSystem.create("EstimationSystem");
		Props mpProps = Props.create(WordCountActor.class);
		ActorRef user = system.actorOf(mpProps, "User");
		user.tell(new StartMessage(dir), null);
		Thread.sleep(2000);
		system.terminate();
	}

}
