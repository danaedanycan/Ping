package fr.epita.assistants.myide.domain.entity.Nodes;

import fr.epita.assistants.myide.domain.entity.Node;
import fr.epita.assistants.myide.domain.entity.NotAFileException;
import fr.epita.assistants.myide.domain.entity.NotAFolderException;
import fr.epita.assistants.myide.utils.Logger;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class FileNode implements Node {

    private FolderNode parent;

    private final File file;

    private Path yourShityPath;

    public FileNode(Path path) throws InvalidPathException, NotAFileException, NotAFolderException {
        this(String.valueOf(path), null, null);
    }
    public void setPath(Path path){
        this.yourShityPath =path;
    }

    FileNode(@NotNull String path, FolderNode parent, FolderNode emitter) throws InvalidPathException,
            NotAFileException, NotAFolderException {
        File fileEntity = new File(path);

        if (!fileEntity.exists()) {
            throw new NotAFileException(fileEntity, NotAFileExceptionProblem.EXISTENCE, emitter);
        } else if (!fileEntity.isFile()) {
            throw new NotAFileException(fileEntity, NotAFileExceptionProblem.NOT_A_FILE, emitter);
        }

        this.file = fileEntity;
        if (parent == null) {
            this.parent = new FolderNode(Path.of(fileEntity.getParent()));
        } else {
            this.parent = parent;
        }

        yourShityPath = Path.of(constructShityPath(parent, false));
    }

    public String constructShityPath(FolderNode parent, boolean recursiveCall) {
        if (parent == null) {
            if (recursiveCall) {
                return ".";
            }
            return file.toPath().toString();
        }

        return parent.constructShityPath(parent.getParent(), true) + File.separatorChar + file.getName();
    }

    @Override
    public Path getPath() {
        return yourShityPath;
    }

    public static FileNode loadOrCreate(Path fullPath, FolderNode parent) {
        Path absolutePath;
        absolutePath = fullPath;

        File fileObject = absolutePath.toFile();
        if (fileObject.isFile()) {
            try {
                return new FileNode(absolutePath.toString(), parent, null);
            } catch (NotAFileException | NotAFolderException notAFileSystemEntryException) {
                Logger.logError("Tried to create a node from a path which seemed to exist right (path: \"" + absolutePath + "\") before the call to " +
                        "the file node constructor but then the following exception occurred: " + notAFileSystemEntryException.getMessage() +
                        ".The operation will be aborted and null will be returned.");
                return null;
            }
        }

        boolean succeeded;
        try {
            File parentObject = Path.of(fileObject.getParent()).toFile();
            if (!parentObject.exists())
                succeeded = parentObject.mkdirs();
            else
                succeeded = true;
        } catch (SecurityException securityException) {
            Logger.logError("Tried creating the necessary folders for the file node \"" + absolutePath + "\" but " +
                    "the following exception occurred: " + securityException.getMessage() +
                    "The operation will be aborted and null will be returned.");
            return null;
        }

        if (!succeeded) {
            Logger.logError("Tried creating the necessary folders for the file node \"" + absolutePath + "\" but " +
                    "the operation for some reason did not succeedThe operation will be aborted and null will be " +
                    "returned.");
            return null;
        }

        if (!fileObject.exists()) {
            try {
                succeeded = fileObject.createNewFile();
            } catch (IOException ioException) {
                Logger.logError("Tried creating the file \"" + fileObject.getName() + "\" but an error happened: " + ioException.getMessage());
                return null;
            }
        }

        if (!succeeded) {
            Logger.logError("Tried creating the file \"" + fileObject.getName() + "\" but it did not seem to work for some unknown reason");
            return null;
        }


        try {
            return new FileNode(absolutePath.toString(), parent, null);
        } catch (NotAFileException | NotAFolderException notAFileSystemEntryException) {
            Logger.logError("Tried to create a node from a path which seemed to exist right (path: \"" + absolutePath + "\") before the call to " +
                    "the file node constructor but then the following exception occurred: " + notAFileSystemEntryException.getMessage() +
                    "The operation will be aborted and null will be returnedNote that the folders did not exist but" +
                    " were successfully created.");
            return null;
        }
    }

    /**
     * @return The Node path.
     */
    public Path getActualRespectablePath() {
        return Path.of(file.getAbsolutePath());
    }

    public String getName() {
        return file.getName();
    }

    public boolean hasParent() {
        return parent != null;
    }

    public FolderNode getParent() {
        return parent;
    }

    void setParent(FolderNode parent) {
        this.parent = parent;
    }

    /**
     * @return The Node type.
     */
    @Override
    public Type getType() {
        return Types.FILE;
    }

    /**
     * If the Node is a Folder, returns a list of its children,
     * else returns an empty list.
     *
     * @return List of node
     */
    @Override
    public List<@NotNull Node> getChildren() {
        return List.of();
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileNode fileNode = (FileNode) o;
        return Objects.equals(file, fileNode.file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    public boolean delete() {
        if (!file.exists()) {
            Logger.logError("Cannot delete a file which does not exist.");
            return false;
        }

        boolean deleted = file.delete();
        if (!deleted) {
            Logger.logError("Tried to delete the file but it did not work");
            return false;
        }

        if (parent != null) {
            parent.removeFile(this);
        }

        return true;
    }

    public void setShityPath(Path shityPath) {
        yourShityPath = shityPath;
    }
}
