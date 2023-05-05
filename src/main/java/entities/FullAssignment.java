package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FullAssignment {
    private UUID id;
    private Project project;
    private List<Contributor> contributors;
    private List<PrintingOrder> printingOrderForContributors;
    private List<UUID> contributorIds;
    private List<UUID> skillIds;

    public FullAssignment() {
    }

    public FullAssignment(UUID id, Project project, List<Contributor> contributors) {
        this.id = id;
        this.project = project;
        this.contributors = contributors;
    }

    public List<UUID> getContributorIds() {
        return contributorIds;
    }

    public void setContributorIds(List<UUID> contributorIds) {
        this.contributorIds = contributorIds;
    }

    public List<UUID> getSkillIds() {
        return skillIds;
    }

    public void setSkillIds(List<UUID> skillIds) {
        this.skillIds = skillIds;
    }

    public List<PrintingOrder> getPrintingOrderForContributors() {
        return printingOrderForContributors;
    }

    public void setPrintingOrderForContributors(List<PrintingOrder> printingOrderForContributors) {
        this.printingOrderForContributors = printingOrderForContributors;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Contributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<Contributor> contributors) {
        this.contributors = contributors;
    }

    public List<String> getContributorNames() {
        return contributors.stream().map(Contributor::getName).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "FullAssignment{" +
                "id=" + id +
                ", project=" + project +
                ", contributors=" + contributors +
                '}';
    }

    public FullAssignment deepCopy() {
        FullAssignment fullAssignment = new FullAssignment();
        fullAssignment.setId(id);
        fullAssignment.setProject(project);

        List<Contributor> copiedContributors = new ArrayList<>();
        for (Contributor contributor : contributors) {
            copiedContributors.add(contributor.deepCopy());
        }
        fullAssignment.setContributors(copiedContributors);
        return fullAssignment;
    }

    public static class PrintingOrder {
        private UUID projectSkillID;
        private Contributor contributor;

        public PrintingOrder() {
        }

        public PrintingOrder(UUID projectSkillID, Contributor contributor) {
            this.projectSkillID = projectSkillID;
            this.contributor = contributor;
        }

        public UUID getProjectSkillID() {
            return projectSkillID;
        }

        public void setProjectSkillID(UUID projectSkillID) {
            this.projectSkillID = projectSkillID;
        }

        public Contributor getContributor() {
            return contributor;
        }

        public void setContributor(Contributor contributor) {
            this.contributor = contributor;
        }

        @Override
        public String toString() {
            return "PrintingOrder{" +
                    "projectSkillID=" + projectSkillID +
                    ", contributor=" + contributor.getName() +
                    '}';
        }
    }
}
