package fr.epita.assistants.myide.domain.entity;

import fr.epita.assistants.myide.domain.entity.Nodes.FileNode;
import fr.epita.assistants.myide.domain.entity.Nodes.FolderNode;

import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Configuration {

    private final FolderNode temporaryStorage;

    private final FileNode settingsStorageFile;

    private final List<RandomAccessFile> indexer;


    public Configuration(Path temporaryStorage) throws UnloadableConfigurationException {
        this.temporaryStorage = FolderNode.loadOrCreate(temporaryStorage);
        if (this.temporaryStorage == null) {
            throw new UnloadableConfigurationException("The temporary storage folder could not be loaded" +
                    "Check logs for more information.");
        }

        settingsStorageFile = this.temporaryStorage.tryGet("settings.json", true);
        if (settingsStorageFile == null) {
            throw new UnloadableConfigurationException("The settings file could not be loaded" +
                    "Check logs for more information.");
        }

        indexer = new ArrayList<>();

    }
    public FolderNode GetPathStorage(){
        return this.temporaryStorage;

    }
    public FileNode GetFileStorage(){
        return this.settingsStorageFile;
    }

    public List<RandomAccessFile> Get_index(){
        return this.indexer;
    }

    public void Add_newRandom(RandomAccessFile e){
        this.indexer.add(e);
    }
    public void Delete_Random(RandomAccessFile e) {
        for (RandomAccessFile random:this.indexer) {
            if (random == e){
                this.indexer.remove(random);
            }
        }
    }
}
