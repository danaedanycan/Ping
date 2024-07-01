package fr.epita.assistants.myide.domain.entity.Features.Git;

import fr.epita.assistants.myide.domain.entity.Mandatory;

public class GitTag extends GitCommand{
    public GitTag() {
        super(Mandatory.Features.Git.TAG);
    }
}
