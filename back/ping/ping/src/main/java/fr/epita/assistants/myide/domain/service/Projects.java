package fr.epita.assistants.myide.domain.service;

import fr.epita.assistants.myide.domain.entity.*;
import fr.epita.assistants.myide.domain.entity.Features.Git.GitAspect;
import fr.epita.assistants.myide.domain.entity.Features.Maven.MavenAspect;
import fr.epita.assistants.myide.domain.entity.Nodes.FolderNode;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Projects implements ProjectService {

    public NodeService nodes = new Nodes();

    public Projects() {
    }

    /**
     * Load a {@link Project} from a path.
     *
     * @param root Path of the root of the project to load.
     * @return New project.
     */
    @Override
    public  Project load(Path root) {

        CoreProject project = new CoreProject(root.toString());
        if(project.getRootNode() == null)
            return null;
        File f = root.resolve(Paths.get(".git")).toFile();
        if (f.exists() && f.isDirectory()) {
            project.addAsp(new GitAspect());
        }
        File ff = root.resolve(Paths.get("pom.xml")).toFile();
        if (ff.exists() && !ff.isDirectory()) {
            project.addAsp(new MavenAspect());
        }
        return project;
    }

    /**
     * Execute the given feature on the given project.
     *
     * @param project     Project for which the features is executed.
     * @param featureType Type of the feature to execute.
     * @param params      Parameters given to the features.
     * @return Execution report of the feature.
     */
    @Override
    public Feature.ExecutionReport execute(Project project, Feature.Type featureType, Object ...params) {
        return null;
    }

    /**
     * @return The {@link NodeService} associated with your {@link Projects}
     */
    @Override
    public NodeService getNodeService() {
        return nodes;
    }

}
