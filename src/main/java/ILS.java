import entities.Contributor;
import entities.FullAssignment;
import entities.Project;
import utilities.FitnessCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ILS {

    public static List<FullAssignment> iteratedLocalSearchWithRandomRestarts(List<FullAssignment> initialSolution, int maxMinutes, List<Project> projects, List<Contributor> contributors) {

        List<FullAssignment> S = new ArrayList<>(initialSolution);
        List<FullAssignment> H = new ArrayList<>(S);
        List<FullAssignment> Best = new ArrayList<>(S);

        long startTime = System.currentTimeMillis();
        long maxMillis = TimeUnit.MINUTES.toMillis(maxMinutes);

        while (System.currentTimeMillis() - startTime < maxMillis) {
            int innerIteration = 0;
            while (System.currentTimeMillis() - startTime < maxMillis && innerIteration < maxMinutes * 60) {

                List<FullAssignment> R = Tweak(Copy(S), projects, contributors);

                int delta = deltaQuality(S, R);
                if (delta > 0) {
                    S = new ArrayList<>(R);
                }

                innerIteration++;
            }

            int delta = deltaQuality(Best, S);

            if (delta > 0) {
                Best = new ArrayList<>(S);
            }

            H = NewHomeBase(H, S);
            S = Perturb(H);
        }

        return Best;
    }

    private static int deltaQuality(List<FullAssignment> oldSolution, List<FullAssignment> newSolution) {
        return Quality(newSolution) - Quality(oldSolution);
    }

    private static List<FullAssignment> Copy(List<FullAssignment> S) {
        return new ArrayList<>(S);
//        return S.stream().map(FullAssignment::deepCopy).collect(Collectors.toList());
    }

    private static int Quality(List<FullAssignment> R) {
        return FitnessCalculator.getFitnessScore(R);
    }

    private static List<FullAssignment> NewHomeBase(List<FullAssignment> H, List<FullAssignment> S) {
        if (Quality(S) >= Quality(H)) {
            return new ArrayList<>(S);
        } else {
            return new ArrayList<>(H);
        }
    }

    private static List<FullAssignment> Tweak(List<FullAssignment> assignments, List<Project> projects, List<Contributor> contributors) {
//        int operator = (int) (Math.random() * 2);
//
//        switch (operator) {
//            case 0:
//                return InsertUnassignedProject(assignments, projects, contributors);
//            case 1:
                return Swap(assignments);
//            default:
//                return assignments;
//        }
    }

    private static List<FullAssignment> Swap(List<FullAssignment> assignments) {
        Random random = new Random();
        int swapCount = (int) Math.ceil(assignments.size() * 0.1);

        for (int i = 0; i < swapCount; i++) {
            int index1 = random.nextInt(assignments.size());
            int index2 = random.nextInt(assignments.size());

            FullAssignment temp = assignments.get(index1);
            assignments.set(index1, assignments.get(index2));
            assignments.set(index2, temp);
        }
        return assignments;
    }

    private static List<FullAssignment> InsertUnassignedProject(List<FullAssignment> assignments, List<Project> projects, List<Contributor> contributors) {
        List<String> usedProjectNames = assignments.stream().map(fullAssignment -> fullAssignment.getProject().getName()).collect(Collectors.toList());
        List<Project> unassignedProjects = projects.stream().filter(project -> !usedProjectNames.contains(project.getName())).collect(Collectors.toList());

        assignments.addAll(AssignmentInitialSolver.solve(contributors, unassignedProjects));
        return assignments;
    }

    private static List<FullAssignment> Perturb(List<FullAssignment> assignments) {
        Random random = new Random();
        int swapCount = (int) Math.ceil(assignments.size() * 0.4);

        for (int i = 0; i < swapCount; i++) {
            int index1 = random.nextInt(assignments.size());
            int index2 = random.nextInt(assignments.size());

            FullAssignment temp = assignments.get(index1);
            assignments.set(index1, assignments.get(index2));
            assignments.set(index2, temp);
        }
        return assignments;
    }
}