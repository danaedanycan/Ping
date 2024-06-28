package fr.epita.assistants.myide.domain.entity.Nodes;

import fr.epita.assistants.myide.domain.entity.*;
import fr.epita.assistants.myide.utils.Logger;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public final class FolderNode implements Node {

    private final File path;

    private Path yourShityPath;

    private final Set<FileNode> fileChildren;

    private final Set<FolderNode> folderChildren;

    private final Map<FileNode, FolderNode> childEntries;

    private FolderNode parent;

    public FolderNode(Path path) throws InvalidPathException, NotAFileException, NotAFolderException {
        this(String.valueOf(path), null, null);
    }
    public FolderNode(@NotNull String path, FolderNode parent, FolderNode emitter) throws InvalidPathException,
            NotAFileException, NotAFolderException {
        File folder = new File(path);

        if (!folder.isDirectory()) {
            NotAFolderExceptionProblem problem = NotAFolderExceptionProblem.NOT_A_FOLDER;
            if (!folder.exists()) {
                problem = NotAFolderExceptionProblem.EXISTENCE;
            }

            throw new NotAFolderException(folder, problem, emitter);
        }

        fileChildren = new HashSet<>();
        folderChildren = new HashSet<>();
        childEntries = new HashMap<>();

        this.path = folder;
        this.parent = parent;
        yourShityPath = Path.of(constructShityPath(parent, false));
        setupChildren(folder, emitter);
    }

    String constructShityPath(FolderNode parent, boolean recursiveCall) {
        if (parent == null) {
            if (recursiveCall) {
                return getName();
            }
            return path.toPath().toString();
        }

        return parent.constructShityPath(parent.getParent(), true) + File.separatorChar + path.getName();
    }

    public String getName() {
        return path.getName();
    }
    public void setPath(Path path){
        this.yourShityPath = path;
    }

    public FolderNode getParent() {
        return parent;
    }

    private void setParent(FolderNode parent) {
        this.parent = parent;
    }

    @Override
    public Path getPath() {
        return yourShityPath;
    }

    /**
     * @return The Node path.
     */
    public Path getActualRespectablePath() {
        return path.toPath();
    }

    /**
     * @return The Node type.
     */
    @Override
    public Type getType() {
        return Types.FOLDER;
    }

    /**
     * If the Node is a Folder, returns a list of its children,
     * else returns an empty list.
     *
     * @return List of node
     */
    @Override
    public List<@NotNull Node> getChildren() {
        List<@NotNull Node> children = new ArrayList<>(fileChildren);
        children.addAll(folderChildren);
        return children;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    public boolean addFile(FileNode file) {
        return addFile(file, false, true);
    }

    public boolean addFile(FileNode file, boolean overwriteIfPresent, boolean createRequiredSubDirectories) {
        if (!overwriteIfPresent && fileChildren.contains(file)) {
            Logger.log("Attempted to add a file \"" + file.getName() + "\" entry which already exists within the " +
                    "folder \"" + file.getActualRespectablePath()+ "\"" +
                    "The operation was thus cancelled.");
            return false;
        }

        if (folderChildren.stream().anyMatch(folder -> Objects.equals(folder.getName(), file.getName()))) {
            Logger.logError("Attempted to add a file \"" + file.getName() + "\" entry within the folder \"" + file.getActualRespectablePath()+ "\"." +
                    "which already contains a folder with that nameThe operation was thus cancelled.");
            return false;
        }

        Path filePath = file.getActualRespectablePath();
        File fileFromPath = filePath.toFile();
        if (!fileFromPath.exists()) {
            return createNonExistentFileSystemEntry(filePath, overwriteIfPresent, createRequiredSubDirectories,
                    Types.FILE);
        }

        if (fileFromPath.isDirectory()) {
            try {
                return addFolder(new FolderNode(Path.of(filePath.toString())), overwriteIfPresent, createRequiredSubDirectories);
            } catch (InvalidPathException invalidPathException) {
                Logger.logError("Can not create a file from a malformed path: \"" + invalidPathException.getMessage() + "\"");
                return false;
            } catch (NotAFileException | NotAFolderException notAFileSystemElementException) {
                Logger.logError("Attempted to create a folder-node from a path confirmed to be moments ago a " +
                        "directory and existing." +
                        " It seems though an issue occurred with the following exception: \"" + notAFileSystemElementException.getMessage() + "\"." +
                        "The operation will be aborted");
                return false;
            }
        }

        String absolutePath = path.getAbsolutePath();
        String newFilePath = absolutePath + File.separatorChar + file.getName();

        if (!FileMoves.movePaths(filePath, Path.of(newFilePath))) {
            return false;
        }

        addFileEntry(file);
        return true;
    }

    private boolean createNonExistentFileSystemEntry(Path filePath, boolean overwriteIfPresent,
                                                     boolean createRequiredSubDirectories,
                                                     Type nodeType) {
        if (!isSubPath(filePath)) {
            Logger.logError("Attempted to add \"" + filePath+ "\" to the folder " +
                    "\"" + path + "\"." +
                    " The file entry does not exist and is not a sub-path of the present folder, therefore");
            return false;
        }

        if (isImmediateChild(filePath)) {
            if (nodeType == Types.FILE) {
                return createFileEntry(filePath, overwriteIfPresent, createRequiredSubDirectories);
            } else {
                return createFolderEntry(filePath, overwriteIfPresent, createRequiredSubDirectories);
            }
        }

        if (!createRequiredSubDirectories) {
            Logger.logError("Attempted to add \"" + filePath+ "\" to the folder " +
                    "\"" + path + "\"." +
                    " The file entry does not exist but is marked in a non-existent subdirectory that is not it " +
                    "is not marked as an immediate child of this directory." +
                    " The function parameter createRequiredSubDirectories is set to false therefore this " +
                    "operation will not happen and be aborted.");
            return false;
        }

        if (nodeType == Types.FILE) {
            return createFileEntry(filePath, overwriteIfPresent, true);
        } else {
            return createFolderEntry(filePath, overwriteIfPresent, true);
        }
    }

    private void addFileEntry(FileNode file) {
        file.setParent(this);
        file.setShityPath(Path.of(file.constructShityPath(this, false)));
        fileChildren.add(file);
        addToChildEntries(file);
    }

    private void addFolderEntry(FolderNode folder) {
        folder.yourShityPath = Path.of(constructShityPath(parent, false));
        folder.setParent(this);
        folderChildren.add(folder);
    }

    public boolean addFolder(FolderNode folder, boolean overwriteIfPresent, boolean createRequiredSubDirectories) {
        if (!overwriteIfPresent && folderChildren.contains(folder)) {
            Logger.log("Attempted to add a folder \"" + folder.getName() + "\" entry which already exists within a " +
                    "folder \"" + getName() + "\"." +
                    "The operation was thus cancelled.");
            return false;
        }

        if (fileChildren.stream().anyMatch(file -> Objects.equals(folder.getName(), getName()))) {
            Logger.logError("Attempted to add a folder \"" + folder.getName() + "\" entry within a folder \"" + getName() + "\" " +
                    "which already contains a file with that nameThe operation was thus cancelled.");
            return false;
        }

        Path folderPath = folder.getActualRespectablePath();
        File folderFromPath = folderPath.toFile();
        if (!folderFromPath.exists()) {
            return createNonExistentFileSystemEntry(folderPath, overwriteIfPresent, createRequiredSubDirectories,
                    Types.FOLDER);
        }

        if (folderFromPath.isFile()) {
            try {
                return addFile(new FileNode(Path.of(folderPath.toString())), overwriteIfPresent, createRequiredSubDirectories);
            } catch (InvalidPathException invalidPathException) {
                Logger.logError("Can not create a folder from a malformed path: \"" + invalidPathException.getMessage() + "\"");
                return false;
            } catch (NotAFileException | NotAFolderException notAFileSystemElementException) {
                Logger.logError("Attempted to create a file-node from a path confirmed to be moments ago a file and " +
                        "existing." +
                        " It seems though an issue occurred with the following exception: \"" + notAFileSystemElementException.getMessage() + "\"." +
                        "The operation will be aborted");
                return false;
            }
        }

        String absolutePath = path.getAbsolutePath();
        String newFolderPath = absolutePath + File.separatorChar + folder.getName();

        if (!FileMoves.movePaths(folderPath, Path.of(newFolderPath))) {
            return false;
        }

        addFolderEntry(folder);
        return true;
    }

    public boolean createFileEntry(Path path, boolean overwriteIfPresent, boolean createRequiredSubDirectories) {
        File file = path.toFile();

        if (!overwriteIfPresent && fileChildren.stream().anyMatch(fileEntry -> Objects.equals(fileEntry.getName(),
                file.getName()))) {
            Logger.log("Attempted to create the file \"" + file.getName() + "\" which already exists within a folder " +
                    "\"" + getName() + "\"" +
                    "The operation was thus cancelled since overwriteIfPresent method option is set to false.");
            return false;
        }

        if (folderChildren.stream().anyMatch(folder -> Objects.equals(folder.getName(), file.getName()))) {
            Logger.logError("Attempted to create a file \"" + file.getName() + "\" within a folder \"" + getName() +
                    "\" " +
                    "which already contains a folder with that nameThe operation was thus cancelled.");
            return false;
        }

        if (!isSubPath(path)) {
            Logger.logError("Attempting to create the file \"" + path+ "\"" +
                    " The file entry does not exist and is not a sub-path of the present folder, therefore");
        }

        if (!createRequiredSubDirectories && !isImmediateChild(path)) {
            Logger.logError("Attempted to create the file \"" + path+ "\" in the " +
                    "folder \"" + path + "\"." +
                    " The file entry does not exist but is marked in a non-existent subdirectory that is not it " +
                    "is not marked as an immediate child of this directory." +
                    " The function parameter createRequiredSubDirectories is set to false therefore this " +
                    "operation will not happen and be aborted.");
            return false;
        }

        boolean alreadyExisted;
        try {
            alreadyExisted = file.createNewFile();
        } catch (SecurityException securityException) {
            Logger.logError("ERROR: IO exception when creating file \"" + path +
                    "\", the following security exception occurred with the message \"" + securityException.getMessage() +
                    "\"The caller has canceled thus cancelled their operation.");
            return false;
        } catch (IOException ioException) {
            Logger.logError("ERROR: IO exception when creating file \"" + path +
                    "\", the following io exception occurred with the message \"" + ioException.getMessage() +
                    "\"The caller has canceled thus cancelled their operation.");
            return false;
        }

        if (!alreadyExisted && !overwriteIfPresent) {
            Logger.log("Attempted to create the file \"" + file.getName() + "\" which already exists within a folder " +
                    "\"" + getName() + "\"" +
                    "The operation was thus cancelled since overwriteIfPresent method option is set to false" +
                    "This message is thrown if the check at the beginning of the method passes but the file is " +
                    "created in the mean time" +
                    " and the file is found when actually creating the file.");
            return false;
        }

        FileNode node;
        try {
            node = new FileNode(Path.of(path.toString()));
        } catch (InvalidPathException invalidPathException) {
            Logger.logError("Can not create a file from a malformed path: \"" + invalidPathException.getMessage() +
                    "\"");
            return false;
        } catch (NotAFileException | NotAFolderException notAFileSystemElementException) {
            Logger.logError("Attempted to create a file from a path confirmed to be moments ago a file and existing." +
                    " It seems though an issue occurred with the following exception: \"" + notAFileSystemElementException.getMessage() + "\"." +
                    "The operation will be aborted");
            return false;
        }

        addFileEntry(node);
        return true;
    }

    public boolean createFolderEntry(Path path, boolean overwriteIfPresent, boolean createRequiredSubDirectories) {
        File file = path.toFile();

        if (!overwriteIfPresent && folderChildren.stream().anyMatch(fileEntry -> Objects.equals(fileEntry.getName(),
                file.getName()))) {
            Logger.log("Attempted to create the folder \"" + file.getName() + "\" which already exists within a " +
                    "folder \"" + getName() + "\"" +
                    "The operation was thus cancelled since overwriteIfPresent method option is set to false.");
            return false;
        }

        if (fileChildren.stream().anyMatch(folder -> Objects.equals(folder.getName(), file.getName()))) {
            Logger.logError("Attempted to create a folder \"" + file.getName() + "\" within a folder \"" + getName() + "\" " +
                    "which already contains a folder with that nameThe operation was thus cancelled.");
            return false;
        }

        if (!isSubPath(path)) {
            Logger.logError("Attempting to create the folder \"" + path+ "\"" +
                    " The folder entry does not exist and is not a sub-path of the present folder, therefore");
        }

        if (!createRequiredSubDirectories && !isImmediateChild(path)) {
            Logger.logError("Attempted to create the folder \"" + path+ "\" in the" +
                    " folder \"" + path + "\"." +
                    " The folder entry does not exist but is marked in a non-existent subdirectory that is not it" +
                    " is not marked as an immediate child of this directory." +
                    " The function parameter createRequiredSubDirectories is set to false therefore this " +
                    "operation will not happen and be aborted.");
            return false;
        }

        boolean alreadyExisted;
        try {
            File parent = Path.of(file.getParent()).toFile();
            if (!parent.exists())
                alreadyExisted = parent.mkdirs();
            else
                alreadyExisted = true;
        } catch (SecurityException securityException) {
            Logger.logError("ERROR: IO exception when creating folder \"" + path +
                    "\", the following security exception occurred with the message \"" + securityException.getMessage() +
                    "\"The caller has canceled thus cancelled their operation.");
            return false;
        }

        if (!alreadyExisted && !overwriteIfPresent) {
            Logger.log("Attempted to create the folder \"" + file.getName() + "\" which already exists within a " +
                    "folder \"" + getName() + "\"" +
                    "The operation was thus cancelled since overwriteIfPresent method option is set to false" +
                    "This message is thrown if the check at the beginning of the method passes but the folder is " +
                    "created in the mean time" +
                    " and the folder is found when actually creating the file.");
            return false;
        }

        FolderNode followupNode;

        String followupElement = getFollowupElement(file.toPath());
        if (followupElement == null) {
            Logger.logError("Since the followup element could not be fetched, aborting the folder creation " +
                    "operation for \"" + path+
                    "\" (entry may be created in file system but not in IDE data structures).");
            return false;
        }

        String followupPath = path + File.pathSeparator + followupElement;

        try {
            followupNode = new FolderNode(followupPath, this, this);

        } catch (InvalidPathException invalidPathException) {
            Logger.logError("Can not create a folder from a malformed path: \"" + invalidPathException.getMessage() + "\"");
            return false;
        } catch (NotAFileException | NotAFolderException notAFileSystemElementException) {
            Logger.logError("Attempted to create a file from a path confirmed to be moments ago a file and existing." +
                    " It seems though an issue occurred with the following exception: \"" + notAFileSystemElementException.getMessage() + "\"." +
                    "The operation will be aborted");
            return false;
        }

        addFolderEntry(followupNode);
        return true;
    }

    private void setupChildren(File path, FolderNode emitter) throws NotAFileException, NotAFolderException {
        String[] immediateChildren = path.list();

        if (immediateChildren == null) {
            return;
        }

        for (String immediateChild : immediateChildren) {
            String fullPath = path.getAbsolutePath() + File.separatorChar + immediateChild;
            File child = new File(fullPath);
            if (child.isDirectory()) {
                folderChildren.add(new FolderNode(fullPath, this, emitter));
                continue;
            }

            FileNode childNode;
            try {
                childNode = new FileNode(fullPath, this, emitter);
                addFileEntry(childNode);
            } catch (InvalidPathException invalidPathException) {
                Logger.logError("Skipping addition of invalid path entry when building children " +
                        "from a malformed path: \"" + invalidPathException.getMessage() + "\"");

            }
        }
    }

    private void addToChildEntries(FileNode child) {
        childEntries.put(child, this);
        if (parent != null) {
            parent.addToChildEntries(child);
        }
    }

    private boolean isSubPath(Path path) {
        return path.startsWith(this.path.toPath());
    }

    private boolean isImmediateChild(Path child) {
        if (!isSubPath(child)) {
            return false;
        }

        String childPath = child.toString();
        int index = childPath.lastIndexOf(File.pathSeparator);

        int parentPathStringLength = this.path.toString().length();
        return index == parentPathStringLength;
    }

    private String getFollowupElement(Path path) {
        List<String> pathElements = getPathElements(path);

        List<String> basePathElements = getPathElements(this.path.toPath());

        int numberOfBasePathElements = basePathElements.size();
        int numberOfPathElements = pathElements.size();

        if (numberOfPathElements > numberOfBasePathElements) {
            return pathElements.get(numberOfBasePathElements);
        }

        Logger.logError("tried to get followup path element for path \"" + path+
                "\" in path \"" +
                this.path.getAbsolutePath() + "\" but there isn't a followup element" +
                " (the number of path elements is less or equal than that of the base path).");

        return null;
    }

    private List<String> getPathElements(Path path) {
        Path absolutePath;
        absolutePath = path;
        String absolutePathString = absolutePath.toString();
        int absolutePathStringLength = absolutePathString.length();

        List<String> pathElements = computePathElements(absolutePathStringLength, absolutePathString);

        return pathElements;
    }

    private static List<String> computePathElements(int absolutePathStringLength, String absolutePathString) {
        StringBuilder elementBuilder = new StringBuilder();
        List<String> pathElements = new ArrayList<>();

        int index = 0;
        while (index < absolutePathStringLength) {
            char character = absolutePathString.charAt(index);
            if (Character.toString(character).equals(File.pathSeparator)) {
                pathElements.add(elementBuilder.toString());
                elementBuilder.setLength(0);
            } else {
                elementBuilder.append(character);
            }

            index += 1;
        }
        return pathElements;
    }

    public boolean containsDescendantFile(String file) {
        Path filePath;
        try {
            filePath = Path.of(file);
        } catch (InvalidPathException invalidPathException) {
            Logger.logError("Invalid path: \"" + file + "\".");
            return false;
        }

        return childEntries.keySet().stream().anyMatch(child -> {
            return child.getActualRespectablePath().equals(filePath);
        });
    }

    public boolean deleteFile(Path file) {
        if (!containsDescendantFile(file.toString())) {
            Logger.logError("Unable to delete file \"" + file+ "\" because " +
                    "the file is not a descendant file.");
            return false;
        }

        File fileObject = file.toFile();
        boolean deleted;
        try {
            deleted = fileObject.delete();
        } catch (SecurityException securityException) {
            Logger.logError("Unable to delete the file \"" + file+ "\" because :" + securityException.getMessage());
            return false;
        }

        if (!deleted) {
            Logger.logError("Was unable to delete the file\"" + file+ "\" for some" +
                    " " +
                    "unspecified reason.");
            return false;
        }

        Optional<Map.Entry<FileNode, FolderNode>> fileNodeToDelete =
                childEntries.entrySet().stream().filter(fileNode -> {
                    return fileNode.getKey().getActualRespectablePath()== file;
                }).findFirst();
        if (fileNodeToDelete.isEmpty()) {
            Logger.logError("For some reason the file \"" + file+ "\" was found a " +
                    "bit before by a call to containsDescendantFile but now was not found in the data structure." +
                    " The file was deleted but unable to be found within the children data structure.");
            return false;
        } else {
            childEntries.remove(fileNodeToDelete.get().getKey());

            for (FolderNode childSubDirectory : folderChildren) {
                childSubDirectory.deleteFile(file);
            }
            fileChildren.remove(fileNodeToDelete.get().getKey());
            return true;
        }
    }

    public boolean deleteFolder(Path folder) {

        File fold = new File(String.valueOf(folder));
        if (!fold.exists() || fold.isDirectory()) {
            Logger.logError("Ce path: " + folder.toString() + " ne correspond a aucun dossier.");
            return false;
        }
        File[] files = fold.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                if (!deleteFolder(files[i].toPath())) {
                    return false;
                }
            } else {
                files[i].delete();
            }
        }
        fold.delete();
        return true;
    }

    public static FolderNode loadOrCreate(Path path) {
        Path absolutePath;
        absolutePath = path;
        File fileObject = absolutePath.toFile();
        if (fileObject.isDirectory()) {
            try {
                return new FolderNode(Path.of(absolutePath.toString()));
            } catch (NotAFileException | NotAFolderException notAFileSystemEntryException) {
                Logger.logError("Tried to create a node from a path which seemed to exist right (path: \"" + absolutePath + "\") before the call to " +
                        "the folder node constructor but then the following exception occurred: " + notAFileSystemEntryException.getMessage() +
                        "The operation will be aborted and null will be returned.");
                return null;
            }
        }

        boolean succeeded;
        try {
            succeeded = fileObject.mkdirs();
        } catch (SecurityException securityException) {
            Logger.logError("Tried creating the necessary folders for the folder node \"" + absolutePath + "\" but " +
                    "the following exception occurred: " + securityException.getMessage() +
                    "The operation will be aborted and null will be returned.");
            return null;
        }
        

        try {
            return new FolderNode(Path.of(absolutePath.toString()));
        } catch (NotAFileException | NotAFolderException notAFileSystemEntryException) {
            Logger.logError("Tried to create a node from a path which seemed to exist right (path: \"" + absolutePath + "\") before the call to " +
                    "the folder node constructor but then the following exception occurred: " + notAFileSystemEntryException.getMessage() +
                    "The operation will be aborted and null will be returnedNote that the folders did not exist but" +
                    " were successfully created.");
            return null;
        }
    }

    public FileNode tryGet(String name, boolean log) {
        Stream<FileNode> filteredCandidates = fileChildren.stream().filter(fileNode -> fileNode.getName().equals(name));
        List<FileNode> candidates = filteredCandidates.toList();

        if (candidates.isEmpty()) {
            if (!log) {
                return null;
            }

            Logger.log("No file with the name \"" + name + "\" was found within the folder node \"" + path.getAbsolutePath() +
                    "\" foundReturning null.");
            return null;
        }

        int numberOfCandidates = candidates.size();
        if (numberOfCandidates != 1 && log) {
            Logger.log("There were multiple files with a name matching \"" + name + "\" in the folder \"" + path.getAbsolutePath() + "\".");
        }

        return candidates.get(0);
    }

    public FolderNode tryGetFolder(String name, boolean log) {
        Stream<FolderNode> filteredCandidates =
                folderChildren.stream().filter(fileNode -> fileNode.getName().equals(name));
        List<FolderNode> candidates = filteredCandidates.toList();

        if (candidates.isEmpty()) {
            if (!log) {
                return null;
            }

            Logger.log("No folder with the name \"" + name + "\" was found within the folder node \"" + path.getAbsolutePath() +
                    "\" foundReturning null.");
            return null;
        }

        int numberOfCandidates = candidates.size();
        if (numberOfCandidates != 1 && log) {
            Logger.log("There were multiple folders with a name matching \"" + name + "\" in the folder \"" + path.getAbsolutePath() + "\".");
        }

        return candidates.get(0);
    }

    public FileNode tryGetPath(String path, boolean log) {
        Path filePath;
        try {
            filePath = Path.of(path);
        } catch (InvalidPathException invalidPathException) {
            Logger.logError("Invalid path: \"" + path + "\".");
            return null;
        }

        Stream<FileNode> filteredCandidates =
                fileChildren.stream().filter(fileNode -> fileNode.getActualRespectablePath().equals(filePath));
        List<FileNode> candidates = filteredCandidates.toList();

        if (candidates.isEmpty()) {
            if (!log) {
                return null;
            }

            Logger.log("No file with the path \"" + path + "\" was found within the folder node \"" + this.path.getAbsolutePath() +
                    "\" foundReturning null.");
            return null;
        }

        int numberOfCandidates = candidates.size();
        if (numberOfCandidates != 1 && log) {
            Logger.log("There were multiple files with a name matching \"" + path + "\" in the folder \"" + this.path.getAbsolutePath() + "\".");
        }

        return candidates.get(0);
    }

    public FolderNode tryGetFolderPath(String path, boolean log) {
        return tryGetFolderPath(path, log, true);
    }

    public FolderNode tryGetFolderPath(String path, boolean log, boolean updateBefore) {
        Path folderPath;
        try {
            folderPath = Path.of(path);
        } catch (InvalidPathException invalidPathException) {
            Logger.logError("Invalid path: \"" + path + "\".");
            return null;
        }

        Path folderPathReal;
        folderPathReal = folderPath;

        if (updateBefore) {
            try {
                setupChildren(folderPathReal.toFile(), this);
            } catch (NotAFileException | NotAFolderException notAFileSystemElementException) {
                Logger.logError(notAFileSystemElementException.getMessage());
                return null;
            }
        }

        if (!isSubPath(folderPathReal)) {
            Logger.log("Folder is not a sub path of rootCannot be found in " + getActualRespectablePath());
            return null;
        }

        List<String> pathElements = getPathElements(folderPathReal);
        List<String> thisPathElements = getPathElements(this.getActualRespectablePath());

        List<String> intermediaryFolderNodes = pathElements.subList(thisPathElements.size(), pathElements.size() - 1);
        FolderNode currentNode = this;
        for (String intermediaryFolderNode : intermediaryFolderNodes) {
            FolderNode nextNode = currentNode.tryGetFolder(intermediaryFolderNode, log);
            if (nextNode == null) {
                return null;
            }

            currentNode = nextNode;
        }

        return currentNode;
    }

    public Node moveNodeTo(Node source) {
        if (source instanceof FileNode fileNode) {
            if (addFile(fileNode))
                return fileNode;
            return null;
        }

        if (source instanceof FolderNode folderNode) {
            if (addFolder(folderNode, true, true))
                return folderNode;
            return null;
        }

        if (source.isFile()) {
            try {
                FileNode fileNode = new FileNode(Path.of(source.getPath().toString()));
                return moveNodeTo(fileNode);
            } catch (NotAFileException | NotAFolderException notAFileSystemElementException) {
                Logger.logError(notAFileSystemElementException.getMessage());
                return null;
            }
        }

        try {
            FolderNode folderNode = new FolderNode(Path.of(source.getPath().toString()));
            return moveNodeTo(folderNode);
        } catch (NotAFileException | NotAFolderException notAFileSystemElementException) {
            Logger.logError(notAFileSystemElementException.getMessage());
            return null;
        }
    }

    public void removeFile(FileNode descendant) {
        fileChildren.remove(descendant);
        childEntries.remove(descendant);
        if (parent != null) {
            parent.removeFile(descendant);
        }
    }

    public boolean delete() {
        if (!path.exists()) {
            Logger.logError("Cannot delete a folder which does not exist.");
            return false;
        }

        boolean deleted = path.delete();
        if (!deleted) {
            Logger.logError("Tried to delete the file but it did not work");
            return false;
        }

        if (parent != null) {
            parent.removeDirectory(this);
        }

        return true;
    }

    private void removeDirectory(FolderNode descendant) {
        folderChildren.remove(descendant);
        descendant.childEntries.forEach((child, parent) -> removeFile(child));
        if (parent != null) {
            parent.removeDirectory(descendant);
        }
    }
}
