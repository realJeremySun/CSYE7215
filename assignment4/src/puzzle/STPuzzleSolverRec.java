package puzzle;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * A single-threaded depth-first solver.
 */
public class STPuzzleSolverRec extends PuzzleSolver {
	private HashSet<Position> visited;
	
	public STPuzzleSolverRec(Puzzle puzzle) {
		super(puzzle);
	}

	public LinkedList<Direction> solve(Position p) {
		if (puzzle.getEnd().equals(p)) {
			return new LinkedList<Direction>();
		} else {
			visited.add(p);
			List<Direction> ds = puzzle.getMoves(p);
			for (Direction d : ds) {
				Position nextP = p.move(d);
				if (!visited.contains(nextP)) {
					LinkedList<Direction> sol = solve(nextP);
					if (sol != null) {
						sol.addFirst(d);
						return sol;
					}
				}
			}
			return null; // no solution
		}
	}
	
	public List<Direction> solve() {
		visited = new HashSet<Position>();
		return solve(puzzle.getStart());
	}
}
