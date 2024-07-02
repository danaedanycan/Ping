package fr.epita.assistants.myide.domain.entity.report;

import fr.epita.assistants.myide.domain.entity.Feature;
import fr.epita.assistants.myide.domain.entity.Node;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @param searchResult All file node where the query have been found.
 * @param isSuccess  Is the report successful.
 */
public record SearchFeatureReport(@NotNull List<String> searchResult, boolean isSuccess) implements Feature.ExecutionReport {
    public List<String> getResults() {
        if(isSuccess)
            return searchResult;

        return null;
    }
}


