package fr.epita.assistants.myide.domain.entity.Features.Maven;

import fr.epita.assistants.myide.domain.entity.Features.Maven.MavenCommand;
import fr.epita.assistants.myide.domain.entity.Mandatory;

public class MavenExec extends MavenCommand {
    public MavenExec() {
        super(Mandatory.Features.Maven.EXEC);
    }
}
