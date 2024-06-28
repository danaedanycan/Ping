package fr.epita.assistants.myide.domain.entity.Features.Maven;

import fr.epita.assistants.myide.domain.entity.*;

import java.util.List;

public class MavenAspect implements Aspect {
    @Override
    public Type getType() {
        return Mandatory.Aspects.MAVEN;
    }

    @Override
    public List<Feature> getFeatureList() {
        return List.of(new MavenCompile(),
                new MavenClean(),
                new MavenTest(),
                new MavenPackage(),
                new MavenInstall(),
                new MavenExec(),
                new MavenTree());
    }
}
