package fr.epita.assistants.myide.domain.entity.Features.Maven;

import fr.epita.assistants.myide.domain.entity.Features.Maven.MavenCommand;
import fr.epita.assistants.myide.domain.entity.Mandatory;

public class MavenTest extends MavenCommand {
    public MavenTest() {
        super(Mandatory.Features.Maven.TEST);
    }
}
