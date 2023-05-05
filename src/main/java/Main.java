import entities.Contributor;
import entities.FullAssignment;
import entities.Project;
import utilities.*;

import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        List<String> fileNames = InputReader.readFileName("a");
        List<String> fileContents = InputReader.readFileContent(fileNames.get(0));

        List<Contributor> contributors = InputReader.readContributors(fileContents);
        List<Project> projects = InputReader.readProjects(fileContents);

        List<Contributor> deepCopyOfContributorsForInitialSolutionValidation = contributors.stream().map(Contributor::deepCopy).collect(Collectors.toList());
        List<Contributor> deepCopyOfContributorsForILSValidation = contributors.stream().map(Contributor::deepCopy).collect(Collectors.toList());

        List<Project> deepCopyOfProjectsForInitialSolutionValidation = projects.stream().map(Project::deepCopy).collect(Collectors.toList());
        List<Project> deepCopyOfProjectsForILSValidation = projects.stream().map(Project::deepCopy).collect(Collectors.toList());

        List<FullAssignment> assignments = InitialSolver.solve(contributors, projects);

        if (!Validator.areAssignmentsValid(assignments, deepCopyOfContributorsForInitialSolutionValidation, deepCopyOfProjectsForInitialSolutionValidation)) {
            System.out.println("Wrong initial solution!");
            System.exit(0);
        }

        System.out.println("The initial solution is valid!");
        System.out.println("Fitness score: " + FitnessCalculator.getFitnessScore(assignments));

        OutputWriter.writeContent(assignments, fileNames.get(1));

//        List<FullAssignment> assignmentAfterILS = IteratedLocalSearch.iteratedLocalSearchWithRandomRestarts(assignments, Integer.parseInt("0"), projects, contributors);
//
//        if (!Validator.areAssignmentsValid(assignmentAfterILS, deepCopyOfContributorsForILSValidation, deepCopyOfProjectsForILSValidation)) {
//            System.out.println("Wrong assignments after ILS algorithm");
//            System.exit(0);
//        }
//
//        System.out.println("Fitness score: " + FitnessCalculator.getFitnessScore(assignmentAfterILS));
//        OutputWriter.writeContent(assignmentAfterILS, fileNames.get(1));
    }
}