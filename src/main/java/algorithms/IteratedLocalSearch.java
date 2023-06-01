package algorithms;

import entities.Assignment;
import entities.Contributor;
import entities.Project;
import utilities.InitialSolver;
import utilities.MetaheuristicUtilities;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class IteratedLocalSearch {

    private static final int SECONDS_IN_MINUTE = 60;


    public static List<Assignment> performSearch(
            List<Assignment> initialSolution,
            int maxMinutes,
            List<Project> projects,
            List<Contributor> contributors,
            List<Contributor> initialContributors
    ) {
        List<Assignment> currentSolution = new ArrayList<>(initialSolution);
        List<Assignment> currentHomeBase = new ArrayList<>(currentSolution);
        List<Assignment> bestSolution = new ArrayList<>(currentSolution);

        long startTime = System.currentTimeMillis();
        long maxMillis = TimeUnit.MINUTES.toMillis(maxMinutes);

        while (System.currentTimeMillis() - startTime < maxMillis) {
            int innerIteration = 0;
//            System.currentTimeMillis() - startTime < maxMillis && innerIteration < maxMinutes * SECONDS_IN_MINUTE
            while (innerIteration < 500) {
                List<Assignment> tweakedSolution = NeighborhoodOperators.tweak(MetaheuristicUtilities.copySolution(currentSolution), projects, contributors);
                if (MetaheuristicUtilities.deltaQuality(currentSolution, tweakedSolution) > 0) {
                    currentSolution = new ArrayList<>(tweakedSolution);
                }
                innerIteration++;
            }

            if (MetaheuristicUtilities.deltaQuality(bestSolution, currentSolution) > 0) {
                bestSolution = new ArrayList<>(currentSolution);
            }
            System.out.println("After Tweak 1: " + MetaheuristicUtilities.quality(bestSolution));
            System.out.println("After Tweak 2: " + MetaheuristicUtilities.quality(currentSolution));

            currentHomeBase = newHomeBase(currentHomeBase, currentSolution);

            List<Contributor> useContributors = initialContributors.stream().map(Contributor::deepCopy).collect(Collectors.toList());
            currentSolution = perturb(currentHomeBase, useContributors).stream().map(Assignment::deepCopy).collect(Collectors.toList());
            contributors = contributors.stream().map(Contributor::deepCopy).collect(Collectors.toList());

            System.out.println("Perturb: " + MetaheuristicUtilities.quality(currentSolution));
        }

        List<UUID> projectIDs = bestSolution.stream().map(assignment -> assignment.getProject().getId()).collect(Collectors.toList());
        List<Project> unassignedProjects = projects.stream().filter(project -> !projectIDs.contains(project.getId())).collect(Collectors.toList());
        List<Assignment> additionalAssignments = InitialSolver.solveMentorshipAndTeamwork(unassignedProjects, contributors);
        bestSolution.addAll(additionalAssignments);

        return bestSolution;
    }


//    public static List<Contributor> updateScore(List<Contributor> initialContributors, List<Assignment> assignments) {
//        List<Contributor> n_contributors = new ArrayList<>();
//
//        for (int i = 0; i < assignments.size(); i++) {
//            Project project = assignments.get(i).getProject();
//            List<Contributor> contributors = new ArrayList<>(assignments.get(i).getRoleWithContributorMap().values());
//
//            for(int j = 0; j < contributors.size(); j++) {
//                if() {
//
//                }
//            }
//
//        }
//
//
//        return initialContributors;
//    }


    public static List<Assignment> newHomeBase(List<Assignment> currentHomeBase, List<Assignment> currentSolution) {
        if (MetaheuristicUtilities.quality(currentSolution) >= MetaheuristicUtilities.quality(currentHomeBase)) {
            return MetaheuristicUtilities.copySolution(currentSolution);
        } else {
            return MetaheuristicUtilities.copySolution(currentHomeBase);
        }
    }


    private static List<Assignment> perturb(List<Assignment> assignments, List<Contributor> initialContributors) {
        List<Project> newProjects = assignments.stream()
                .map(Assignment::getProject)
                .collect(Collectors.toList());

        int numElementsToRemove = (int) (assignments.size() * 0.5);

        Random random = new Random();
        for (int i = 0; i < numElementsToRemove; i++) {
            int randomIndex = random.nextInt(newProjects.size());
            newProjects.remove(randomIndex);
        }
//        newProjects.sort((b1, b2) -> Integer.compare(-b2.getBestBefore(), -b1.getBestBefore()));
//        newProjects.sort((p1, p2) -> {
//            // Compare by score
//            int scoreComparison = Integer.compare(p2.getScore(), p1.getScore());
//            if (scoreComparison != 0) {
//                return scoreComparison;
//            }
//            // Scores are equal, compare by bestBefore
//            return Integer.compare(p1.getBestBefore(), p2.getBestBefore());
//        });
////        Collections.shuffle(newProjects);
//
////        newProjects.sort((b1, b2) -> Integer.compare(-b2.getBestBefore(), -b1.getBestBefore()));
////        Collections.shuffle(initialContributors);

        newProjects.sort((b1, b2) -> Integer.compare(-b2.getScore(), -b1.getScore()));

        return InitialSolver.solveMentorshipAndTeamwork(newProjects, initialContributors);
    }
}