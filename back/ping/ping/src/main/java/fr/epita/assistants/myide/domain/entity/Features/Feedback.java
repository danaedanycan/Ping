package fr.epita.assistants.myide.domain.entity.Features;


import fr.epita.assistants.myide.domain.entity.Feature;
import fr.epita.assistants.myide.domain.entity.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Feedback implements Feature.ExecutionReport {
    Boolean command_valid = false;
    List<Node> files = null;

    List<Integer> nb_occ = null;
    Set<String> Modified = null;
    Set<String> Untracted = new HashSet<>();

    public void setter_valid(){
        this.command_valid = true;
    }
    public void setValid(boolean isValid) { this.command_valid = isValid; }

    public void set_files(List<Node> files){
        this.files =files;
    }

    public void set_untracted(Set<String> untracted){
        this.Untracted = untracted;
    }
    public Set<String> get_untracted(){
        return this.Untracted;
    }
    public List<Node> get_files(){
        return this.files;
    }
    public void set_nbocc(List<Integer> files){
        this.nb_occ =files;
    }
    public List<Integer> get_nbocc(){
        return this.nb_occ;
    }

    @Override
    public boolean isSuccess() {
        return command_valid;
    }
}
