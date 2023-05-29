package algorithms;

import entities.Assignment;
import entities.Contributor;
import entities.Project;
import entities.Skill;
import utilities.MetaheuristicUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class IteratedLocalSearch {

    private static final int SECONDS_IN_MINUTE = 60;


    public static List<Assignment> performSearch(List<Assignment> initialSolution, int maxMinutes, List<Project> projects, List<Contributor> contributors) {
        List<Assignment> currentSolution = new ArrayList<>(initialSolution);
        List<Assignment> currentHomeBase = new ArrayList<>(currentSolution);
        List<Assignment> bestSolution = new ArrayList<>(currentSolution);

        long startTime = System.currentTimeMillis();
        long maxMillis = TimeUnit.MINUTES.toMillis(maxMinutes);

        while (System.currentTimeMillis() - startTime < maxMillis) {
            int innerIteration = 0;
            while (System.currentTimeMillis() - startTime < maxMillis && innerIteration < maxMinutes * SECONDS_IN_MINUTE) {
                List<Assignment> tweakedSolution = NeighborhoodOperators.tweak(MetaheuristicUtilities.copySolution(currentSolution), projects, contributors);
                if (MetaheuristicUtilities.deltaQuality(currentSolution, tweakedSolution) > 0) {
                    currentSolution = new ArrayList<>(tweakedSolution);
                }
                innerIteration++;
            }

            if (MetaheuristicUtilities.deltaQuality(bestSolution, currentSolution) > 0) {
                bestSolution = new ArrayList<>(currentSolution);
            }

            currentHomeBase = newHomeBase(currentHomeBase, currentSolution);
            currentSolution = perturb(currentHomeBase, contributors);
        }

        return bestSolution;
    }


    public static List<Assignment> newHomeBase(List<Assignment> currentHomeBase, List<Assignment> currentSolution) {
        if (MetaheuristicUtilities.quality(currentSolution) >= MetaheuristicUtilities.quality(currentHomeBase)) {
            return MetaheuristicUtilities.copySolution(currentSolution);
        } else {
            return MetaheuristicUtilities.copySolution(currentHomeBase);
        }
    }


    private static List<Assignment> perturb(List<Assignment> assignments, List<Contributor> contributors) {

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