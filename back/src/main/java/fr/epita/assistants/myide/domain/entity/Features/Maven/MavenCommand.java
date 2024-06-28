package fr.epita.assistants.myide.domain.entity.Features.Maven;

import fr.epita.assistants.myide.domain.entity.Feature;
import fr.epita.assistants.myide.domain.entity.Features.Feedback;
import fr.epita.assistants.myide.domain.entity.Mandatory;
import fr.epita.assistants.myide.domain.entity.Project;
import fr.epita.assistants.myide.utils.Logger;
import io.smallrye.common.constraint.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

abstract public class MavenCommand implements Feature {

    private Mandatory.Features.Maven mavenCommand;

    protected MavenCommand(Mandatory.Features.Maven mavenCommand) {
        this.mavenCommand = mavenCommand;
    }

    @NotNull
    private String commandToString(Mandatory.Features.Maven command) {
        String cmd = switch (command) {
            case COMPILE -> "compile";
            case EXEC -> "exec:java";
            case TEST -> "test";
            case TREE -> "dependency:tree";
            case CLEAN -> "clean";
            case INSTALL -> "install";
            case PACKAGE -> "package";
        };
        return cmd;
    }

    @Override
    public ExecutionReport execute(Project project, Object... params) {
        Feedback executionStatus = new Feedback();
        executionStatus.setValid(true);
        Path rootPath;
        try {
            rootPath = project.getRootNode().getPath().toAbsolutePath().toRealPath();
        } catch (IOException e) {
            executionStatus.setValid(false);
            return executionStatus;
        }
        List<String> mvnParams = new ArrayList<>();
        String commandToString = commandToString(this.mavenCommand);
        mvnParams.add("mvn");
        mvnParams.add(commandToString);
        for (Object parameter : params) {
            mvnParams.add(parameter.toString());
        }
        ProcessBuilder mvnInstruction = new ProcessBuilder(mvnParams);
        mvnInstruction.directory(rootPath.toFile());
        try {
            Process p = mvnInstruction.start();
            p.waitFor();
            executionStatus.setValid(p.exitValue() == 0);
            Logger.log("Maven command " + "mvn " + commandToString + " has been successfully executed.");
            Logger.log("The exit value is " + p.exitValue());
        } catch (IOException e) {
            Logger.logError("No project with name " + rootPath + " found.");
            executionStatus.setValid(false);
        } catch (InterruptedException e) {
            Logger.logError("Process " + mvnParams + " was interrupted and did not terminate.");
            executionStatus.setValid(false);
        }
        return executionStatus;
    }

    @Override
    public Type type() {
        return mavenCommand;
    }
}
