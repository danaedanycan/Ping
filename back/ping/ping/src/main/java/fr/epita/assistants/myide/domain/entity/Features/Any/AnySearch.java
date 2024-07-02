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
        List<String> res = new ArrayList<>();
        List<Integer> nb_occ = new ArrayList<>();
        for (Object param : params) {
            Fill_index(res, nb_occ, project.getRootNode().getPath().toString(), param.toString());
        }

        SearchFeatureReport FinalRes = new SearchFeatureReport(res, !res.isEmpty());
        return FinalRes;
    }

    private void Fill_index(List<String> res, List<Integer> nb_occ, String project, String param) {
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
                            res.add((value.getPath()));
                        nb_occ.add(is_here);
                    }
                } catch (FileNotFoundException e) {
                    Logger.logError(e.toString());
                }
            }
        }

    }

    private boolean isHere(List<String> res, String path) {

        boolean result = true;
        for (String node : res) {
            if (node.equals(path)) {
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
