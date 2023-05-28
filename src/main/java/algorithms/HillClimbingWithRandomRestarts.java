package algorithms;

import entities.Assignment;
import entities.Contributor;
import entities.Project;
import utilities.MetaheuristicUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HillClimbingWithRandomRestarts {
    private static final int SECONDS_IN_MINUTE = 60;

    public static List<Assignment> performSearch(List<Assignment> initialSolution, int maxMinutes, List<Project> projects, List<Contributor> contributors) {
        List<Assignment> currentSolution = new ArrayList<>(initialSolution);
        List<Assignment> bestSolution = new ArrayList<>(currentSolution);

        long startTime = System.currentTimeMillis();
        long maxMillis = TimeUnit.MINUTES.toMillis(maxMinutes);

        while (System.currentTimeMillis() - startTime < maxMillis) {
            int innerIteration = 0;
            while (System.currentTimeMillis() - startTime < maxMillis && innerIteration < maxMinutes * SECONDS_IN_MINUTE) {
                List<Assignment> tweakedSolution = NeighborhoodOperators.tweak(MetaheuristicUtilities.copySolution(currentSolution), projects, contributors);
                if (MetaheuristicUtilities.deltaQuality(currentSolution, tweakedSolution) > 0) {
                    currentSolution = new ArrayList<>(tweakedSolution);
                }
                innerIteration++;
            }

            if (MetaheuristicUtilities.deltaQuality(bestSolution, currentSolution) > 0) {
                bestSolution = new ArrayList<>(currentSolution);
            }
        }

        return bestSolution;
    }
}
