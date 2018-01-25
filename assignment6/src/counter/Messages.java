//by XiaoCase
package counter;

import java.util.LinkedList;

public class Messages {
	
	public static class StartMessage{
		final private String stmessage;
		public StartMessage(String stmessage) {
			this.stmessage = stmessage;
		}
		public String get() {
			return stmessage;
		}
	}

	public static class InputMessage{
		final private LinkedList<String> data;
		final private String file;
		public InputMessage(LinkedList<String> data, String file) {
			this.data = data;
			this.file = file;
		}
		public LinkedList<String> get() {
			return data;
		}
		public String toString() {
			return file;
		}
	}
	
	public static class CountMessage{
		final private int counterID;
		final private int count;
		final private String file;
		public CountMessage(int counterID, int count, String file) {
			this.counterID = counterID;
			this.count = count;
			this.file = file;
		}
		public int getCount() {
			return count;
		}
		public int getID() {
			return counterID;
		}
		public String toString() {
			return file;
		}
	}
	
	public static class ResultMessage{
		final private int result;
		final private int estimate;
		final private String file;
		public ResultMessage(int result, int estimate, String file) {
			this.result = result;
			this.estimate = estimate;
			this.file = file;
		}
		public int getResult() {
			return result;
		}
		public int getEstimate() {
			return estimate;
		}
		public String toString() {
			return file;
		}
	}

	public static class AdjustMessage{
		final private boolean bigger;
		public AdjustMessage(boolean bigger) {
			this.bigger = bigger;
		}
		public boolean get() {
			return bigger;
		}
	}
	
	
}