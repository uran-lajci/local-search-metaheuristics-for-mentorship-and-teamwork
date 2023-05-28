package algorithms;

import entities.Assignment;
import entities.Contributor;
import entities.Project;
import utilities.MetaheuristicUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SimulatedAnnealing {

    public static List<Assignment> performSearch(List<Assignment> initialSolution, int maxMinutes, List<Project> projects, List<Contributor> contributors) {
        List<Assignment> currentSolution = new ArrayList<>(initialSolution);
        List<Assignment> bestSolution = new ArrayList<>(currentSolution);

        long startTime = System.currentTimeMillis();
        long maxMillis = TimeUnit.MINUTES.toMillis(maxMinutes);
        double temperature = maxMinutes;
        double coolingRate = 0.99;

        while (System.currentTimeMillis() - startTime < maxMillis && temperature > 1.0) {
            List<Assignment> tweakedSolution = NeighborhoodOperators.tweak(MetaheuristicUtilities.copySolution(currentSolution), projects, contributors);
            int deltaQuality = MetaheuristicUtilities.deltaQuality(currentSolution, tweakedSolution);

            if (deltaQuality > 0 || shouldAcceptWorseSolution(deltaQuality, temperature)) {
                currentSolution = new ArrayList<>(tweakedSolution);
            }

            if (deltaQuality > 0) {
                bestSolution = new ArrayList<>(currentSolution);
            }

            temperature *= coolingRate;
        }

        return bestSolution;
    }

    private static boolean shouldAcceptWorseSolution(int deltaQuality, double temperature) {
        double acceptanceProbability = Math.exp(deltaQuality / temperature);
        double randomValue = Math.random();
        return randomValue < acceptanceProbability;
    }

}