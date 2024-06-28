package fr.epita.assistants.myide.domain.entity.Features.Maven;

import fr.epita.assistants.myide.domain.entity.Mandatory;

public class MavenClean extends MavenCommand {
    public MavenClean() {
        super(Mandatory.Features.Maven.CLEAN);
    }
}
