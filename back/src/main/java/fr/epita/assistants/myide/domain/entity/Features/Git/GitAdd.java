package fr.epita.assistants.myide.domain.entity.Features.Git;

import fr.epita.assistants.myide.domain.entity.Mandatory;

public class GitAdd extends GitCommand {

    public GitAdd() {
        super(Mandatory.Features.Git.ADD);
    }
}
