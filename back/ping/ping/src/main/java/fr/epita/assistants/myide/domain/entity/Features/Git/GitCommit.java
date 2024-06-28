package fr.epita.assistants.myide.domain.entity.Features.Git;

import fr.epita.assistants.myide.domain.entity.Features.Git.GitCommand;
import fr.epita.assistants.myide.domain.entity.Mandatory;

public class GitCommit extends GitCommand {
    public GitCommit() {
        super(Mandatory.Features.Git.COMMIT);
    }
}
