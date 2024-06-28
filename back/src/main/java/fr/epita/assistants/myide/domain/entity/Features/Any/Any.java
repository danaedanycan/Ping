package fr.epita.assistants.myide.domain.entity.Features.Any;

import fr.epita.assistants.myide.domain.entity.*;

import java.util.List;

public class Any implements Aspect {
    @Override
    public Type getType() {
        return Mandatory.Aspects.ANY;
    }

    @Override
    public List<Feature> getFeatureList() {
        return List.of(new AnyCleanup(), new AnyDist(), new AnySearch());
    }
}
