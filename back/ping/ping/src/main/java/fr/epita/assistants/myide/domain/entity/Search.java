package fr.epita.assistants.myide.domain.entity;

import fr.epita.assistants.myide.domain.entity.Feature;
import fr.epita.assistants.myide.domain.entity.Project;

public class Search implements Feature {
    /**
     * @param project {@link Project} on which the feature is executed.
     * @param params  Parameters given to the features.
     * @return {@link ExecutionReport}
     */
    @Override
    public ExecutionReport execute(Project project, Object... params) {
        return null;
    }

    /**
     * @return The type of the Feature.
     */
    @Override
    public Type type() {
        return null;
    }

}
