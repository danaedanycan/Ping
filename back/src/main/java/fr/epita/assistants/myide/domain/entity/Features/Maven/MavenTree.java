package fr.epita.assistants.myide.domain.entity.Features.Maven;

import fr.epita.assistants.myide.domain.entity.Features.Maven.MavenCommand;
import fr.epita.assistants.myide.domain.entity.Mandatory;

public class MavenTree extends MavenCommand {
    public MavenTree() {
        super(Mandatory.Features.Maven.TREE);
    }
}
