package fr.epita.assistants.myide.domain.entity.Features.Git;

import fr.epita.assistants.myide.domain.entity.Features.Git.GitCommand;
import fr.epita.assistants.myide.domain.entity.Mandatory;

public class GitPull extends GitCommand {
    public GitPull() {
        super(Mandatory.Features.Git.PULL);
    }
}
