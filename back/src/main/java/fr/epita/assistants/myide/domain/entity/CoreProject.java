package fr.epita.assistants.myide.domain.entity;

import fr.epita.assistants.myide.domain.entity.Features.Any.Any;
import fr.epita.assistants.myide.domain.entity.Nodes.FolderNode;
import fr.epita.assistants.myide.utils.Logger;

import java.nio.file.Path;
import java.util.*;

public class CoreProject implements Project {
    public Node rootNode = null;
    public Set<Aspect> aspects;

    public List<Process> processes = new ArrayList<>();

    public CoreProject(String root)
    {
        try {
            this.rootNode = new FolderNode(Path.of(root));
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

    public void addAsp(Aspect asp)
    {
        this.aspects.add(asp);
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
