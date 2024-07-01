package fr.epita.assistants.myide.domain.service.Git;

public class CommitClass {
    private String current_project;
    private String commit;

    // Getters and setters
    public String getCurrent_project() {
        return current_project;
    }

    public void setCurrent_project(String current_project) {
        this.current_project = current_project;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }
}
