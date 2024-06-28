package fr.epita.assistants.myide.domain.entity.Features.Maven;

import fr.epita.assistants.myide.domain.entity.Features.Maven.MavenCommand;
import fr.epita.assistants.myide.domain.entity.Mandatory;

public class MavenInstall extends MavenCommand {
    public MavenInstall() {
        super(Mandatory.Features.Maven.INSTALL);
    }
}
