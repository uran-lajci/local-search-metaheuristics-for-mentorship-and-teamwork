import entities.Contributor;
import entities.FullAssignment;
import entities.Project;
import entities.Skill;
import utilities.FitnessCalculator;
import utilities.InputReader;
import utilities.Validator;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Test {

    public static void main(String[] args) throws Exception {
        List<String> fileNames = InputReader.readFileName("c");
        List<String> fileContents = InputReader.readFileContent(fileNames.get(0));

        List<Contributor> contributors = InputReader.readContributors(fileContents);
        contributors = contributors.stream()
                .sorted(Comparator
                        .comparingInt((Contributor contributor) -> contributor.getSkills().size())
                        .thenComparingInt(contributor -> contributor.getSkills().stream()
                                .mapToInt(Skill::getLevel)
                                .sum()))
                .collect(Collectors.toList());
//        Random randomContributor = new Random();
//        long seedForContributor = randomContributor.nextLong();
//        Collections.shuffle(contributors, new Random(seedForContributor));

        List<Project> projects = InputReader.readProjects(fileContents);
        projects = projects.stream()
                .sorted(Comparator
                        .comparingInt(Project::getScore).reversed()
                        .thenComparingInt(Project::getBestBefore).reversed()
                        .thenComparingInt(Project::getDaysToComplete))
                .collect(Collectors.toList());

        List<Contributor> deepCopyOfContributorsForInitialSolutionValidation = contributors.stream().map(Contributor::deepCopy).collect(Collectors.toList());
        List<Contributor> deepCopyOfContributorsForILSValidation = contributors.stream().map(Contributor::deepCopy).collect(Collectors.toList());

        List<Project> deepCopyOfProjectsForInitialSolutionValidation = projects.stream().map(Project::deepCopy).collect(Collectors.toList());
        List<Project> deepCopyOfProjectsForILSValidation = projects.stream().map(Project::deepCopy).collect(Collectors.toList());

        List<FullAssignment> assignments = AssignmentInitialSolver.solve(contributors, projects);

        if (!Validator.areAssignmentsValid(assignments, deepCopyOfContributorsForInitialSolutionValidation, deepCopyOfProjectsForInitialSolutionValidation)) {
            System.out.println("Wrong initial solution!");
            System.exit(0);
        }

//        System.out.println(contributors);

        System.out.println("The initial solution is valid!");
        int initialFitnessScore = FitnessCalculator.getFitnessScore(assignments);
        System.out.println("Fitness score: " + initialFitnessScore);

//        List<FullAssignment> assignmentsAfterILS = ILS.iteratedLocalSearchWithRandomRestarts(assignments, 1, projects, contributors);
//
//        if (!Validator.areAssignmentsValid(assignmentsAfterILS, deepCopyOfContributorsForILSValidation, deepCopyOfProjectsForILSValidation)) {
//            System.out.println("Wrong iterated local search solution!");
//            System.exit(0);
//        }
//
//        System.out.println("\nThe ILS solution is valid!");
//        int ilsFitnessScore = FitnessCalculator.getFitnessScore(assignmentsAfterILS);
//        System.out.println("Fitness score: " + ilsFitnessScore);
//
//        System.out.println("Added: " + (ilsFitnessScore - initialFitnessScore));
//
//        OutputWriter.writeContent(assignmentsAfterILS, fileNames.get(1));
    }
}
