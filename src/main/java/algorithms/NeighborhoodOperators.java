package algorithms;

import entities.Assignment;
import entities.Contributor;
import entities.Project;
import utilities.FitnessCalculator;
import utilities.InitialSolver;

import java.util.*;
import java.util.stream.Collectors;

public class NeighborhoodOperators {
    public static List<Assignment> tweak(List<Assignment> assignments, List<Project> projects, List<Contributor> contributors) {
        int operator = (int) (Math.random() * 2);

        switch (operator) {
            case 0:
                return insertProjects(assignments, projects, contributors);
            case 1:
                return swapAssignments(assignments);
            default:
                return assignments;
        }
    }


    public static List<Assignment> insertProjects(List<Assignment> assignments, List<Project> projects, List<Contributor> contributors) {
        List<String> assignedProjectIds = assignments.stream()
                .filter(Objects::nonNull)
                .filter(assignment -> assignment.getProject() != null)
                .map(assignment -> assignment.getProject().getName())
                .collect(Collectors.toList());

        List<Project> unassignedProjects = projects.stream()
                .filter(project -> !assignedProjectIds.contains(project.getName()))
                .collect(Collectors.toList());

        if (unassignedProjects.size() > 0) {
            List<Assignment> additionalFullAssignments = InitialSolver.solveMentorshipAndTeamwork(unassignedProjects, contributors);
            assignments.addAll(additionalFullAssignments);
        }
        return assignments;
    }

    public static List<Assignment> swapAssignments(List<Assignment> assignments) {

        Map<String, List<Contributor>> contributorsState = InitialSolver.getContributorsState();
        Map<String, List<Contributor>> newContributorState = new HashMap<>();
        for (String projectName : contributorsState.keySet()) {
            newContributorState.put(projectName.split("-")[1], contributorsState.get(projectName));
        }

        int i = new Random(assignments.size()).nextInt();
        int j = new Random(assignments.size()).nextInt();

//        for (int i = 0; i < assignments.size(); i++) {
//            for (int j = 0; j < assignments.size(); j++) {
        if (i != j) {
            Project secondProject = assignments.get(j).getProject();
            Map<Integer, Contributor> secondProjectAssignedContributors = assignments.get(j).getRoleWithContributorMap();
            List<UUID> secondProjectAssignedContributorIds = secondProjectAssignedContributors.values().stream().map(Contributor::getId).collect(Collectors.toList());

            List<Contributor> firstContributors = newContributorState.get(assignments.get(i).getProject().getName());

            List<Contributor> OnlyProjectContributors = firstContributors.stream()
                    .filter(contributor -> secondProjectAssignedContributorIds.contains(contributor.getId())).collect(Collectors.toList());

            List<Project> c = new ArrayList<>();
            c.add(secondProject);
            List<Assignment> newAssignments = InitialSolver.solveMentorshipAndTeamwork(c, OnlyProjectContributors);

            if (newAssignments.size() > 0) {
                int assignmentsScore1 = FitnessCalculator.getFitnessScore(assignments);
                List<Assignment> newAssignmentList = assignments.stream().map(Assignment::deepCopy).collect(Collectors.toList());
                Collections.swap(newAssignmentList, i, j);

                int assignmentsScore2 = FitnessCalculator.getFitnessScore(newAssignmentList);

                if (assignmentsScore2 > assignmentsScore1) {
//                            System.out.println(i + " = " + assignmentsScore2 + " " + assignmentsScore1);
                    return newAssignmentList;
                }
                // Update Score
            }
        }
//            }
//        }

        return assignments;
    }
}
