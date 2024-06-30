package fr.epita.assistants.myide.domain.entity.Features.Git;

import fr.epita.assistants.myide.domain.entity.Mandatory;

public class GitStatus extends GitCommand {

    public GitStatus() {
        super(Mandatory.Features.Git.STATUS);
    }
}
