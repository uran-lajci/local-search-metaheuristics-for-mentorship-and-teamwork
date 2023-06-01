package utilities;

import entities.Assignment;
import entities.Contributor;
import entities.Project;
import entities.Skill;

import java.util.*;
import java.util.stream.Collectors;

public class InitialSolver {

    private static final Map<String, List<Contributor>> projectNameWithContributorsMap = new HashMap<>();
    private static Map<UUID, String> contributorIdAndSkillNameToIncrease = new HashMap<>();
    private static Map<UUID, String> contributorIdAndSkillNameToAdd = new HashMap<>();

    public static List<Assignment> solveMentorshipAndTeamwork(List<Project> projects, List<Contributor> contributors) {
        List<Project> n_projects = projects.stream().map(Project::deepCopy).collect(Collectors.toList());
        Map<UUID, List<String>> contributorIdWithSkillNamesMap = getContributorSkillsMap(contributors);
        Map<UUID, Map<String, Integer>> contributorIdAndSkillNameWithLevel = getContributorSkillLevelMap(contributors);
        List<Assignment> assignments = new ArrayList<>();

        List<UUID> doNotLookProjects = new ArrayList<>();

        int k = 0;

        for (int i = 0; i < projects.size(); i++) {
//            if(i == 2000) {
//                break;
//            }
            k++;
//            System.out.println("k=" + k + ": i=" + i);
            Project project = projects.get(i);
            if (!doNotLookProjects.contains(project.getId())) {
                Map<Integer, Contributor> assignedContributorsMap = new HashMap<>();
                List<UUID> assignedContributorIds = new ArrayList<>();
                List<UUID> addedSkillIds = new ArrayList<>();

                for (Skill projectSkill : project.getSkills()) {
                    String skillName = projectSkill.getName();

                    contributors.sort((c1, c2) -> {
                        int level1 = c1.getSkills().stream()
                                .filter(skill -> skill.getName().equals(skillName))
                                .map(Skill::getLevel)
                                .findFirst()
                                .orElse(Integer.MIN_VALUE);

                        int level2 = c2.getSkills().stream()
                                .filter(skill -> skill.getName().equals(skillName))
                                .map(Skill::getLevel)
                                .findFirst()
                                .orElse(Integer.MIN_VALUE);

                        return Integer.compare(-level1, -level2);
                    });

                    for (Contributor contributor : contributors) {

//                        assignContributorToProject(
//                                addedSkillIds,
//                                projectSkill,
//                                assignedContributorIds,
//                                contributor,
//                                contributorIdWithSkillNamesMap,
//                                contributorIdAndSkillNameWithLevel,
//                                assignedContributorsMap
//                        );



                        if (skillIsNotAdded(addedSkillIds, projectSkill) && contributorIsNotAdded(assignedContributorIds, contributor)) {
                            if(!contributorHasTheSkill(contributorIdWithSkillNamesMap, contributor, projectSkill)) {
                                if (projectSkill.getLevel() == 1 && hasMentor(projectSkill.getLevel(), projectSkill.getName(), assignedContributorsMap)) {
                                    addContributorWithZeroSkillToProject(addedSkillIds, projectSkill, contributor, assignedContributorsMap, assignedContributorIds);
                                }
                            }
                            else {
                                int contributorSkillLevel = contributorIdAndSkillNameWithLevel.get(contributor.getId()).get(projectSkill.getName());
                                if (contributorSkillLevel < projectSkill.getLevel()) {
                                    if (contributorSkillLevel == projectSkill.getLevel() - 1 && hasMentor(projectSkill.getLevel(), projectSkill.getName(), assignedContributorsMap)) {
                                        addContributorToProject(addedSkillIds, projectSkill, assignedContributorsMap, contributor, assignedContributorIds);
                                        contributorIdAndSkillNameToIncrease.put(contributor.getId(), projectSkill.getName());
                                    }
                                }
                                else {
                                    addContributorToProject(addedSkillIds, projectSkill, assignedContributorsMap, contributor, assignedContributorIds);
                                    if (contributorSkillLevel == projectSkill.getLevel()) {
                                        contributorIdAndSkillNameToIncrease.put(contributor.getId(), projectSkill.getName());
                                    }
                                }
                            }


//                            if (contributorHasTheSkill(contributorIdWithSkillNamesMap, contributor, projectSkill)) {
//                                int contributorSkillLevel = contributorIdAndSkillNameWithLevel.get(contributor.getId()).get(projectSkill.getName());
//                                if (contributorSkillLevel >= projectSkill.getLevel()) {
//                                    addContributorToProject(addedSkillIds, projectSkill, assignedContributorsMap, contributor, assignedContributorIds);
//                                    if (contributorSkillLevel == projectSkill.getLevel()) {
//                                        contributorIdAndSkillNameToIncrease.put(contributor.getId(), projectSkill.getName());
//                                    }
//                                } else if (contributorSkillLevel == projectSkill.getLevel() - 1 && hasMentor(projectSkill.getLevel(), projectSkill.getName(), assignedContributorsMap)) {
//                                    addContributorToProject(addedSkillIds, projectSkill, assignedContributorsMap, contributor, assignedContributorIds);
//                                    contributorIdAndSkillNameToIncrease.put(contributor.getId(), projectSkill.getName());
//                                }
//                            } else if (projectSkill.getLevel() == 1 && hasMentor(projectSkill.getLevel(), projectSkill.getName(), assignedContributorsMap)) {
//                                addContributorWithZeroSkillToProject(addedSkillIds, projectSkill, contributor, assignedContributorsMap, assignedContributorIds);
//                            }


                        }
                    }

                    if (isProjectFullyAssigned(project, addedSkillIds)) {
                        assignments.add(new Assignment(UUID.randomUUID(), project, assignedContributorsMap));
                        projectNameWithContributorsMap.put(projectNameWithContributorsMap.size() + "-" + project.getName(), contributors.stream().map(Contributor::deepCopy).collect(Collectors.toList()));
                        updateTheAssignedSkillsOfContributors(contributors);
                        rearrangeContributors(contributors, assignedContributorsMap);
                        doNotLookProjects.add(project.getId());
//                        i = 0;
                        break;
                    }

                    clearContributorSkillMaps();
                }
            }
        }

        projects = n_projects;

        return assignments;
    }

    public static Map<String, List<Contributor>> getContributorsState() {
        return projectNameWithContributorsMap;
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

//    private static void assignContributorToProject(
//            List<UUID> addedSkillIds,
//            Skill projectSkill,
//            List<UUID> assignedContributorIds,
//            Contributor contributor,
//            Map<UUID, List<String>> contributorIdWithSkillNamesMap,
//            Map<UUID, Map<String, Integer>> contributorIdAndSkillNameWithLevel,
//            Map<Integer, Contributor> assignedContributorsMap
//    ) {
//
//    }

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
        contributors.removeAll(assignedContributors.values());
        contributors.addAll(assignedContributors.values());
    }

    private static void clearContributorSkillMaps() {
        contributorIdAndSkillNameToIncrease = new HashMap<>();
        contributorIdAndSkillNameToAdd = new HashMap<>();
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
}