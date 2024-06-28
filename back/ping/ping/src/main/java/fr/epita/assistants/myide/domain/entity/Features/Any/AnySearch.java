package fr.epita.assistants.myide.domain.entity.Features.Any;

import fr.epita.assistants.myide.domain.entity.*;
import fr.epita.assistants.myide.domain.entity.Features.Feedback;
import fr.epita.assistants.myide.domain.entity.Nodes.FileNode;
import fr.epita.assistants.myide.domain.entity.report.SearchFeatureReport;
import fr.epita.assistants.myide.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AnySearch implements Feature {

    @Override
    public ExecutionReport execute(Project project, Object... params) {
        List<Node> res = new ArrayList<>();
        List<Integer> nb_occ = new ArrayList<>();
        for (Object param : params) {
            Fill_index(res, nb_occ, project.getRootNode().getPath().toString(), param.toString());
        }
        Feedback result = new Feedback();
        if (!res.isEmpty()) {
            result.setter_valid();
            result.set_files(res);
            result.set_nbocc(nb_occ);
        }
        SearchFeatureReport FinalRes = new SearchFeatureReport(res, !res.isEmpty());
        return FinalRes;
    }

    private void Fill_index(List<Node> res, List<Integer> nb_occ, String project, String param) {
        File base = new File(project);
        File[] files = base.listFiles();
        for (File value : files) {
            if (value.isDirectory()) {
                Fill_index(res, nb_occ, value.getPath().toString(), param);
            } else {
                try {
                    File file = new File(value.getPath());
                    Scanner myReader = new Scanner(file);
                    int is_here = 0;
                    while (myReader.hasNextLine()) {
                        String data = myReader.nextLine().toLowerCase();
                        if (data.contains(param.toLowerCase())) {
                            is_here += (data.length() - data.replace(param, "").length()) / param.length();

                        }
                    }
                    if (is_here > 0) {
                        if (isHere(res, value.getPath()))
                            res.add(new FileNode(Path.of(value.getPath())));
                        nb_occ.add(is_here);
                    }

                } catch (NotAFileException | NotAFolderException e) {
                    Logger.log("Problem in adding a new file in the indexer: " + e);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    private boolean isHere(List<Node> res, String path) {

        boolean result = true;
        for (Node node : res) {
            if (node.getPath().toString().equals(path)) {
                return false;
            }

        }
        return result;
    }


    @Override
    public Type type() {
        return Mandatory.Features.Any.SEARCH;
    }
}
