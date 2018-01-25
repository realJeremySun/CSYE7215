//By-XiaoCase
package puzzle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicIntegerArray;


/**
 * This file needs to hold your solver to be tested. 
 * You can alter the class to extend any class that extends PuzzleSolver.
 * It must have a constructor that takes in a Puzzle.
 * It must have a solve() method that returns the datatype List<Direction>
 *   which will either be a reference to a list of steps to take or will
 *   be null if the puzzle cannot be solved.
 */
public class StudentMultiPuzzleSolver extends PuzzleSolver
{
	ArrayList<LinkedList<Direction>> resultlist;
	Puzzle temp;
	int core;
	ExecutorService exec;
	List<Future<List<Direction>>> list;
	
	public class SolutionFoundBy extends Exception
    {
		public Position pos;
	    public Direction from;
	
        public SolutionFoundBy(Position pos, Direction from)
        {
        	this.pos = pos;
	        this.from = from;
	    }
    }
	
    public StudentMultiPuzzleSolver(Puzzle puzzle)
    {
        super(puzzle);
        resultlist= new ArrayList<LinkedList<Direction>>();
        temp = new Puzzle();
		temp.height=puzzle.height;
		temp.width=puzzle.width;
		temp.puzzle=new AtomicIntegerArray(puzzle.width * puzzle.height);
		for(int i = 0; i<puzzle.width * puzzle.height; i++) {
			temp.puzzle.set(i, puzzle.puzzle.get(i));
		}
		core = Math.min(Runtime.getRuntime().availableProcessors(),1);
    }  
 
    private class DFStask implements Callable<List<Direction>>{
    	private LinkedList<Choice> choiceStack;
    	List<Direction> result;

    	public DFStask() {
    		result = new LinkedList<Direction>();
    		choiceStack = new LinkedList<Choice>();
    	}

    	public Choice firstChoice(Position pos) throws SolutionFoundBy
	    {
	        LinkedList<Direction> moves;

	        moves = temp.getMoves(pos);
	        if (moves.size() == 1) {
	        	return follow(pos, moves.getFirst());
	        }
	        else return new Choice(pos, null, moves);
	    }    	

        public Choice follow(Position at, Direction dir) throws SolutionFoundBy
        {

            LinkedList<Direction> choices;
            Direction go_to = dir, 
            came_from = dir.reverse();

            at = at.move(go_to);
            do
            {
                if (at.equals(puzzle.getEnd())) throw new SolutionFoundBy(at, go_to.reverse());      
                if (at.equals(puzzle.getStart())) throw new SolutionFoundBy(at, go_to.reverse());
                choices = temp.getMoves(at);
                choices.remove(came_from);

                if (choices.size() == 1)
                {

                    go_to = choices.getFirst();
           
                    at = at.move(go_to);
                    came_from = go_to.reverse();
                }
            } while (choices.size() == 1);

            // return new Choice(at,choices);
            Choice ret = new Choice(at, came_from, choices);
            return ret;
        }
      
        public LinkedList<Direction> followDir(Position from, Position to, Direction dir)
        {
        	LinkedList<Direction> result = new LinkedList<Direction>();
            LinkedList<Direction> choices;
            Direction go_to = dir, came_from = dir.reverse();

            result.add(go_to);
            from = from.move(go_to);
            while(!from.equals(to))
            {
                choices = temp.getMoves(from);
                choices.remove(came_from);

                if (choices.size() == 1)
                {
                    go_to = choices.getFirst();
                    result.add(go_to);
                    from = from.move(go_to);
                    came_from = go_to.reverse();
                }
            } 
            return  result;
        }
        
		@Override
		public List<Direction> call() {
			Choice ch;
	        
	        try
	        {
	            choiceStack.push(firstChoice(temp.getStart()));
	            while (!choiceStack.isEmpty())
	            {
	                ch = choiceStack.peek();
	                if (ch.isDeadend())
	                {
	                    // backtrack.
	                	choiceStack.pop();
	                    if (!choiceStack.isEmpty()) {
	                    	choiceStack.peek().choices.pop();
//	                    	switch(choiceStack.peek().choices.pop()) {
//		        				case NORTH:
//		        					temp.setSouth(choiceStack.peek().at.move(Direction.NORTH));
//		        					break;
//		        	            case SOUTH:
//		        	            	temp.setSouth(choiceStack.peek().at);
//		        	            	break;
//		        	            case EAST:
//		        	            	temp.setEast(choiceStack.peek().at);
//		        	            	break;
//		        	            case WEST:
//		        	            	temp.setEast(choiceStack.peek().at.move(Direction.WEST));
//		        	            	break;
//	                    	}
	                    }
	                    continue;
	                }
	                choiceStack.push(follow(ch.at, ch.choices.peek()));     
	            }
	            Thread.currentThread().interrupt();
	        }
	        catch (SolutionFoundBy e)
	        {   	
            	Iterator<Choice> iter = choiceStack.iterator();
                LinkedList<Direction> solutionPath = new LinkedList<Direction>();
                LinkedList<Choice> reverseStack = new LinkedList<Choice>();
                while (iter.hasNext())
                {
                	ch = iter.next();
                    solutionPath.push(ch.choices.peek());
                    reverseStack.push(ch);
                }
 
                if(!reverseStack.getFirst().at.equals(puzzle.getStart())) {
                	result.addAll( followDir( puzzle.getStart(),reverseStack.getFirst().at, temp.getMoves(puzzle.getStart()).getFirst() ) );
                }
                for(int i = 0; i < reverseStack.size()-1; i++) {
                	result.addAll(followDir(reverseStack.get(i).at,reverseStack.get(i+1).at, solutionPath.get(i)));
                }
                result.addAll(followDir( reverseStack.get(reverseStack.size()-1).at, temp.getEnd(), solutionPath.get(solutionPath.size()-1) ));
    			return result;
	        	}
	        return result;
		} 	
    }
    
    public void readyFor() {
		exec = Executors.newFixedThreadPool(8);
		list = new ArrayList<Future<List<Direction>>>();
    	for(int i = 0; i < core; i++) {
    		list.add( exec.submit(new DFStask()) );
    	}
    }
    
    public List<Direction> solve()
    {
    	readyFor();
    	for(int i = 0; i < core; i++) {
    		try {
    			resultlist.add((LinkedList<Direction>) list.get(i).get());	
			} catch (InterruptedException | ExecutionException r) {
				exec.shutdownNow();
			}
    	}
 
    	for(LinkedList<Direction> r: resultlist) {
    		if(puzzle.checkSolution((r))){
    			exec.shutdown();
    			return r;
    		}
    	}
		return null;
    }
}
