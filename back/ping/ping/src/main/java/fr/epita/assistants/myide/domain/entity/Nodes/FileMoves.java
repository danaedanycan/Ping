package fr.epita.assistants.myide.domain.entity.Nodes;

import fr.epita.assistants.myide.utils.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class FileMoves {
    static boolean movePaths(Path source, Path destination) {
        try {
            Files.move(source, destination);
        } catch (IOException ioException) {
            Logger.logError("ERROR: IO exception when moving file from path \"" + source +
                    "\" to path \"" + destination + "\", the following io exception occurred with the message \"" + ioException.getMessage() +
                    "\"The caller has canceled thus cancelled their operation.");
            return false;
        }

        return true;
    }

    private FileMoves() {

    }
}
