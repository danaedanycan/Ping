package fr.epita.assistants.myide.domain.entity.Features.Any;

import fr.epita.assistants.myide.domain.entity.*;
import fr.epita.assistants.myide.domain.entity.Features.Feedback;
import fr.epita.assistants.myide.domain.entity.Nodes.FolderNode;
import fr.epita.assistants.myide.utils.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AnyDist implements Feature {


    @Override
    public ExecutionReport execute(Project project, Object... params) {
        Feedback res = new Feedback();
        AnyCleanup t = new AnyCleanup();
        t.execute(project, params);
        ZipOutputStream zipOut = null;
        FileOutputStream fos = null;
        try {
            FolderNode root = new FolderNode(Path.of(project.getRootNode().getPath().toString()));
            File folder = new File(project.getRootNode().getPath().toString());

            fos = new FileOutputStream(root.getName() + ".zip");
            zipOut = new ZipOutputStream(fos);
            File[] srcFiles = folder.listFiles();
            if (srcFiles != null) {
                System.out.println(srcFiles.length);
                for (File srcFile : srcFiles) {
                    if (srcFile.isFile()) {
                        zipFile(srcFile, zipOut);
                    } else {
                        zipFolder(srcFile, srcFile.getName(), zipOut);
                    }
                }
            }
        } catch (NotAFileException | NotAFolderException | FileNotFoundException e) {
            Logger.log("Exception: " + e);
        } catch (IOException e) {
            Logger.log("Io exception error: " + e);
        } finally {
            try {
                if (zipOut != null) {
                    zipOut.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Logger.log("Io exception error during close: " + e);
            }
        }
        res.setter_valid();
        return res;
    }

    private void zipFile(File file, ZipOutputStream zipOut) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            zipOut.closeEntry();
        }
    }

    private void zipFolder(File folder, String parentFolder, ZipOutputStream zipOut) throws IOException {
        File[] srcFiles = folder.listFiles();
        if (srcFiles != null) {
            for (File srcFile : srcFiles) {
                if (srcFile.isFile()) {
                    zipOut.putNextEntry(new ZipEntry(parentFolder + "/" + srcFile.getName()));
                    try (FileInputStream fis = new FileInputStream(srcFile)) {
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = fis.read(bytes)) >= 0) {
                            zipOut.write(bytes, 0, length);
                        }
                        zipOut.closeEntry();
                    }
                } else {
                    zipFolder(srcFile, parentFolder + "/" + srcFile.getName(), zipOut);
                }
            }
        }
    }



    @Override
    public Type type() {
        return Mandatory.Features.Any.DIST;
    }
}
