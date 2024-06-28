package fr.epita.assistants.myide.domain.entity.Features;


import fr.epita.assistants.myide.domain.entity.Feature;
import fr.epita.assistants.myide.domain.entity.Node;

import java.util.List;

public class Feedback implements Feature.ExecutionReport {
    Boolean command_valid = false;
    List<Node> files = null;

    List<Integer> nb_occ = null;

    public void setter_valid(){
        this.command_valid = true;
    }
    public void setValid(boolean isValid) { this.command_valid = isValid; }

    public void set_files(List<Node> files){
        this.files =files;
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
