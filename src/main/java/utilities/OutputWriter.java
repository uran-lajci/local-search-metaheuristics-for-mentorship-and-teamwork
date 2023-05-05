package utilities;

import entities.FullAssignment;
import entities.Skill;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class OutputWriter {

    public static void writeContent(List<FullAssignment> assignments, String fileName) throws IOException {
        FileWriter writer = new FileWriter(fileName);
        writer.write(assignments.size() + "\n");

        for (FullAssignment assignment : assignments) {
            writer.write(assignment.getProject().getName() + "\n");

            List<FullAssignment.PrintingOrder> printingOrderList = assignment.getPrintingOrderForContributors();
            Map<UUID, FullAssignment.PrintingOrder> map = printingOrderList.stream().collect(Collectors.toMap(FullAssignment.PrintingOrder::getProjectSkillID, printingOrder -> printingOrder));

            for (Skill skill : assignment.getProject().getSkills()) {
                writer.write(map.get(skill.getId()).getContributor().getName() + " ");
            }

            writer.write("\n");
        }
        writer.close();

        System.out.println("Wrote assignments\n");
    }
}
