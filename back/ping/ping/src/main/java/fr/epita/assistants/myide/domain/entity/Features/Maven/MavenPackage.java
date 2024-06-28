package fr.epita.assistants.myide.domain.entity.Features.Maven;

import fr.epita.assistants.myide.domain.entity.Features.Maven.MavenCommand;
import fr.epita.assistants.myide.domain.entity.Mandatory;

public class MavenPackage extends MavenCommand {
    public MavenPackage() {
        super(Mandatory.Features.Maven.PACKAGE);
    }
}
