package puzzle;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An efficient single-threaded depth-first solver.
 */
public class STPuzzleSolverDFS extends SkippingPuzzleSolver
{
    public STPuzzleSolverDFS(Puzzle puzzle)
    {
        super(puzzle);
    }

    /**
     * Performs a depth-first search for the exit. The solver operates by
     * maintaining a stack of choices. During each iteration, the choice at the
     * top of the stack is examined. If choice.isEmpty() is true, then we have
     * reached a dead-end and must backtrack by popping the stack. If the choice
     * is not empty, then we proceed down the first path in the list of options.
     * If the exit is encountered, then SolutionFound is thrown and we generate
     * the solution path, which we return. At any given point in the execution,
     * the list of first choices yields the current path. That is, if the choice
     * stack is:
     * 
     * <pre>
     * [[E W S] [E W] [S N] [N]]
     * </pre>
     * 
     * Then the current path is given by the list:
     * 
     * <pre>
     * [E E S N]
     * </pre>
     */
    public List<Direction> solve()
    {
        LinkedList<Choice> choiceStack = new LinkedList<Choice>();
        Choice ch;

        try
        {
            choiceStack.push(firstChoice(puzzle.getStart()));
           // if (puzzle.display != null) puzzle.setColor(choiceStack.peek().at, 2);
            while (!choiceStack.isEmpty())
            {
            	
                ch = choiceStack.peek();
                if (ch.isDeadend())
                {
                    // backtrack.
                    choiceStack.pop();
                    if (!choiceStack.isEmpty()) choiceStack.peek().choices.pop();
                    continue;
                }
                choiceStack.push(follow(ch.at, ch.choices.peek()));
                if (puzzle.display != null) { 
                	puzzle.setColor(choiceStack.peek().at, 2);
                
	                puzzle.display.updateDisplay();
	                try
	                {
	                    Thread.sleep(5);
	                }
	                catch (InterruptedException e)
	                {
	                	
	                }
                }
            }
            // No solution found.
            return null;
        }
        catch (SolutionFound e)
        {
//        	for(Choice a: choiceStack) {
//        		System.out.print("["+a.at.col+","+a.at.row+"] ");
//        	}
//        	System.out.println(" ");
//        	System.out.println(choiceStack.size()+" ");
            Iterator<Choice> iter = choiceStack.iterator();
            LinkedList<Direction> solutionPath = new LinkedList<Direction>();
            while (iter.hasNext())
            {
            	ch = iter.next();
                solutionPath.push(ch.choices.peek());
            }

            if (puzzle.display != null) puzzle.display.updateDisplay();
//            System.out.println(solutionPath.size());
            List<Direction> result = pathToFullPath(solutionPath);
//            System.out.println("result"+result);
//            System.out.println("result"+result.size());
            return result;
        }
    }
}
