package fr.epita.assistants.myide.domain.entity.Features.Any;

import fr.epita.assistants.myide.domain.entity.Feature;
import fr.epita.assistants.myide.domain.entity.Features.Feedback;
import fr.epita.assistants.myide.domain.entity.Mandatory;
import fr.epita.assistants.myide.domain.entity.Project;
import fr.epita.assistants.myide.utils.Logger;

import java.io.*;

public class AnyCleanup implements Feature {
    @Override
    public ExecutionReport execute(Project project, Object... params)  {

        Feedback res = new Feedback();
        File file = new File(project.getRootNode().getPath()+ "/.myideignore");
        FileReader fr = null;
        try {
            fr = new FileReader(file);

        BufferedReader br = new BufferedReader(fr);
        for(String line = br.readLine(); line != null;line=br.readLine()){

            File to_delete = new File(project.getRootNode().getPath().toString() + "/" + line);
            if(to_delete.isFile()){
                to_delete.delete();
            }
            else{
                Delete_folder(to_delete);
            }

        } } catch (IOException e) {

            Logger.log("Any Cleanup error:\n"+e);
            return res;
        }

        res.setter_valid();

        return res;
    }

    private void Delete_folder(File folder){
        File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            if(files[i].isDirectory()){
                Delete_folder(files[i]);
            }
            else {
                files[i].delete();
            }
        }
        folder.delete();
    }

    @Override
    public Type type() {
        return Mandatory.Features.Any.CLEANUP;
    }
}
