package puzzle;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;

/**
 * Main entry point for the program. Provides command-line support for
 * generating random puzzles and passing puzzle files to solvers.
 */
public class Main
{
    private Puzzle puzzle;
    private boolean solvable;

    /**
     * Method that calls the solvers. To add your solver to the list of solvers
     * that is run, uncomment it in the "solvers" array defined at the top of this
     * method.
     */
    public void solve()
    {
        // Add your solvers to this array to test them.
        PuzzleSolver[] solvers =
        {
        	new STPuzzleSolverRec(puzzle),
        	new STPuzzleSolverDFS(puzzle),
        	new STPuzzleSolverBFS(puzzle),
            new StudentMultiPuzzleSolver(puzzle),  //uncomment this line when you are ready to test yours
        };

        for (PuzzleSolver solver : solvers)
        {
            long startTime, endTime;
            float sec;

            System.out.println();
            System.out.println(className(solver.getClass()) + ":");

            startTime = System.currentTimeMillis();
            List<Direction> soln = solver.solve();
            endTime = System.currentTimeMillis();
            sec = (endTime - startTime) / 1000F;
            
            
            if (soln == null)
            {
                if (!solvable) System.out.println("Correctly found no solution in " + sec + " seconds.");
                else System.out.println("Incorrectly returned no solution when there is one.");
            }
            else
            {
                if (puzzle.checkSolution(soln)) System.out.println("Correct solution found in " + sec + " seconds.");
                else System.out.println("Incorrect solution found.");
            }
        }
    }

    public static void main(String[] args)
    {
        Main m = new Main();

        String puzzleLocationNotInProjectFolder = "maze//"; //replace this with your puzzle directory
        String whichPuzzleToUse = "500x500.mz"; //which puzzle file to load
        String[] replaceArgs = {puzzleLocationNotInProjectFolder+whichPuzzleToUse};
        args = replaceArgs;
        
        //You probably shouldn't change the lines below
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("File " + file.getAbsolutePath() + " does not exist.");
            System.exit(-2);
        }
        try {
            m.read(args[0]);
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException while reading puzzle from: " + args[0]);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException while reading puzzle from: " + args[0]);
            e.printStackTrace();
        }
        
        // Uncomment to use puzzle display
        //m.initDisplay();
        
        m.solve();
    }
    

    @SuppressWarnings("unchecked")
    private void read(String filename) throws IOException, ClassNotFoundException
    {
        PuzzleInputStream in =
                new PuzzleInputStream(new BufferedInputStream(new FileInputStream(filename)));
        puzzle = (Puzzle) in.readObject();
        solvable = in.readBoolean();
        in.close();
    }

    private String className(Class<?> cl)
    {
        StringBuffer fullname = new StringBuffer(cl.getName());
        String name = fullname.substring(fullname.lastIndexOf(".") + 1);
        return name;
    }
    
    
    private void initDisplay()
    {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int puzzle_width = puzzle.getWidth();
        int puzzle_height = puzzle.getHeight() + 2;
        int cell_width = (dim.width / puzzle_width);
        int cell_height = (dim.height / puzzle_height);
        int cell_size = Math.min(cell_width, cell_height);

        if (cell_size >= 2)
        {
            JFrame frame = new JFrame("Puzzle Solver");
            PuzzleDisplay display = new PuzzleDisplay(puzzle);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            puzzle.display = display;
            frame.setSize(puzzle_width * cell_size, puzzle_height * cell_size);
            frame.setVisible(true);
            Insets insets = frame.getInsets();
            frame.setSize(puzzle_width * cell_size + insets.left + insets.right + 3,
                    puzzle_height * cell_size + insets.top + insets.bottom + 2);
            System.out.println(frame.getSize());
            frame.getContentPane().add(display);
        }
        else
        {
            System.out.println("Puzzle too large to display on-screen.");
        }
    }
}
