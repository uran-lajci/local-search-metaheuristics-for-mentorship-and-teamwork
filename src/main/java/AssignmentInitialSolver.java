import entities.*;

import java.util.*;
import java.util.stream.Collectors;

public class AssignmentInitialSolver {

    public static List<FullAssignment> solve(List<Contributor> contributors, List<Project> projects) {
        Map<UUID, Map<UUID, Skill>> contributorSkillsMap = contributors.stream().collect(Collectors.toMap(Contributor::getId, contributor -> contributor.getSkills().stream().collect(Collectors.toMap(Skill::getId, skill -> skill))));
        Map<UUID, List<String>> contributorSkillMap = contributors.stream().collect(Collectors.toMap(Contributor::getId, contributor -> contributor.getSkills().stream().map(Skill::getName).collect(Collectors.toList())));
        Map<UUID, Map<String, SkillNameWithLevel>> contributorSkillWithLevelMap = contributors.stream().collect(Collectors.toMap(Contributor::getId, contributor -> contributor.getSkills().stream().collect(Collectors.toMap(Skill::getName, skill -> new SkillNameWithLevel(skill.getName(), skill.getLevel())))));

        List<FullAssignment> assignments = new ArrayList<>();
        List<ContributorWithAssignedSkill> contributorSkillToBeIncreased = new ArrayList<>();

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
                                contributorsToIncreaseScore.add(new ContributorWithAssignedSkill(contributors.get(k), projectSkill));
                                if (projectSkill.getLevel() == contributorLevel) {
                                    for (Skill s : contributors.get(k).getSkills()) {
                                        if (Objects.equals(s.getName(), projectSkill.getName())) {
                                            s.setLevel(s.getLevel() + 1);
                                        }
                                    }
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
                                    for (Skill s : contributors.get(k).getSkills()) {
                                        if (Objects.equals(s.getName(), projectSkill.getName())) {
                                            s.setLevel(s.getLevel() + 1);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
//                        else if (projectSkill.getLevel() == 1) {
//                            if (!contributorIds.contains(contributorId) && !skillIds.contains(projectSkill.getId())) {
//                                if (hasMentor(projectSkill.getName(), projectSkill.getLevel(), assignedContributors)) {
//                                    printingOrders.add(new FullAssignment.PrintingOrder(projectSkill.getId(), contributors.get(k)));
//                                    assignmentContributor.add(contributors.get(k));
//                                    contributorIds.add(contributorId);
//                                    skillIds.add(projectSkill.getId());
//                                    assignedContributors.add(new ContributorWithAssignedSkill(contributors.get(k), projectSkill));
//                                    contributorsToIncreaseScore.add(new ContributorWithAssignedSkill(contributors.get(k), projectSkill));
//                                    contributors.get(k).getSkills().add(new Skill(UUID.randomUUID(), projectSkill.getName(), 1));
//                                    break;
//                                }
//                            }
//                        }

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
            } else {
                for (int d = 0; d < contributors.size(); d++) {
                    Contributor contributor = contributors.get(d);
                    for (int c = 0; c < contributorSkillToBeIncreased.size(); c++) {
                        if (Objects.equals(contributor.getName(), contributorSkillToBeIncreased.get(c).getContributor().getName())) {
                            for (int k = 0; k < contributor.getSkills().size(); k++) {
                                if (Objects.equals(contributor.getSkills().get(k).getName(), contributorSkillToBeIncreased.get(c).getAssignedSkill().getName())) {
                                    int level = contributor.getSkills().get(k).getLevel() - 1;
                                    contributor.getSkills().get(k).setLevel(level);
                                }
                            }
                        }
                    }
                }
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

}
