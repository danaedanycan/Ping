package fr.epita.assistants;

import fr.epita.assistants.myide.domain.entity.Project;
import fr.epita.assistants.myide.domain.entity.CoreProject;
import fr.epita.assistants.myide.domain.service.ProjectService;
import fr.epita.assistants.myide.domain.service.Projects;
import fr.epita.assistants.myide.utils.Given;

import java.nio.file.Path;

/**
 * Starter class, we will use this class and the init method to get a
 * configured instance of {@link ProjectService}.
 */
@Given(overwritten = false)
public class MyIde {

    public Path indexFile;
    public Path tempFolder;
    /**
     * Init methodIt must return a fully functional implementation of {@link ProjectService}.
     *
     * @return An implementation of {@link ProjectService}.
     */
    public ProjectService init(Path path ) {
        Projects proj = null;
        CoreProject project = (CoreProject) proj.load(path);
        if(project instanceof CoreProject && project != null){

            return null;
        }
        return (ProjectService) project;
    }

    /**
     * Record to specify where the configuration of your IDE
     * must be storedMight be useful for the search feature.
     */
    public record Configuration(Path indexFile,
                                Path tempFolder) {

    }
}
