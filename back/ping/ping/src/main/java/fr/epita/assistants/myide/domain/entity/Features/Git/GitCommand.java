package fr.epita.assistants.myide.domain.entity.Features.Git;

import fr.epita.assistants.myide.domain.entity.*;
import fr.epita.assistants.myide.domain.entity.Features.Feedback;
import fr.epita.assistants.myide.domain.entity.classes.Credentials;
import fr.epita.assistants.myide.utils.Logger;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.*;

import java.io.*;
import java.nio.file.*;
import java.util.Set;

abstract public class GitCommand implements Feature {
    public Mandatory.Features.Git gitCommand;
    protected Credentials cred = new Credentials();

    protected GitCommand(Mandatory.Features.Git git) {
        this.gitCommand = git;
    }

    @Override
    public ExecutionReport execute(Project project, Object... params) {
        Feedback feedback = new Feedback();
        feedback.setValid(true);

        if (gitCommand != null && cred.readFromFile("src/main/resources/credentials.txt")) {
            try {
                switch (gitCommand) {
                    case ADD:
                        feedback = gitAdd(project, params);
                        break;
                    case PULL:
                        feedback = gitPull(project, params);
                        break;
                    case COMMIT:
                        feedback = gitCommit(project, params);
                        break;
                    case PUSH:
                        feedback = gitPush(project, params);
                        break;
                    case STATUS:
                        feedback = gitStatus(project);
                        break;
                    case TAG:
                        feedback = gitTag(project);
                        break;
                }
            } catch (IOException e) {
                System.err.println("IOException" + e);
            } catch (GitAPIException e) {
                System.err.println("GitAPIException" + e);
            }
        }
        return feedback;
    }


    protected Feedback gitStatus(Project project) {
        Feedback feedback = new Feedback();
        String root_node = project.getRootNode().getPath().toString();
        File folder = new File(root_node, ".git");
        try (org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(folder)) {
            Status status = git.status().call();

            Set<String> untrac = feedback.get_untracted();
            untrac.addAll(status.getModified());
            untrac.addAll(status.getUntracked());
            feedback.set_untracted(untrac);
        } catch (IOException | GitAPIException e) {
            return null;
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
                } else if (isUntracked) {
                    Logger.log("File " + fileName + " is currently untracked.");
                } else {
                    Logger.log("Modified file " + fileName + " will be added.");
                }
                add.addFilepattern(fileName);
            }

            if (feedback.isSuccess()) {
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
            pullCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(cred.getUsername(), cred.getKey()));

            PullResult result = pullCommand.call();
            if (result.isSuccessful()) {
                Logger.log("Pull works");
            } else {
                Logger.log("Pull does not work");
                feedback.setValid(false);
            }
        } catch (Exception e) {
            Logger.logError("Repository " + directory_git + " cannot be opened. No such repository found.");
            feedback.setValid(false);
        }
        return feedback;
    }

    public Feedback gitPush(Project project, Object... params) throws IOException, GitAPIException {
        Feedback feedback = new Feedback();
        feedback.setValid(true);

        Path pathToGitAtRoot = Paths.get(project.getRootNode().getPath().toString(), ".git");

        try (Git git = Git.open(pathToGitAtRoot.toFile())) {
            Logger.log("user:"+cred.getUsername()+"\nmdp:"+cred.getKey());
            PushCommand push = git.push();
            push.setCredentialsProvider(new UsernamePasswordCredentialsProvider(cred.getUsername(), cred.getKey()));

            try {
                Iterable<PushResult> feedbackPush = push.call();
                for (PushResult result : feedbackPush) {
                    String messages = result.getMessages();
                    if (messages != null && !messages.isEmpty()) {
                        if (messages.contains("rejected")) {
                            Logger.logError("Push was unsuccessful");
                            Logger.logError(messages);
                            feedback.setValid(false);
                        } else {
                            Logger.log("Push result messages: " + messages);
                        }
                    }
                }
            } catch (GitAPIException e) {
                Logger.logError("Cannot push to remote. Access denied. ");
                Logger.logError(e.toString());
                feedback.setValid(false);
            }
        } catch (IOException e) {
            Logger.logError("Repository with path " + pathToGitAtRoot + " not found.");
            Logger.logError(e.toString());
            feedback.setValid(false);
        }
        return feedback;
    }
    private Feedback gitTag(Project project, Object... params) {
        Feedback feedback = new Feedback();
        feedback.setValid(true);

        org.eclipse.jgit.api.Git git;
        try {
            git = org.eclipse.jgit.api.Git.open(Paths.get(project.getRootNode().getPath().toString(), ".git").toFile());
        } catch (Exception e) {
            Logger.logError("Could not open repository.");
            Logger.logError(e.toString());
            feedback.setValid(false);
            return feedback;
        }

        TagCommand tagCommand = git.tag();
        StringBuilder builder = new StringBuilder();
        for (Object param : params) {
            builder.append(param.toString()).append(" ");
        }
        String tagName = builder.toString().trim();

        if (tagName.isEmpty()) {
            Logger.logError("Tag name is empty.");
            feedback.setValid(false);
            return feedback;
        }

        tagCommand.setName(tagName);
        try {
            tagCommand.call();
        } catch (GitAPIException e) {
            Logger.logError("Tagging failed.");
            Logger.logError(e.toString());
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
        try {
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
