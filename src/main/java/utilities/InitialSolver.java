package utilities;

import entities.Assignment;
import entities.Contributor;
import entities.Project;
import entities.Skill;

import java.util.*;
import java.util.stream.Collectors;

public class InitialSolver {

    private static Map<UUID, String> contributorIdAndSkillNameToIncrease = new HashMap<>();
    private static Map<UUID, String> contributorIdAndSkillNameToAdd = new HashMap<>();
    private static Map<String, List<Contributor>> projectNameWithContributorsMap = new HashMap<>();

    public static List<Assignment> solveMentorshipAndTeamwork(List<Project> projects, List<Contributor> contributors) {

        applyHeuristic(projects, contributors);

        Map<UUID, List<String>> contributorIdWithSkillNamesMap = getContributorSkillsMap(contributors);
        Map<UUID, Map<String, Integer>> contributorIdAndSkillNameWithLevel = getContributorSkillLevelMap(contributors);
        List<Assignment> assignments = new ArrayList<>();

        for (Project project : projects) {
            Map<Integer, Contributor> assignedContributorsMap = new HashMap<>();
            List<UUID> assignedContributorIds = new ArrayList<>();
            List<UUID> addedSkillIds = new ArrayList<>();

            for (Skill projectSkill : project.getSkills()) {
                for (Contributor contributor : contributors) {
                    assignContributorToProject(
                            addedSkillIds,
                            projectSkill,
                            assignedContributorIds,
                            contributor,
                            contributorIdWithSkillNamesMap,
                            contributorIdAndSkillNameWithLevel,
                            assignedContributorsMap
                    );
                }

                if (isProjectFullyAssigned(project, addedSkillIds)) {
                    assignments.add(new Assignment(UUID.randomUUID(), project, assignedContributorsMap));
                    projectNameWithContributorsMap.put(projectNameWithContributorsMap.size() + "-" + project.getName(), contributors.stream().map(Contributor::deepCopy).collect(Collectors.toList()));
                    updateTheAssignedSkillsOfContributors(contributors);
                    rearrangeContributors(contributors, assignedContributorsMap);
                    break;
                }

                clearContributorSkillMaps();
            }
        }
        return assignments;
    }

    public static Map<String, List<Contributor>> getContributorsState() {
//        System.out.println("------------------------------------------------------------------------------");
//
//        for (String projectName : projectNameWithContributorsMap.keySet()) {
//            System.out.println(projectName);
//            List<Contributor> contributorList = projectNameWithContributorsMap.get(projectName);
//            for(Contributor contributor : contributorList) {
//                System.out.print(contributor.getName() + " ");
//                for(Skill skill : contributor.getSkills()) {
//                    System.out.print(skill.getName() + " " + skill.getLevel() + " - ");
//                }
//            }
//            System.out.println();
//        }
//
//        System.out.println("------------------------------------------------------------------------------");
        return projectNameWithContributorsMap;
    }

    public static class ProjectComparator implements Comparator<Project> {
        @Override
        public int compare(Project p1, Project p2) {
            int p1TotalSkills = p1.getSkills().stream().mapToInt(Skill::getLevel).sum();
            int p2TotalSkills = p2.getSkills().stream().mapToInt(Skill::getLevel).sum();

            // compare based on daysToComplete
            int comparison = Integer.compare(p1.getDaysToComplete(), p2.getDaysToComplete());
            if (comparison != 0) return comparison;

            // then compare based on score
            comparison = Integer.compare(p1.getScore(), p2.getScore());
            if (comparison != 0) return comparison;

            // then compare based on total skills
            return Integer.compare(p1TotalSkills, p2TotalSkills);
        }
    }

    private static void applyHeuristic(List<Project> projects, List<Contributor> contributors) {
        Collections.shuffle(contributors, new Random());
//        Collections.shuffle(projects, new Random());
        projects.sort((b1, b2) -> Integer.compare(-b2.getBestBefore(), -b1.getBestBefore()));
//        projects.sort((p1, p2) -> {
//            // Compare by score
//            int scoreComparison = Integer.compare(p2.getScore(), p1.getScore());
//            if (scoreComparison != 0) {
//                return scoreComparison;
//            }
//            // Scores are equal, compare by bestBefore
//            return Integer.compare(p1.getBestBefore(), p2.getBestBefore());
//        });
//        projects.sort(new ProjectComparator());
    }


    private static Map<UUID, List<String>> getContributorSkillsMap(List<Contributor> contributors) {
        return contributors.stream()
                .collect(Collectors.toMap(
                        Contributor::getId,
                        contributor -> contributor.getSkills().stream().map(Skill::getName).collect(Collectors.toList())));
    }


    private static Map<UUID, Map<String, Integer>> getContributorSkillLevelMap(List<Contributor> contributors) {
        return contributors.stream()
                .collect(Collectors.toMap(
                        Contributor::getId,
                        contributor -> contributor.getSkills().stream()
                                .collect(Collectors.toMap(
                                        Skill::getName,
                                        Skill::getLevel,
                                        (existingValue, newValue) -> existingValue))));
    }


