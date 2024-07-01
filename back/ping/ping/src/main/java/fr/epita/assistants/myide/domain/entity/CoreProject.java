package fr.epita.assistants.myide.domain.entity;

import fr.epita.assistants.myide.domain.entity.Features.Any.Any;
import fr.epita.assistants.myide.domain.entity.Nodes.FolderNode;
import fr.epita.assistants.myide.utils.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CoreProject implements Project {
    public Node rootNode = null;
    public Set<Aspect> aspects;

    public List<Process> processes = new ArrayList<>();

    public CoreProject(String root)
    {
        try {
            this.rootNode = new FolderNode(Path.of(root));
            this.aspects = new HashSet<>();
            Any anyAspect = new Any();
            aspects.add(anyAspect);
        } catch (NotAFileException | NotAFolderException e) {
            Logger.logError("Can't load the project");

        }


    }

    public void addProcess(Process process)
    {
        processes.add(process);
    }
    public boolean hasAspect(Class<? extends Aspect> aspectClass) {
        for (Aspect aspect : aspects) {
            if (aspect.getClass().equals(aspectClass)) {
                return true;
            }
        }
        return false;
    }
    public void addAsp(Aspect asp)
    {
        this.aspects.add(asp);
    }

    private List<String> getFileArchitectureRec(File root) {
        List<String> fileList = new ArrayList<>();
        if (root.isDirectory() && root.listFiles() != null) {
            for (File file : root.listFiles()) {
                fileList.addAll(getFileArchitectureRec(file));
            }
        } else if (!root.isDirectory()) {
            String doubleQuote = "\"";
            fileList.add(doubleQuote.concat(root.getPath()).concat(doubleQuote));
        }
        return fileList;
    }

    public List<String> getFileArchitecture() {
        if (rootNode == null) {
            Logger.logError("No root found for project");
            return new ArrayList<>();
        }
        File root = new File(rootNode.getPath().toString());
        List<String> fileList = new ArrayList<>();
        if (root.isDirectory())
            return getFileArchitectureRec(root);
        String doubleQuote = "\"";
        fileList.add(doubleQuote.concat(root.getPath()).concat(doubleQuote));
        return fileList;
    }

    @Override
    public Node getRootNode() {
        return rootNode;
    }

    @Override
    public Set<Aspect> getAspects() {
        return aspects;
    }

    @Override
    public Optional<Feature> getFeature(Feature.Type featureType) {
        List<Feature> allFeatures = getFeatures();
        for (var feat: allFeatures)
        {
            if (feat.type() == featureType)
            {
                return Optional.of(feat);
            }
        }
        return Optional.empty();
    }
}
