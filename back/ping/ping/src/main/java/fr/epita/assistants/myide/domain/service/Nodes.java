package fr.epita.assistants.myide.domain.service;

import fr.epita.assistants.myide.domain.entity.*;
import fr.epita.assistants.myide.domain.entity.Nodes.FileNode;
import fr.epita.assistants.myide.domain.entity.Nodes.FolderNode;
import fr.epita.assistants.myide.utils.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

public class Nodes implements NodeService {

    @Override
    public Node update(Node node, int from, int to, byte[] insertedContent) {
        if (node.isFolder()) {
            return node;
        } else {
            byte[] bytes = new byte[0];
            try {
                bytes = Files.readAllBytes(node.getPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] a = Arrays.copyOfRange(bytes, 0, from);
            byte[] b = new byte[0];
            if (to >= bytes.length) {
                b = new byte[0];
            } else {
                b = Arrays.copyOfRange(bytes, to, bytes.length);
            }
            try {
                outputStream.write(a);
                outputStream.write(insertedContent);
                outputStream.write(b);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            byte[] c = outputStream.toByteArray();
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(node.getPath().toFile(), false);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                output.write(c);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return node;
        }
    }

    @Override
    public boolean delete(Node node) {
        File deleteDirectory = node.getPath().toFile();
        List<Node> allContents = node.getChildren();
        if (!allContents.isEmpty()) {
            for (var nodos : allContents) {
                delete(nodos);
            }
        }
        deleteDirectory.delete();
        node = null;
        return true;
    }

    @Override
    public Node create(Node folder, String name, Node.Type type) {
        if (type == Node.Types.FILE) {
            Node newNode = null;
            try {
                newNode = new FileNode(folder.getPath().resolve(Paths.get(name)));
            } catch (NotAFileException e) {
                throw new RuntimeException(e);
            } catch (NotAFolderException e) {
                throw new RuntimeException(e);
            }
            File file = folder.getPath().resolve(Paths.get(name)).toFile();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return newNode;
        } else {
            Node newNode = null;
            try {
                newNode = new FolderNode(folder.getPath().resolve(Paths.get(name)));
            } catch (NotAFileException e) {
                throw new RuntimeException(e);
            } catch (NotAFolderException e) {
                throw new RuntimeException(e);
            }
            File dir = folder.getPath().resolve(Paths.get(name)).toFile();
            dir.mkdirs();
            return newNode;
        }
    }

    public void ChangeChildren(Node node, Node parent) {
        List<Node> allChildren = node.getChildren();
        for (var noeud : allChildren) {
            ChangeChildren(noeud, parent);
        }
        if (node.isFile()) {
            ((FileNode) node).setPath(parent.getPath().resolve(node.getPath().getFileName()));
        } else {
            ((FolderNode) node).setPath(parent.getPath().resolve(node.getPath().getFileName()));
        }
    }

    @Override
    public Node move(Node nodeToMove, Node destinationFolder) {
        try {
            Files.move(nodeToMove.getPath(), destinationFolder.getPath().resolve(nodeToMove.getPath().getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (nodeToMove.isFile()) {
            FileNode recipient = (FileNode) nodeToMove;
            recipient.setPath(destinationFolder.getPath().resolve(nodeToMove.getPath().getFileName()));
        } else {
            ChangeChildren(nodeToMove, destinationFolder);
        }
        return nodeToMove;
    }
}
