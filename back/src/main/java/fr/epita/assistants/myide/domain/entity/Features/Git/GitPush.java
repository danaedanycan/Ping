package fr.epita.assistants.myide.domain.entity.Features.Git;

import fr.epita.assistants.myide.domain.entity.Features.Git.GitCommand;
import fr.epita.assistants.myide.domain.entity.Mandatory;

public class GitPush extends GitCommand {
    public GitPush() {
        super(Mandatory.Features.Git.PUSH);
    }
}
