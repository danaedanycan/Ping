package fr.epita.assistants.myide.domain.entity.Features.Git;

import fr.epita.assistants.myide.domain.entity.*;

import java.util.List;

public class GitAspect implements Aspect {
    @Override
    public Type getType() {
        return Mandatory.Aspects.GIT;
    }

    @Override
    public List<Feature> getFeatureList() {
        return List.of(new GitAdd(),
                new GitPull(),
                new GitPush(),
                new GitCommit(),
                new GitStatus());
    }
}
