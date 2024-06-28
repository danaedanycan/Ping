package fr.epita.assistants.myide.domain.entity.Features.Git;

import fr.epita.assistants.myide.domain.entity.*;
import fr.epita.assistants.myide.domain.entity.Features.Feedback;
import fr.epita.assistants.myide.utils.Logger;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.*;

import java.io.*;
import java.nio.file.*;


abstract public class GitCommand implements Feature {
    public Mandatory.Features.Git gitCommand;

    protected GitCommand(Mandatory.Features.Git git) {
        this.gitCommand = git;
    }

    @Override
    public ExecutionReport execute(Project project, Object... params) {
        Feedback feedback = new Feedback();
        feedback.setValid(true);
        if (gitCommand != null) {
            try {
                if (gitCommand == Mandatory.Features.Git.ADD) {
                    feedback = gitAdd(project, params);
                }
                if (gitCommand == Mandatory.Features.Git.PULL) {
                    feedback = gitPull(project, params);
                }
                if (gitCommand == Mandatory.Features.Git.COMMIT) {
                    feedback = gitCommit(project, params);
                }
                if (gitCommand == Mandatory.Features.Git.PUSH) {
                    feedback = gitPush(project, params);
                }
            } catch (IOException e) {
                System.err.println("IOException" + e);
            } catch (GitAPIException e) {
                System.err.println("GitAPIException" + e);
            }
        }
        return feedback;
    }

    protected Feedback gitAdd(Project project, Object... params) throws IOException, GitAPIException {
        Feedback feedback = new Feedback();
        feedback.setValid(true);

        String root_node = project.getRootNode().getPath().toString();
        File folder = new File(root_node, ".git");

        try (org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(folder)) {
            Status status = git.status().call();
            AddCommand add = git.add();

            for (Object param : params) {
                String fileName = param.toString();
                boolean isModified = !status.getModified().isEmpty() && status.getModified().contains(fileName);
                boolean isChanged = !status.getChanged().isEmpty() && status.getChanged().contains(fileName);
                boolean isUntracked = !status.getUntracked().isEmpty() && status.getUntracked().contains(fileName);
                File file = new File(root_node, fileName);
                if (!file.exists()) {
                    Logger.logError("File " + fileName + " has not been found.");
                    feedback.setValid(false);
                    break;
                } else if (!isChanged && !isModified && !isUntracked) {
                    Logger.logError("File " + fileName + " cannot be added because it has not been updated since last commit.");
                    feedback.setValid(false);
                    continue;
                }
                else if (isUntracked) {
                    Logger.log("File " + fileName + " is currently untracked.");
                }
                else {
                    Logger.log("Modified file " + fileName + " will be added.");
                }
                add.addFilepattern(fileName);
            }

            if (feedback.isSuccess())
            {
                add.call();
            }
        } catch (Exception e) {
            Logger.logError("Repository " + folder.getAbsolutePath() + " not found.");
            Logger.logError(e.toString());
        }
        return feedback;
    }

    protected Feedback gitPull(Project project, Object... params) throws IOException, GitAPIException {
        Feedback feedback = new Feedback();
        feedback.setValid(true);

        Path directory_git = Paths.get(project.getRootNode().getPath().toString(), ".git");

        try (org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(directory_git.toFile())) {
            PullCommand pullCommand = git.pull();

            PullResult result = pullCommand.call();


            if (result.isSuccessful()) {
                Logger.log("pull works");
            } else {
                Logger.log("pull does not work");
                feedback.setValid(false);
            }
        } catch(Exception e) {
            Logger.logError("Repository " + directory_git + " cannot be openedNo such repository found.");
            feedback.setValid(false);
        }
        return feedback;
    }


    protected Feedback gitPush(Project project, Object... params) throws IOException, GitAPIException {
        Feedback feedback = new Feedback();
        feedback.setValid(true);

        Path path_to_git_at_root = Paths.get(project.getRootNode().getPath().toString(), ".git");

        try (org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(path_to_git_at_root.toFile())) {
            PushCommand push = git.push();
            try {
                Iterable<PushResult> feedback_push = push.call();
                for (PushResult result : feedback_push) {
                    if (!result.getMessages().isEmpty() && result.getMessages().contains("rejected")) {
                        Logger.logError("Push was unsuccessful");
                        Logger.logError(result.getMessages());
                        feedback.setValid(false);
                    }
                }
            } catch (Exception e) {
                Logger.logError("Cannot push to remoteAccess denied.");
                Logger.logError(e.toString());
                feedback.setValid(false);
            }
        } catch(IOException e) {
            Logger.logError("Repository with path " + path_to_git_at_root + " not found.");
            feedback.setValid(false);
        }
        return feedback;
    }

    protected Feedback gitCommit(Project project, Object... params) throws IOException, GitAPIException {
        Feedback feedback = new Feedback();
        feedback.setValid(true);

        org.eclipse.jgit.api.Git git;
        try {
            git = org.eclipse.jgit.api.Git.open(Paths.get(project.getRootNode().getPath().toString(), ".git").toFile());
        } catch (Exception e) {
            Logger.logError("Could not open");
            Logger.logError(e.toString());
            feedback.setValid(false);
            return feedback;
        }
        CommitCommand commit = git.commit();
        StringBuilder builder = new StringBuilder();
        for (Object param : params) {
            builder.append(param.toString());
        }
        String commit_message = builder.toString();
        commit.setMessage(commit_message);
        try{
            commit.call();
        } catch (Exception e) {
            Logger.logError("Commit failed.");
            Logger.logError(e.toString());
            feedback.setValid(false);
        }
        return feedback;
    }

    @Override
    public Type type() {
        return gitCommand;
    }
}
