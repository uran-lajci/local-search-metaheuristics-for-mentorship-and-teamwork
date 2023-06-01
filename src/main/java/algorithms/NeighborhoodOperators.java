package algorithms;

import entities.Assignment;
import entities.Contributor;
import entities.Project;
import entities.Skill;
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
//            case 2:
//                return replaceContributors(assignments, contributors);
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
//            unassignedProjects.sort((p1, p2) -> {
//                // Compare by score
//                int scoreComparison = Integer.compare(p2.getScore(), p1.getScore());
//                if (scoreComparison != 0) {
//                    return scoreComparison;
//                }
//                // Scores are equal, compare by bestBefore
//                return Integer.compare(p1.getBestBefore(), p2.getBestBefore());
//            });
//            unassignedProjects.sort((b1, b2) -> Integer.compare(-b2.getBestBefore(), -b1.getBestBefore()));

            unassignedProjects.sort((b1, b2) -> Integer.compare(-b2.getScore(), -b1.getScore()));
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

//        int i = new Random(assignments.size()).nextInt();
//        int j = new Random(assignments.size()).nextInt();

        int iterations = (int) Math.ceil(assignments.size() * 0.05); // Calculate 20% of the list size
        List<Integer> indices = new ArrayList<>();

// Generate random indices
        Random random = new Random();
        while (indices.size() < iterations) {
            int index = random.nextInt(assignments.size());
            if (!indices.contains(index)) {
                indices.add(index);
            }
        }

        for (int i : indices) {
            for (int j : indices) {
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
                    }
                }
            }
        }


//        for (int i = 0; i < assignments.size(); i++) {
//            for (int j = 0; j < assignments.size(); j++) {
//                if (i != j) {
//                    Project secondProject = assignments.get(j).getProject();
//                    Map<Integer, Contributor> secondProjectAssignedContributors = assignments.get(j).getRoleWithContributorMap();
//                    List<UUID> secondProjectAssignedContributorIds = secondProjectAssignedContributors.values().stream().map(Contributor::getId).collect(Collectors.toList());
//
//                    List<Contributor> firstContributors = newContributorState.get(assignments.get(i).getProject().getName());
//
//                    List<Contributor> OnlyProjectContributors = firstContributors.stream()
//                            .filter(contributor -> secondProjectAssignedContributorIds.contains(contributor.getId())).collect(Collectors.toList());
//
//                    List<Project> c = new ArrayList<>();
//                    c.add(secondProject);
//                    List<Assignment> newAssignments = InitialSolver.solveMentorshipAndTeamwork(c, OnlyProjectContributors);
//
//                    if (newAssignments.size() > 0) {
//                        int assignmentsScore1 = FitnessCalculator.getFitnessScore(assignments);
//                        List<Assignment> newAssignmentList = assignments.stream().map(Assignment::deepCopy).collect(Collectors.toList());
//                        Collections.swap(newAssignmentList, i, j);
//
//                        int assignmentsScore2 = FitnessCalculator.getFitnessScore(newAssignmentList);
//
//                        if (assignmentsScore2 > assignmentsScore1) {
////                            System.out.println(i + " = " + assignmentsScore2 + " " + assignmentsScore1);
//                            return newAssignmentList;
//                        }
//                        // Update Score
//                    }
//                }
//            }
//        }

        return assignments;
    }


    public static List<Assignment> replaceContributor(List<Assignment> assignments) {

        for(Assignment assignment : assignments) {
            List<Contributor> contributors = new ArrayList<>(assignment.getRoleWithContributorMap().values());

        }

        return assignments;
    }

    public static List<Assignment> replaceContributors(List<Assignment> assignments, List<Contributor> contributors) {
        int removeCount = (int) Math.ceil(assignments.size() * 0.8);
        List<Assignment> removedAssignments = new ArrayList<>();
        for (int i = 0; i < removeCount; i++) {
            removedAssignments.add(assignments.remove(assignments.size() - 1));
        }

        for (Assignment assignment : removedAssignments) {
            Project project = assignment.getProject();
            Map<Integer, Contributor> contributorMap = assignment.getRoleWithContributorMap();
            for (Integer index : contributorMap.keySet()) {
                Map<String, Integer> contributorSkillLevel = contributorMap.get(index).getSkills().stream().collect(
                        Collectors.toMap(Skill::getName, Skill::getLevel, (existingValue, newValue) -> existingValue));

                Skill skill = project.getSkills().get(index - 1);

                if (contributorSkillLevel.containsKey(skill.getName())) {
                    if (skill.getLevel() == contributorSkillLevel.get(skill.getName())
                            || skill.getLevel() == contributorSkillLevel.get(skill.getName()) - 1) {
                        for (Contributor contributor : contributors) {
                            if (Objects.equals(contributor.getName(), contributorMap.get(index).getName())) {
                                for (Skill contributorSkill : contributor.getSkills()) {
                                    if (Objects.equals(skill.getName(), contributorSkill.getName())) {
                                        skill.setLevel(skill.getLevel() - 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return assignments;
    }
}
