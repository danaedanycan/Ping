package fr.epita.assistants.myide.presentation.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epita.assistants.myide.domain.entity.CoreProject;
import fr.epita.assistants.myide.domain.entity.Feature;
import fr.epita.assistants.myide.domain.entity.Features.Feedback;
import fr.epita.assistants.myide.domain.entity.Features.Git.GitAdd;
import fr.epita.assistants.myide.domain.entity.Features.Git.GitAspect;
import fr.epita.assistants.myide.domain.entity.Features.Git.GitCommand;
import fr.epita.assistants.myide.domain.entity.Features.Git.GitStatus;
import fr.epita.assistants.myide.domain.entity.classes.Credentials;
import fr.epita.assistants.myide.domain.service.AddClass;
import fr.epita.assistants.myide.domain.service.Projects;
import fr.epita.assistants.myide.domain.service.UpdateRequest;
import fr.epita.assistants.myide.utils.Logger;
import jakarta.json.Json;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.*;
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
    @GET
    @Path("/get/credentials")
    public Response Get_credentials() {
        String filePath = "src/main/resources/credentials.txt";

        // Appel de la méthode pour lire les deux premières lignes
        String[] result = readFile(filePath);
        // Afficher le résultat
        if (result != null) {
            Credentials cred = new Credentials(result[0],result[1]);
            return Response.ok(cred).build();
        } else {
            System.out.println("iciiiii");
            return Response.status(400,"Their is not git credentials please set up them:").build();
        }
    }

    public static String[] readFile(String filePath) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String[] lines = new String[3];
            lines[0] = reader.readLine();
            lines[1] = reader.readLine();
            lines[2] = reader.readLine();

            // Check if there are more lines in the file
            if (reader.readLine() != null) {
                return null;
            }

            if (lines[0] != null && lines[1] != null && lines[2] != null) {
                return lines;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @POST
    @Path("/set/credentials")
    public Response Set_credentials(String Jsonobj) {
        String filePath = "src/main/resources/credentials.txt";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Credentials credentials = objectMapper.readValue(Jsonobj, Credentials.class);
            credentials.writeToFile(filePath);
            return Response.ok().build();

        } catch (Exception e) {
            System.out.println(e);
            return  Response.status(400,"There is a probleme in the credentials saving").build();
        }
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response openProject(String path) {
        try {
            System.out.println("Received path: " + path);

            Projects test = new Projects();
            CoreProject proj = (CoreProject) test.load(java.nio.file.Path.of(path));

            if (proj instanceof CoreProject && proj != null && proj.getRootNode() != null) {
                return Response.ok(proj.getFileArchitecture()).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("Impossible de charger le projet.").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erreur interne lors du chargement du projet.").build();
        }
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
        try {
            if (file.createNewFile()) {
                Logger.log("The file is correctly created");
                return Response.ok(file.getAbsolutePath())
                        .build();
            }
            String errorMessage = "The file "+path+" already exist";
            Logger.logError(errorMessage );
            return Response.status(400, errorMessage)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @POST
    @Path("/create/folder")
    public Response createFolder(String path) {
        if (!Files.exists(java.nio.file.Path.of(path))) {
            try {
                // Create the directory
                Files.createDirectories(java.nio.file.Path.of(path));
                System.out.println("Directory created successfully at " + java.nio.file.Path.of(path));
                return Response.ok().build();
            } catch (IOException e) {
                return Response.status(400, "Failed to create directory: " + e.getMessage()).build();
            }
        } else {
            System.out.println("Directory already exists at " + java.nio.file.Path.of(path));
            return Response.status(400, "This Folder already exists.").build();
        }

    }


    @POST
    @Path("/delete/file")
    public Response deleteFile(String path) {

        if (!Files.exists(java.nio.file.Path.of(path))) {


        }
        try {
            Files.delete(java.nio.file.Path.of(path));
        } catch (IOException e) {
            return Response.status(400, "File does not exist: " + path)
                    .build();
        }
        System.out.println("File deleted successfully.");

        return Response.ok()
                .build();
    }

    @POST
    @Path("/delete/folder")
    public Response deleteFolder(String path) {


        File folder = new File(path);

        try {
            // Call a recursive method to delete the folder and its contents
            deleteFolder(folder);
        } catch (Exception e) {
            return Response.status(400,"Failed to delete folder: " + e.getMessage()).build();
        }
        return Response.ok().build();
    }


    private static void deleteFolder(File folder) throws Exception {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        if (!folder.delete()) {
            throw new Exception("Failed to delete " + folder);
        }
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
    @Path("/execFeature/status")
    public Response Gitstat(String project_path){
        Projects test = new Projects();
        CoreProject proj = (CoreProject) test.load(java.nio.file.Path.of(project_path));
        if(proj instanceof CoreProject && proj!=null && proj.hasAspect(GitAspect.class)) {
            GitStatus stat = new GitStatus();
            Feedback feed = (Feedback) stat.execute(proj);
            ObjectMapper mapper = new ObjectMapper();
            try {
                String res = mapper.writeValueAsString(feed.get_untracted());
                return Response.ok(res).build();
            } catch (JsonProcessingException e) {
                return Response.status(400, "impossible")
                        .build();
            }

        }
        return Response.ok()
                .build();
//        return Response.ok().build();
    }

    @POST
    @Path("/execFeature/Gitadd")
    public Response GitAdd(String project_path){

        System.out.println(project_path);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            AddClass projectData = objectMapper.readValue(project_path, AddClass.class);

            String currentProject = projectData.getCurrent_project();
            String add = projectData.getAdd();
            Projects test = new Projects();
            CoreProject proj = (CoreProject) test.load(java.nio.file.Path.of(currentProject));
            if(proj != null && proj.hasAspect(GitAspect.class)) {
                GitAdd gitAdd = new GitAdd();
                Feedback add_feedback = (Feedback) gitAdd.execute(proj,add);
                if(add_feedback.isSuccess()){
                    GitStatus stat = new GitStatus();
                    Feedback feed = (Feedback) stat.execute(proj);
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        String res = mapper.writeValueAsString(feed.get_untracted());
                        return Response.ok(res).build();
                    } catch (JsonProcessingException e) {
                        return Response.status(400, "impossible")
                                .build();
                    }
                }
                else{
                    return Response.status(400, "impossible")
                            .build();
                }

            }
            return Response.ok()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(400, "impossible")
                    .build();
        }

    }

    @POST
    @Path("/move")
    public Response move(String JsonRequest) {
        ObjectMapper objectMapper = new ObjectMapper();
        UpdateRequest updateRequest = null;
        try {
            updateRequest = objectMapper.readValue(JsonRequest, UpdateRequest.class);
        } catch (JsonProcessingException e) {
            return Response.status(400, e.toString()).build();
        }
        String src = updateRequest.getPath();
        String dst = updateRequest.getContent();
        System.out.println(src+"\n"+dst);

        try {
            // Déplacer le fichier ou le dossier
            Files.move(java.nio.file.Path.of(src), java.nio.file.Path.of(dst));
        } catch (IOException e) {
            System.out.println(e);
            return Response.status(400,"Erreur lors du déplacement : " + e.getMessage() ).build();
        }
        return Response.ok("Déplacement réussi de " + src + " vers " + dst).build();
    }

    @POST
    @Path("/update")
    public Response update(String JsonRequest) {

        ObjectMapper objectMapper = new ObjectMapper();
        UpdateRequest updateRequest = null;
        try {
            updateRequest = objectMapper.readValue(JsonRequest, UpdateRequest.class);
        } catch (JsonProcessingException e) {
            return Response.status(400, e.toString()).build();
        }

        String path = updateRequest.getPath();
        String content = updateRequest.getContent();

        if (!Files.exists(java.nio.file.Path.of(path))) {
            return Response.status(400, "It's impossible to update a file that does not exist.").build();

        }
        try {
            Files.write(java.nio.file.Path.of(path),content.getBytes());
        } catch (IOException e) {
            return Response.status(400, "It's impossible to update a file that does not exist.").build();
        }
        return Response.ok(path).build();
    }
}

