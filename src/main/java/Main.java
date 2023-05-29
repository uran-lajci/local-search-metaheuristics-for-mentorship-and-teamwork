import algorithms.HillClimbingWithRandomRestarts;
import algorithms.IteratedLocalSearch;
import algorithms.SimulatedAnnealing;
import entities.Assignment;
import entities.Contributor;
import entities.Project;
import utilities.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        try {
            List<String> inputAndOutputName = InputReader.readFileName(args[0]);
            List<String> fileContents = InputReader.readFileContent(inputAndOutputName.get(0));

            List<Contributor> contributors = InputReader.readContributors(fileContents);
            List<Project> projects = InputReader.readProjects(fileContents);

//            applyHeuristic(projects, contributors, args[0]);

            System.out.println("\nInitial solution");
            List<Assignment> initialAssignments = InitialSolver.solveMentorshipAndTeamwork(projects, contributors);
            processAndValidateAssignments(initialAssignments, contributors, projects);

            System.out.println("\nOptimized solution with Iterated Local Search algorithm (" + args[1] + (Integer.parseInt(args[1]) == 1 ? " minute)" : " minutes)"));
            List<Assignment> assignmentsAfterILS = IteratedLocalSearch.performSearch(initialAssignments, Integer.parseInt(args[1]), projects, contributors);
            processAndValidateAssignments(assignmentsAfterILS, contributors, projects);
            OutputWriter.writeContent(assignmentsAfterILS, inputAndOutputName.get(1) + "_ILS" + "_" + args[1]);

            System.out.println("\nOptimized solution with Simulated Annealing algorithm (" + args[1] + (Integer.parseInt(args[1]) == 1 ? " minute)" : " minutes)"));
            List<Assignment> assignmentsAfterSA = SimulatedAnnealing.performSearch(initialAssignments, Integer.parseInt(args[1]), projects, contributors);
            processAndValidateAssignments(assignmentsAfterSA, contributors, projects);
            OutputWriter.writeContent(assignmentsAfterSA, inputAndOutputName.get(1) + "_SA" + "_" + args[1]);

            System.out.println("\nOptimized solution with Hill Climbing with Random Restarts algorithm (" + args[1] + (Integer.parseInt(args[1]) == 1 ? " minute)" : " minutes)"));
            List<Assignment> assignmentsAfterHCRR = HillClimbingWithRandomRestarts.performSearch(initialAssignments, Integer.parseInt(args[1]), projects, contributors);
            processAndValidateAssignments(assignmentsAfterHCRR, contributors, projects);
            OutputWriter.writeContent(assignmentsAfterHCRR, inputAndOutputName.get(1) + "_HCRR" + "_" + args[1]);

//            System.out.println("\nSubmitted solution information");
//            List<NameAssignment> nameAssignments = InputReader.readAssignments(inputAndOutputName.get(1));
//            List<Assignment> submittedAssignments = Assignment.from(nameAssignments, projects, contributors);
//            processAndValidateAssignments(submittedAssignments, contributors, projects);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static void checkIfAssignmentsAreValid(List<Assignment> assignments, List<Contributor> contributors, List<Project> projects) throws Exception {
        if (!Validator.areAssignmentsValid(assignments, contributors, projects)) {
            throw new Exception("Wrong solution");
        }
    }

    private static void printAssignmentsInformation(int fitnessScore, List<Assignment> assignments) {
        System.out.println("Fitness score: " + fitnessScore);
        System.out.println("Number of assignments: " + assignments.size());
    }

    private static void processAndValidateAssignments(List<Assignment> assignments, List<Contributor> contributors, List<Project> projects) throws Exception {
        checkIfAssignmentsAreValid(assignments, contributors, projects);
        printAssignmentsInformation(FitnessCalculator.getFitnessScore(assignments), assignments);
    }

    private static void applyHeuristic(List<Project> projects, List<Contributor> contributors, String fileName) {
        Collections.shuffle(contributors, new Random());

        if(Objects.equals(fileName, "c")) {
            System.out.println("C heuristic");
            projects.sort((p1, p2) -> {
                // Compare by score
                int scoreComparison = Integer.compare(p2.getScore(), p1.getScore());
                if (scoreComparison != 0) {
                    return scoreComparison;
                }
                // Scores are equal, compare by bestBefore
                return Integer.compare(p1.getBestBefore(), p2.getBestBefore());
            });
        }
        else if(Objects.equals(fileName, "e")) {
            System.out.println("E heuristic");
            projects.sort((b1, b2) -> Integer.compare(-b2.getBestBefore(), -b1.getBestBefore()));
        }
        else if(Objects.equals(fileName, "f")) {
            System.out.println("F heuristic");
            projects.sort(new InitialSolver.ProjectComparator());
        }
        else {
            System.out.println("No heuristic");
            Collections.shuffle(projects, new Random());
        }
    }
}