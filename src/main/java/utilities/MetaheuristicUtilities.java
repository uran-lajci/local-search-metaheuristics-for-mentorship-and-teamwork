package utilities;

import entities.Assignment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MetaheuristicUtilities {
    public static List<Assignment> copySolution(List<Assignment> assignments) {
        return assignments.stream().map(Assignment::deepCopy).collect(Collectors.toList());
    }


    public static int deltaQuality(List<Assignment> oldSolution, List<Assignment> newSolution) {
        int divergenceIndex = 0;
        for (int i = 0; i < Math.min(oldSolution.size(), newSolution.size()); i++) {
            if (!oldSolution.get(i).equals(newSolution.get(i))) {
                divergenceIndex = i;
                break;
            }
        }

        List<Assignment> changedAssignmentsInNewSolution = new ArrayList<>(newSolution.subList(divergenceIndex, newSolution.size()));
        List<Assignment> changedAssignmentsInOldSolution = oldSolution.size() > divergenceIndex ? oldSolution.subList(divergenceIndex, oldSolution.size()) : new ArrayList<>();

        return quality(changedAssignmentsInNewSolution) - quality(changedAssignmentsInOldSolution);
    }


    public static int quality(List<Assignment> assignments) {
        return FitnessCalculator.getFitnessScore(assignments);
    }
}
