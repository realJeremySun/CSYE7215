package puzzle;

import java.util.LinkedList;
import java.util.List;

/**
 * A single-threaded breadth-first solver.
 */
public class STPuzzleSolverBFS extends SkippingPuzzleSolver
{
    public class SolutionNode
    {
        public SolutionNode parent;
        public Choice choice;

        public SolutionNode(SolutionNode parent, Choice choice)
        {
            this.parent = parent;
            this.choice = choice;
        }
    }

    public STPuzzleSolverBFS(Puzzle puzzle)
    {
        super(puzzle);
    }

    Direction exploring = null;

    /**
     * Expands a node in the search tree, returning the list of child nodes.
     * 
     * @throws SolutionFound
     */
    public List<SolutionNode> expand(SolutionNode node) throws SolutionFound
    {
        LinkedList<SolutionNode> result = new LinkedList<SolutionNode>();
        if (puzzle.display != null) puzzle.setColor(node.choice.at, 0);
        for (Direction dir : node.choice.choices)
        {
            exploring = dir;
            Choice newChoice = follow(node.choice.at, dir);
            if (puzzle.display != null) puzzle.setColor(newChoice.at, 2);
            result.add(new SolutionNode(node, newChoice));
        }
        return result;
    }

    /**
     * Performs a breadth-first search of the puzzle. The algorithm builds a tree
     * rooted at the start position. Parent pointers are used to point the way
     * back to the entrance. The algorithm stores the list of leaves in the
     * variables "frontier". During each iteration, these leaves are each
     * expanded and the children the result become the new frontier. If a node
     * represents a dead-end, it is discarded. Execution stops when the exit is
     * discovered, as indicated by the SolutionFound exception.
     */
    public List<Direction> solve()
    {
        SolutionNode curr = null;
        LinkedList<SolutionNode> frontier = new LinkedList<SolutionNode>();

        try
        {
            frontier.push(new SolutionNode(null, firstChoice(puzzle.getStart())));
            while (!frontier.isEmpty())
            {
                LinkedList<SolutionNode> new_frontier = new LinkedList<SolutionNode>();
                for (SolutionNode node : frontier)
                {
                    if (!node.choice.isDeadend())
                    {
                        curr = node;
                        new_frontier.addAll(expand(node));
                    }
                    else if (puzzle.display != null)
                    {
                        puzzle.setColor(node.choice.at, 0);
                    }
                }
                frontier = new_frontier;
                if (puzzle.display != null)
                {
                    puzzle.display.updateDisplay();
                    try
                    {
                        Thread.sleep(50);
                    }
                    catch (InterruptedException e)
                    {
                    }
                    // Could use: puzzle.display.waitForMouse();
                    // if we wanted to pause until a mouse button was pressed.
                }
            }
            // No solution found.
            return null;
        }
        catch (SolutionFound e)
        {
            if (curr == null)
            {
                // this only happens if there was a direct path from the start
                // to the end
                return pathToFullPath(puzzle.getMoves(puzzle.getStart()));
            }
            else
            {
                LinkedList<Direction> soln = new LinkedList<Direction>();
                // First save the direction we were going in when the exit was
                // discovered.
                soln.addFirst(exploring);
                while (curr != null)
                {
                    try
                    {
                        Choice walkBack = followMark(curr.choice.at, curr.choice.from, 1);
                        if (puzzle.display != null)
                        {
                            puzzle.display.updateDisplay();
                        }
                        soln.addFirst(walkBack.from);
                        curr = curr.parent;
                    }
                    catch (SolutionFound e2)
                    {
                        // If there is a choice point at the puzzle entrance, then
                        // record
                        // which direction we should choose.
                        if (puzzle.getMoves(puzzle.getStart()).size() > 1) soln.addFirst(e2.from);
                        if (puzzle.display != null)
                        {
                            markPath(soln, 1);
                            puzzle.display.updateDisplay();
                        }
                        return pathToFullPath(soln);
                    }
                }
                markPath(soln, 1);
                return pathToFullPath(soln);
            }
        }
    }
}
