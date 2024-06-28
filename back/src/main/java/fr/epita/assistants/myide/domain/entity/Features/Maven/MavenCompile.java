package fr.epita.assistants.myide.domain.entity.Features.Maven;

import fr.epita.assistants.myide.domain.entity.Features.Maven.MavenCommand;
import fr.epita.assistants.myide.domain.entity.Mandatory;

public class MavenCompile extends MavenCommand {
    public MavenCompile() {
        super(Mandatory.Features.Maven.COMPILE);
    }
}
