package utilities;

import entities.Contributor;
import entities.FullAssignment;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class OutputWriter {

    public static void writeContent(List<FullAssignment> assignments, String fileName) throws IOException {
        FileWriter writer = new FileWriter(fileName);
        writer.write(assignments.size() + "\n");

        for (FullAssignment assignment : assignments) {
            writer.write(assignment.getProject().getName() + "\n");
            for (Contributor contributor : assignment.getContributors()) {
                writer.write(contributor.getName() + " ");
            }
            writer.write("\n");
        }
        writer.close();

        System.out.println("Wrote assignments\n");
    }
}
