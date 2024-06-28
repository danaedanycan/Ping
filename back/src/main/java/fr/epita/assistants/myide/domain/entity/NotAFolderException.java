package fr.epita.assistants.myide.domain.entity;

import fr.epita.assistants.myide.domain.entity.Nodes.FolderNode;
import fr.epita.assistants.myide.domain.entity.Nodes.NotAFolderExceptionProblem;
import fr.epita.assistants.myide.utils.Logger;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.nio.file.Path;

public final class NotAFolderException extends Exception {

    public NotAFolderException(@NotNull File file, @NotNull NotAFolderExceptionProblem problem, FolderNode emitter) {
        super(generateErrorMessage(file, problem, emitter));
        Logger.logError(generateErrorMessage(file, problem, emitter));
    }

    private static @NotNull String generateErrorMessage(@NotNull File file, @NotNull NotAFolderExceptionProblem problem, FolderNode emitter) {
        String baseMessage;
        if (problem == NotAFolderExceptionProblem.EXISTENCE) {
            baseMessage = "ERROR: " + Path.of(file.getAbsolutePath()) + ": does not exist as a directory entry in your system";
        } else baseMessage = "ERROR: " + Path.of(file.getAbsolutePath()) + ": exists but is not a folder";

        if (emitter == null) {
            return baseMessage;
        }

        return baseMessage + " (emitted from the " + emitter.getActualRespectablePath() + "'s child node builder";
    }
}
