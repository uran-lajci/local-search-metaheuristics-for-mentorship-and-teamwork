import entities.*;

import java.util.*;
import java.util.stream.Collectors;

public class AssignmentInitialSolver {

    public static List<FullAssignment> solve(List<Contributor> contributors, List<Project> projects) {
        Map<UUID, Map<UUID, Skill>> contributorSkillsMap = contributors.stream().collect(Collectors.toMap(Contributor::getId, contributor -> contributor.getSkills().stream().collect(Collectors.toMap(Skill::getId, skill -> skill))));
        Map<UUID, List<String>> contributorSkillMap = contributors.stream().collect(Collectors.toMap(Contributor::getId, contributor -> contributor.getSkills().stream().map(Skill::getName).collect(Collectors.toList())));
        Map<UUID, Map<String, SkillNameWithLevel>> contributorSkillWithLevelMap = contributors.stream().collect(Collectors.toMap(Contributor::getId, contributor -> contributor.getSkills().stream().collect(Collectors.toMap(Skill::getName, skill -> new SkillNameWithLevel(skill.getName(), skill.getLevel())))));


        List<FullAssignment> assignments = new ArrayList<>();

        go_to_next_project:
        for (int i = 0; i < projects.size(); i++) {
            List<UUID> contributorIds = new ArrayList<>();
            List<UUID> skillIds = new ArrayList<>();

            List<Contributor> assignmentContributor = new ArrayList<>();
            List<FullAssignment.PrintingOrder> printingOrders = new ArrayList<>();
            List<Skill> projectSkills = projects.get(i).getSkills();
            List<ContributorWithAssignedSkill> assignedContributors = new ArrayList<>();
            List<ContributorWithAssignedSkill> contributorsToIncreaseScore = new ArrayList<>();


            for (int j = 0; j < projectSkills.size(); j++) {
                Skill projectSkill = projectSkills.get(j);

                for (int k = 0; k < contributors.size(); k++) {
                    UUID contributorId = contributors.get(k).getId();


                    if (contributorSkillMap.get(contributorId).contains(projectSkill.getName())) {
                        int contributorLevel = contributorSkillWithLevelMap.get(contributorId).get(projectSkill.getName()).getLevel();
                        if (projectSkill.getLevel() <= contributorLevel) {
                            if (!contributorIds.contains(contributorId) && !skillIds.contains(projectSkill.getId())) {
                                printingOrders.add(new FullAssignment.PrintingOrder(projectSkill.getId(), contributors.get(k)));
                                assignmentContributor.add(contributors.get(k));
                                contributorIds.add(contributorId);
                                skillIds.add(projectSkill.getId());
                                assignedContributors.add(new ContributorWithAssignedSkill(contributors.get(k), projectSkill));
                                if (projectSkill.getLevel() == contributorLevel) {
                                    contributorsToIncreaseScore.add(new ContributorWithAssignedSkill(contributors.get(k), projectSkill));
                                }
                                break;
                            }
                        } else if (projectSkill.getLevel() - 1 == contributorSkillWithLevelMap.get(contributorId).get(projectSkill.getName()).getLevel()) {
                            if (!contributorIds.contains(contributorId) && !skillIds.contains(projectSkill.getId())) {
                                if (hasMentor(projectSkill.getName(), projectSkill.getLevel(), assignedContributors)) {
                                    printingOrders.add(new FullAssignment.PrintingOrder(projectSkill.getId(), contributors.get(k)));
                                    assignmentContributor.add(contributors.get(k));
                                    contributorIds.add(contributorId);
                                    skillIds.add(projectSkill.getId());
                                    assignedContributors.add(new ContributorWithAssignedSkill(contributors.get(k), projectSkill));
                                    contributorsToIncreaseScore.add(new ContributorWithAssignedSkill(contributors.get(k), projectSkill));
                                    break;
                                }
                            }
                        } else if (projectSkill.getLevel() == 1) {
                            if (!contributorIds.contains(contributorId) && !skillIds.contains(projectSkill.getId())) {
                                if (hasMentor(projectSkill.getName(), projectSkill.getLevel(), assignedContributors)) {
                                    printingOrders.add(new FullAssignment.PrintingOrder(projectSkill.getId(), contributors.get(k)));
                                    assignmentContributor.add(contributors.get(k));
                                    contributorIds.add(contributorId);
                                    skillIds.add(projectSkill.getId());
                                    assignedContributors.add(new ContributorWithAssignedSkill(contributors.get(k), projectSkill));
                                    contributorsToIncreaseScore.add(new ContributorWithAssignedSkill(contributors.get(k), projectSkill));
                                    contributors.get(k).getSkills().add(new Skill(UUID.randomUUID(), projectSkill.getName(), 1));
                                    break;
                                }
                            }
                        }

                    }

                    if (projects.get(i).getSkills().size() == skillIds.size()) {
                        break go_to_next_project;
                    }
                }

            }

            if (projects.get(i).getSkills().size() == printingOrders.size()) {
                FullAssignment assignment = new FullAssignment();
                assignment.setProject(projects.get(i));
                assignment.setContributors(assignmentContributor);
                assignment.setPrintingOrderForContributors(printingOrders);
                assignments.add(assignment);
                increaseLevelOfAssignedContributorSkills(contributorsToIncreaseScore, contributorSkillsMap);
            }
        }
        return assignments;
    }

    private static boolean hasMentor(String skillName, int skillLevel, List<ContributorWithAssignedSkill> contributorWithAssignedSkillList) {
        for (ContributorWithAssignedSkill contributorWithAssignedSkill : contributorWithAssignedSkillList) {
            Skill mentorSkill = contributorWithAssignedSkill.getAssignedSkill();
            if (Objects.equals(mentorSkill.getName(), skillName) && mentorSkill.getLevel() >= skillLevel) {
                return true;
            }
        }
        return false;
    }

    private static void increaseLevelOfAssignedContributorSkills(List<ContributorWithAssignedSkill> contributorWithAssignedSkills, Map<UUID, Map<UUID, Skill>> contributorSkillsMap) {
        for (ContributorWithAssignedSkill contributorWithSkill : contributorWithAssignedSkills) {
            Skill skill = contributorSkillsMap.get(contributorWithSkill.getContributor().getId())
                    .get(contributorWithSkill.getAssignedSkill().getId());

            if (skill != null) {
                skill.setLevel(contributorWithSkill.getAssignedSkill().getLevel() + 1);
            }
        }
    }
}
