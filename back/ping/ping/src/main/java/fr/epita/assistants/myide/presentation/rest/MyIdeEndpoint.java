package fr.epita.assistants.myide.presentation.rest;

import fr.epita.assistants.myide.domain.entity.CoreProject;
import fr.epita.assistants.myide.domain.service.Projects;
import fr.epita.assistants.myide.utils.Logger;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MyIdeEndpoint {
    private boolean alreadyTriedLoadingUpProject = false;

    @GET
    @Path("/hello")
    public Response helloWorld() {
        Logger.log("Saying hello !");
        return Response.ok("Hello World !").build();
    }

    @POST
    @Path("/execute-command")
    public Response executeCommand(Map<String, String> request) {
        String command = request.get("command");
        StringBuilder response = new StringBuilder();

        try {
            // Assurez-vous de diviser la commande en arguments si nécessaire
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;

            // Configurer le ProcessBuilder en fonction du système d'exploitation
            if (os.contains("win")) {
                // Commande pour Windows
                processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Commande pour Linux/Unix/Mac
                processBuilder = new ProcessBuilder("sh", "-c", command);
            } else {
                throw new UnsupportedOperationException("Unsupported operating system: " + os);
            }

            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                response.append(output.toString());
            } else {
                response.append(error.toString());
            }

        } catch (Exception e) {
            response.append("Error: ").append(e.getMessage());
        }
        System.out.println(response.toString());
        return Response.ok(response.toString()).build();
    }

    @POST
    @Path("/open/project")
    public Response openProject(String path) {

        System.out.println(path);
        Projects test = new Projects();
        CoreProject proj = (CoreProject) test.load(java.nio.file.Path.of(path));
        if(proj instanceof CoreProject && proj!=null)
            return Response.ok(proj.getRootNode().getPath()+"/pom.xml").build();

        return Response.status(400, "impossible")
                .build();
//        return Response.ok().build();
    }

    @POST
    @Path("/open/file")
    public Response openFile(String path) {
        if (!Files.isRegularFile(Paths.get(path))) {
            String errorMessage = "Cannot open file.";
            Logger.logError(errorMessage + " The path " + path);
            return Response.status(400, errorMessage)
                    .build();
        }

        Logger.log("The path is open.");
        return Response.ok(path)
                .build();
    }

    @POST
    @Path("/create/file")
    public Response createFile(String path) {
        File file = new File(path);
        if (file.isFile()) {
            String errorMessage = "Cannot create file.";
            Logger.logError(errorMessage + " The path " + path);
            return Response.status(400, errorMessage)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
        Logger.log("The file is correctly created");
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @POST
    @Path("/create/folder")
    public Response createFolder(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            String errorMessage = "Cannot create folder.";
            Logger.logError(errorMessage + " The path " + path);
            return Response.status(400, errorMessage)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
        Logger.log("The folder is correctly created");
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @POST
    @Path("/delete/file")
    public Response deleteFile(String path) {
        File file = new File(path);
        if (!file.isFile()) {
            String errorMessage = "Cannot delete file.";
            Logger.logError(errorMessage + " The path " + path);
            return Response.status(400, errorMessage)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
        Logger.log("The file is deleted.");
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @POST
    @Path("/delete/folder")
    public Response deleteFolder(String path) {
        File file = new File(path);
        if (!file.isDirectory()) {
            String errorMessage = "Cannot delete folder.";
            Logger.logError(errorMessage + " The path " + path);
            return Response.status(400, errorMessage)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
        Logger.log("The folder is deleted.");
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @POST
    @Path("/execFeature")
    public Response execFeature(String feature, List<String> params, String project) {
        String errorMessage = "Cannot execute feature.";
        Logger.logError(errorMessage);
        return Response.status(400, errorMessage)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @POST
    @Path("/move")
    public Response move(String src, String dst) {
        String errorMessage = "Cannot move file";
        Logger.log(errorMessage + " from " + src + " to " + dst);
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @POST
    @Path("/update")
    public Response update(String path, int from, int to, String content) {
        File file = new File(path);
        if (file.isFile()) {
            String message = "File is updated.";
            Logger.log(message + " The path " + path + " from " + from + " to " + to + " content " + content);
            return Response.ok(message)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }

        String errorMessage = "Cannot update file.";
        Logger.logError(errorMessage + " The path " + path + " from " + from + " to " + to + " content " + content);
        return Response.status(400, errorMessage)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }
}