    private static void assignContributorToProject(
            List<UUID> addedSkillIds,
            Skill projectSkill,
            List<UUID> assignedContributorIds,
            Contributor contributor,
            Map<UUID, List<String>> contributorIdWithSkillNamesMap,
            Map<UUID, Map<String, Integer>> contributorIdAndSkillNameWithLevel,
            Map<Integer, Contributor> assignedContributorsMap
    ) {
        if (skillIsNotAdded(addedSkillIds, projectSkill) && contributorIsNotAdded(assignedContributorIds, contributor)) {
            if (contributorHasTheSkill(contributorIdWithSkillNamesMap, contributor, projectSkill)) {
                int contributorSkillLevel = contributorIdAndSkillNameWithLevel.get(contributor.getId()).get(projectSkill.getName());
                if (contributorSkillLevel >= projectSkill.getLevel()) {
                    addContributorToProject(addedSkillIds, projectSkill, assignedContributorsMap, contributor, assignedContributorIds);
                    if (contributorSkillLevel == projectSkill.getLevel()) {
                        contributorIdAndSkillNameToIncrease.put(contributor.getId(), projectSkill.getName());
                    }
                } else if (contributorSkillLevel == projectSkill.getLevel() - 1 && hasMentor(projectSkill.getLevel(), projectSkill.getName(), assignedContributorsMap)) {
                    addContributorToProject(addedSkillIds, projectSkill, assignedContributorsMap, contributor, assignedContributorIds);
                    contributorIdAndSkillNameToIncrease.put(contributor.getId(), projectSkill.getName());
                }
            } else if (projectSkill.getLevel() == 1 && hasMentor(projectSkill.getLevel(), projectSkill.getName(), assignedContributorsMap)) {
                addContributorWithZeroSkillToProject(addedSkillIds, projectSkill, contributor, assignedContributorsMap, assignedContributorIds);
            }
        }
    }


    private static boolean skillIsNotAdded(List<UUID> addedSkill, Skill projectSkill) {
        return !addedSkill.contains(projectSkill.getId());
    }


    private static boolean contributorIsNotAdded(List<UUID> assignedContributorIds, Contributor contributor) {
        return !assignedContributorIds.contains(contributor.getId());
    }


    private static boolean contributorHasTheSkill(Map<UUID, List<String>> contributorIdWithSkillNamesMap, Contributor contributor, Skill projectSkill) {
        return contributorIdWithSkillNamesMap.get(contributor.getId()).contains(projectSkill.getName());
    }


    private static void addContributorToProject(
            List<UUID> addedSkill,
            Skill projectSkill,
            Map<Integer, Contributor> assignedContributorsMap,
            Contributor contributor,
            List<UUID> assignedContributorIds
    ) {
        addedSkill.add(projectSkill.getId());
        assignedContributorsMap.put(assignedContributorsMap.size() + 1, contributor);
        assignedContributorIds.add(contributor.getId());
    }


    private static boolean hasMentor(int projectSkillLevel, String projectSkillName, Map<Integer, Contributor> assignedContributorsToProjectMap) {
        for (Contributor contributor : assignedContributorsToProjectMap.values()) {
            for (Skill skill : contributor.getSkills()) {
                if (Objects.equals(skill.getName(), projectSkillName) && skill.getLevel() >= projectSkillLevel) {
                    return true;
                }
            }
        }
        return false;
    }


    private static void addContributorWithZeroSkillToProject(
            List<UUID> addedSkill,
            Skill projectSkill,
            Contributor contributor,
            Map<Integer, Contributor> assignedContributorsToProject,
            List<UUID> assignedContributorIdsToProject
    ) {
        addedSkill.add(projectSkill.getId());
        assignedContributorsToProject.put(assignedContributorsToProject.size() + 1, contributor);
        assignedContributorIdsToProject.add(contributor.getId());
        contributorIdAndSkillNameToAdd.put(contributor.getId(), projectSkill.getName());
    }


    private static boolean isProjectFullyAssigned(Project project, List<UUID> addedSkill) {
        return project.getSkills().size() == addedSkill.size();
    }


    private static void updateTheAssignedSkillsOfContributors(List<Contributor> contributors) {
        for (Contributor contributor : contributors) {
            if (contributorIdAndSkillNameToIncrease.containsKey(contributor.getId())) {
                for (Skill skill : contributor.getSkills()) {
                    if (Objects.equals(skill.getName(), contributorIdAndSkillNameToIncrease.get(contributor.getId()))) {
                        skill.setLevel(skill.getLevel() + 1);
                    }
                }
            }

            if (contributorIdAndSkillNameToAdd.containsKey(contributor.getId())) {
                contributor.getSkills().add(new Skill(UUID.randomUUID(), contributorIdAndSkillNameToAdd.get(contributor.getId()), 1));
            }
        }
    }


    private static void rearrangeContributors(List<Contributor> contributors, Map<Integer, Contributor> assignedContributors) {
        // Remove all assigned contributors from the contributors list
        contributors.removeAll(assignedContributors.values());
        contributors.addAll(assignedContributors.values());
        // Add all assigned contributors to random positions in the contributors list
//        for (Contributor assignedContributor : assignedContributors.values()) {
//            int randomIndex = new Random().nextInt(contributors.size() + 1);
//            contributors.add(randomIndex, assignedContributor);
//        }
    }


    private static void clearContributorSkillMaps() {
        contributorIdAndSkillNameToIncrease = new HashMap<>();
        contributorIdAndSkillNameToAdd = new HashMap<>();
    }
}