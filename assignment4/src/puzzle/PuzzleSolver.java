//immutable
package puzzle;

import java.util.List;

/**
 * Superclass of all solvers. Do not change!
 */
public abstract class PuzzleSolver
{
    protected Puzzle puzzle;

    public PuzzleSolver(Puzzle puzzle)
    {
        this.puzzle = puzzle;
    }

    /**
     * Solve the puzzle and return the solution. A solution is a list of
     * directions that lead from the puzzle start to the end. If no solution
     * exists, null should be returned.
     * 
     * @return The list of directions that would lead a person from the puzzle
     *         start to the end.
     */
    public abstract List<Direction> solve();
}
