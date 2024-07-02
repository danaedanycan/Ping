const { shell } = require('electron');
const { ipcRenderer } = require('electron');

const ideButton = document.getElementById("idebutton");
const code_display = document.getElementById("code_data");
const terminal_input = document.getElementById("fileInput");
const shell_ici = document.getElementById("shell_command");
const filename = document.getElementById("bonjour");
const codepanel = document.getElementById("code");
const terminalpanel = document.getElementById("terminal");
const shortcutspanel = document.getElementById("shortcuts");
const archipanel = document.getElementById("run-configs");
const Commit_Button = document.getElementById("commit_button");
const Push_Button = document.getElementById("push_button");
const Pull_Button = document.getElementById("pull_button");
const execute =  document.getElementById("execute_file")
const fileList =  document.getElementById("fileList");
let current_project = null;
let to_add = [];

async function showDialog(message) {
    await ipcRenderer.invoke('show-dialog', message);
}

class ConfigWindow {

    constructor(fileList) {
        this.fileList = fileList;
    }

    getFileList() {
        return this.fileList;
    }

}

function displayProjectArchitecture(configWindow) {
    fileList.innerHTML = '';
    for (const filePath of configWindow.getFileList()) {
        //const filePathHTML = document.createElement('li');
        const filePathButtonHTML = document.createElement('button');
        filePathButtonHTML.addEventListener("click", async () => {
            await OpenFile(filePath);
        })
        filePathButtonHTML.innerHTML = filePath;
        // filePathHTML.appendChild(filePathButtonHTML);
        fileList.appendChild(filePathButtonHTML);
    }
}
async function Openproject(path) {
    try {
        const response = await fetch('http://localhost:8080/api/open/project', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: path
        });
        const result = await response.text();
        if (response.ok) {
            //terminal_input.value += "\n"+result;
            const resultModified = result.replace(/\\/g, "\\\\");
            //terminal_input.value += "\n" + resultModified;
            const configWindow = new ConfigWindow(JSON.parse(resultModified));
            displayProjectArchitecture(configWindow);
            current_project = path;
            terminal_input.value += "\n The project iss correctly open !\n";
            await showDialog("The project is correctly open !")
            await GitStatus();
        } else {
            terminal_input.value += "\nyhtyhyt" + result+"\n";
            await showDialog(result);
        }
    } catch (error) {
        terminal_input.value += "\nici" + error+"\n";
        await showDialog(error)
    }
}



//requete git Status
async function GitStatus() {
    try {
        const response = await fetch('http://localhost:8080/api/execFeature/status', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: current_project
        });
        const result = await response.text();
        if (response.ok) {
            let list;
            try {
                list = JSON.parse(result);
            } catch (error) {
                terminal_input.value += ("\nFailed to parse string as JSON:"+ error);
                await showDialog("\nFailed to parse string as JSON: "+ error)
            }
            if (Array.isArray(list)) {
                const buttonContainer = document.getElementById('buttonContainer');
                //vide le conteneur actuel des files à Add
                buttonContainer.innerHTML = '';
                //pour chaque File à add, affiche un bouton dans la fenêtre git
                list.forEach(item => {
                    const button = document.createElement('button');
                    button.textContent = item; // Le texte du bouton est l'élément de la liste
                    button.classList.add('btn'); // Ajouter une classe si nécessaire
                    button.addEventListener('click', () => {
                        Git_ADD(button.textContent)
                    });

                    buttonContainer.appendChild(button);
                });
            } else {
                terminal_input.value += ("The parsed result is not an array.");
                await showDialog("The parsed result is not an array.")
            }


        } else {
            terminal_input.value += "\n" + result + "\n";
            await showDialog(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        await showDialog(error)
    }
}

//Requete git add, ne prends qu'un fichier/dossier pour le moment en Path, il n'a pas à etre absolu si le projet a été ouvert
//si current_project == null ne peut pas marcher.
async function Git_ADD(path) {
    try {
        const response = await fetch('http://localhost:8080/api/execFeature/Gitadd', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ current_project: current_project, add: path })
        });
        const result = await response.text();
        if (response.ok) {
            if(current_project!==null)
                await GitStatus();
            await showDialog("ADD is succesfull We remove it from the files to add list.")
        } else {
            terminal_input.value += "\n" + result + "\n";
            await showDialog(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        await showDialog(error)
    }
}
//Commit, prends le message mess
async function GitCommit(commit_mess) {
    try {
        const response = await fetch('http://localhost:8080/api/execFeature/Gitcommit', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ current_project: current_project, commit: commit_mess })
        });
        const result = await response.text();
        if (response.ok) {
            await showDialog("Commit is Successfull ! You can Push !")
        } else {
            terminal_input.value += "\efzfen" + result + "\n";
            await showDialog("Commit is not Successfull !")
        }
    } catch (error) {
        terminal_input.value += "\nggrr" + error + "\n";
        await showDialog(error)
    }
}
//Git tag with the tag message commit_mess
async function GitTag(commit_mess) {
    try {
        const response = await fetch('http://localhost:8080/api/execFeature/Gittag', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ current_project: current_project, commit: commit_mess })
        });
        const result = await response.text();
        if (response.ok) {
            await showDialog("Commit is successfuly tag!")
        } else {
            terminal_input.value += "\n" + result + "\n";
            await showDialog(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        await showDialog(error)
    }
}
//push
async function GitPush() {
    try {
        await GitPull();
        const response = await fetch('http://localhost:8080/api/execFeature/Gitpush', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body:current_project
        });
        if (response.ok) {
            await showDialog("Push is Successfull! Congratulation !")
        } else {
            await showDialog("Push isn't Successfull, maybe you need to PULL, or set correctly your git credentials")
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        await showDialog(error)
    }
}
async function GitPull() {
    try {
        const response = await fetch('http://localhost:8080/api/execFeature/Gitpull', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body:current_project
        });
        if (response.ok) {
            await showDialog("Pull is Successfull! Congratulation !")
        } else {
            await showDialog("Pull isn't Successfull, maybe you need to set correctly your git credentials")
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        await showDialog(error)
    }
}
//open a project and charge Files to add in GitWindow

//open a File in CodeWindow
async function OpenFile(path) {
    try {
        const response = await fetch('http://localhost:8080/api/open/file', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: path
        });
        const result = await response.text();
        if (response.ok) {
            const response = await fetch(result);
            if (response.ok) {
                code_display.value = await response.text();
                filename.innerHTML = result;
                await showDialog("File "+result+" is open in Code Window")
            } else {
                terminal_input.value += "\n" + result + "\n";
                await showDialog(result)
            }
        } else {
            terminal_input.value += "\n" + result + "\n";
            await showDialog(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
    }
}
//create a File and update current Files to add
async function CreateFile(path) {
    try {
        const response = await fetch('http://localhost:8080/api/create/file', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: path
        });
        const result = await response.text();
        if (response.ok) {
            await OpenFile(result);
            to_add.push(result);
            terminal_input.value += "\n The file is correctly created.\n";
            await showDialog("The file is correctly created.")
            if(current_project!==null)
                await GitStatus();
        } else {
            terminal_input.value += "\n" + result + "\n";
            await showDialog(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        await showDialog(error)
    }
}
//Delete a File and update files to add
async function DeleteFile(path) {
    try {
        const response = await fetch('http://localhost:8080/api/delete/file', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: path
        });
        const result = await response.text();
        if (response.ok) {
            filename.innerHTML = "";
            code_display.value = "";
            terminal_input.value += "\n The file is correctly deleted.\n"
            await showDialog("The file is correctly deleted.")
            if (to_add.find(path)) {
                to_add = to_add.filter(item => item !== path)
            }
            if(current_project!==null)
                await GitStatus();
        } else {
            terminal_input.value += "\n" + result + "\n";
            await showDialog(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        await showDialog(error)
    }
}
//close A file from CodeWindow
function CloseFile() {
    code_display.value = ""
    filename.innerHTML = ""
}
function CloseProject(){
    current_project = null;
    fileList.innerHTML = "";
    filename.value = "";
    code_display.value = "";
    to_add.innerHTML = "";
    document.getElementById("execute_result").value="";
    document.getElementById("commit_message").value="";
    document.getElementById("tag_message").value=""
}
//Update a file and update files to add
async function UpdateFile(path, content) {
    try {
        const response = await fetch('http://localhost:8080/api/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ path: path, content: content })
        });
        const result = await response.text();
        if (response.ok) {
            terminal_input.value += '\n' + 'The file is correctly updated.\n'
            await showDialog("The file is correctly updated.")
            if(current_project!==null)
                await GitStatus();
        } else {
            terminal_input.value += "\n" + result + "\n";
            await showDialog(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        await showDialog(error)
    }
}
//create a new Folder
async function CreateFolder(path) {
    try {
        const response = await fetch('http://localhost:8080/api/create/folder', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: path
        });
        const result = await response.text();
        if (response.ok) {
            terminal_input.value += "\n The folder is correctly created.\n"
            await showDialog("The folder is correctly created.")
        } else {
            terminal_input.value += "\n" + result + "\n";
            await showDialog(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        await showDialog(error)
    }

}
//Delete a folder and update files to add
async function DeleteFolder(path) {
    try {
        const response = await fetch('http://localhost:8080/api/delete/folder', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: path
        });
        const result = await response.text();
        if (response.ok) {
            terminal_input.value += "\n The folder is correctly deleted.\n"
            await showDialog("The folder is correctly deleted.")
            if(current_project!==null)
                await GitStatus();
        } else {
            terminal_input.value += "\n" + result + "\n";
            await showDialog(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        await showDialog(error)
    }

}

//Ne marche pas à regarder dans le back
async function Move(src, dst) {
    try {
        const response = await fetch('http://localhost:8080/api/move', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ path: src, content: dst })
        });
        const result = await response.text();
        if (response.ok) {
            terminal_input.value += '\n' + 'The file is correctly updated.\n'
            if (filename.innerHTML === src) {
                CloseFile();
            }
        } else {
            terminal_input.value += "\n" + result + "\n";
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
    }
}

//Récuperer la derniere ligne du Terminal
function getLastLine() {
    const text = terminal_input.value;
    const lines = text.split('\n');
    const lastLine = lines[lines.length - 1];
    return `${lastLine}`;
}
//En soit ne sert à rien, juste a voir si ca compile bien, avec la première phrase dans le terminal
async function loadDefaultFile() {
    try {
        const response = await fetch('terminal.txt');
        if (response.ok) {
            terminal_input.value = await response.text();
        } else {
            console.error('Failed to fetch terminal.txt');
        }
    } catch (error) {
        console.error('Error fetching terminal.txt:', error);
    }
}
//execute une commande du terminal
async function exec_terminal(command) {
    try {
        const response = await fetch('http://localhost:8080/api/execute-command', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ command: command })
        });
        const result = await response.text();
        terminal_input.value += "\n" + result + "\n";
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
    }
}
//met en plein écran la div Element
function toggleFullScreen(element) {
    if (!document.fullscreenElement) {
        if (element.requestFullscreen) {
            element.requestFullscreen();
        } else if (element.mozRequestFullScreen) { // Firefox
            element.mozRequestFullScreen();
        } else if (element.webkitRequestFullscreen) { // Chrome, Safari et Opera
            element.webkitRequestFullscreen();
        } else if (element.msRequestFullscreen) { // Internet Explorer/Edge
            element.msRequestFullscreen();
        }
    } else {
        exitFullScreen();
    }
}

//Sort du grand écran
function exitFullScreen() {
    if (document.fullscreenElement) {
        if (document.exitFullscreen) {
            document.exitFullscreen();
        } else if (document.mozCancelFullScreen) { // Firefox
            document.mozCancelFullScreen();
        } else if (document.webkitExitFullscreen) { // Chrome, Safari et Opera
            document.webkitExitFullscreen();
        } else if (document.msExitFullscreen) { // Internet Explorer/Edge
            document.msExitFullscreen();
        }
    }
}

//regarde si les crédentials sont renseignés dans le back
async function Get_credentials() {
    try {
        const response = await fetch('http://localhost:8080/api/get/credentials', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
        });
        return response.ok;
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
    }
}
//Set les crédentials dans le back
async function Set_credentials(username, password) {
    try {
        const response = await fetch('http://localhost:8080/api/set/credentials', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ identifiant: username, key: password })
        });
        return response.ok;
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
    }
}

//execute la commande IDE
async function IDE_EXEC() {
    const test = getLastLine();
    const commands = test.split(' ');
    //pour open
    if (commands[0] === "open") {
        //un project
        if (commands[1] === "project") {
            await Openproject(commands[2]);
            //un file
        } else if (commands[1] === "file") {
            await OpenFile(commands[2]);
        }
        else {
            terminal_input.value += "\n" + "I can't open a " + commands[1] + " Did you want to say 'open project [absolute_path]' ?\nOr maybe 'open file [absolute_path]'"

        }

        //pour créer
    } else if (commands[0] === "create") {
        //file
        if (commands[1] === "file") {
            await CreateFile(commands[2]);
        }
        //folder
        else if (commands[1] === "folder") {
            await CreateFolder(commands[2]);
        }
        else {
            terminal_input.value += "\n" + "I can't create a " + commands[1] + " Did you want to say 'open folder [absolute_path]' ?\nOr maybe 'open file [absolute_path]'"
            await showDialog("I can't create a " + commands[1] + " Did you want to say 'open folder [absolute_path]' ?\nOr maybe 'open file [absolute_path]'")
        }
    }
    else if (commands[0] === "delete") {
        if (commands[1] === "file") {
            await DeleteFile(commands[2]);
        }
        else if (commands[1] === "folder") {
            await DeleteFolder(commands[2]);
        }

        else {
            terminal_input.value += "\n" + "I can't delete a " + commands[1] + " Did you want to say 'open folder [absolute_path]' ?\nOr maybe 'open file [absolute_path]'"

        }
    }
    else if (commands[0] === "move") {
        await Move(commands[1], commands[2])
    }
    else if(commands[0] === "git")
    {
        if(commands[1] === "add"){
            await Git_ADD(commands[2]);
        }
        else  if(commands[1] === "commit"){
            await GitCommit(commands[2]);
        }
        else  if(commands[1] === "push"){
            await GitPush();
        }
        else if(commands[1] === "pull"){
            await GitPull();
        }
        else if(commands[1] === "tag"){
            await GitTag(commands[2])
        }
        else{
            terminal_input.value+="\n I dont' know this command, if you want to to know available commands enter 'help' and click on Ide Action"
        }
    }
    else if(commands[0] === "close")
    {
        if(commands[1]==="project"){
            CloseProject();
        }
        else if(commands[1]==="file"){
            CloseFile();
        }
    }
    else if (commands[0] === "help"){
        terminal_input.value +=
            "\nAvailables commands are :\n" +
            "   - 'open project [absolute_project_path]'\n"+
            "   - 'open file [absolute_file_path]'\n\n"+
            "   - 'create file [absolute_file_path]'\n"+
            "   - 'create folder [absolute_folder_path]'\n\n"+
            "   - 'delete file [absolute_file_path]'\n"+
            "   - 'delete folder [absolute_folder_path]'\n\n"+
            "   - 'move [src_absolute_folder_or_file_path] [dst_folder_path]'\n\n"+
            "   - 'git add [absolute_path]'\n"+
            "   - 'git commit [commit_message]'\n"+
            "   - 'git push'\n"+
            "   - 'git  pull'\n"+
            "   - 'git tag [tag_message]'\n\n"+
            "   - 'close project' close the current project\n"+
        "   - 'close file' close the in The Code Window\n\n"


    }
    else {
        terminal_input.value += "\n I dont' know this command, if you want to to know available commands enter 'help' and click on Ide Action"
    }
    terminal_input.value += "\n";
}

function exec(contentfile,filename) {
    const exts = filename.split(".")
    const extension = exts[exts.length -1];
    terminal_input.value+=filename+"\nf,erifn,eri   "+extension+"\n"
    outputTextarea.value = ""
    if(extension==="js"){
        try {
            const originalConsoleLog = console.log;

            // Rediriger console.log vers l'élément textarea
            const outputTextarea = document.getElementById("execute_result");
            console.log = function(...args) {
                outputTextarea.value += args.join(" ") + "\n";
            };

            // Exécuter le script
            const executeScript = new Function(contentfile);
            executeScript();

            // Restaurer la console originale
            console.log = originalConsoleLog;
        } catch (error) {
            window.getElementById("execute_result").value += "\nErreur d'exécution du script : " + error.message + "\n";

        }
    }
    else if(extension==="html"){
        shell.openItem(filename);
    }
}

async function Credent(){
    const div = document.getElementById('fullscreenDiv');
    div.classList.toggle('active');
    toggleFullScreen(document.getElementById('fullscreenDiv'));
    const username = document.getElementById("identifiant");
    const pasword = document.getElementById("motDePasse");
    document.getElementById('save_cred').addEventListener('click', async function () {
        try {
            const result = await Set_credentials(username.value, pasword.value);
            if (result) {
                div.classList.remove('active');
                if (document.fullscreenElement) {
                    await document.exitFullscreen();
                }
            } else {
                console.log('Failed to set credentials');
            }
        } catch (error) {
            console.error('Error setting credentials:', error);
        }
    });
}
function Color_Bold(list_to_color) {
    const fileList = document.getElementById('fileList');
    const buttons = fileList.getElementsByTagName('button');

    for (const button of buttons) {
        if (list_to_color.includes(button.innerHTML)) {
            terminal_input.value+=button.innerHTML+" yes\n"
            button.style.color = 'red';
            button.style.fontWeight = 'bold';
        } else {
            terminal_input.value+=button.innerHTML+" nop\n"
            button.style.color = 'black';
            button.style.fontWeight = 'normal';
        }
    }
    terminal_input.value+=list_to_color
}


function getType(obj) {
    if (Array.isArray(obj)) {
      return 'Array';
    } else if (obj instanceof Date) {
      return 'Date';
    } else if (obj instanceof Function) {
      return 'Function';
    } else if (obj === null) {
      return 'null';
    } else if (obj === undefined) {
      return 'undefined';
    } else if (typeof obj === 'object') {
      return 'Object';
    } else {
      return typeof obj;
    }
  }
//AnySearch
async  function AnySearch(word){
    try {


            const response = await fetch('http://localhost:8080/api/Search', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ current_project: current_project, commit: word })
            });
            const result = await response.text();
            if (response.ok) {
                //const resultModified = result.replace(/\\/g, "\\\\");
                //terminal_input.value += "\n" + resultModified;
                const listA = JSON.parse(result);
                terminal_input.value += '\n' + "We've found the word in "+listA.length+" files "+getType(listA)+"\n"
                let index = 0;
// for (let element of listA) {
//  terminal_input.value+=`Element at index ${index} is of type: ${typeof element}\n`;
//   index++;
// }
                if(listA.length > 0){
                    Color_Bold(listA);
                }

            } else {
                terminal_input.value += "\nresss" + result + "\n";
            }
    } catch (error) {
        terminal_input.value += "\nerr" + error + "\n";
    }

}
// actions
document.addEventListener('DOMContentLoaded', async () => {
    // regarde si il y a des credentials
    let cred = await Get_credentials();

    console.log('JavaScript loaded');

    // execute la shell command
    shell_ici.addEventListener('click', async function () {
        const test = getLastLine();
        await exec_terminal(test);
    });
    //execute l'ide command
    ideButton.addEventListener('click', async function () {
        await IDE_EXEC();
    });

    //commit
    Commit_Button.addEventListener('click', async function () {
        const commit_mess = document.getElementById("commit_message")
        await GitCommit(commit_mess.value);
    });
    //push
    Push_Button.addEventListener('click', async function () {
        const tag_mess = document.getElementById("tag_message")
        //tag si il y a un messag dans tag
        if(tag_mess.value!==""  )
            await GitTag(tag_mess.value);
        await GitPush();
    });
    Pull_Button.addEventListener('click', async function () {
        await GitPull();
    });
    execute.addEventListener('click', async function () {
        //tag si il y a un messag dans tag
        exec(code_display.value,filename.innerHTML)
    });

    //Raccourcis
    document.addEventListener('keydown', async  function (event) {
        //Sauvegarde le fichier actuel dans CodeWindow
        if (event.ctrlKey && event.key === 'S'||event.ctrlKey && event.key === 's') {
            const path = filename.innerHTML;
            const content = code_display.value;
            UpdateFile(path, content);
        }
        if (event.ctrlKey && event.key === 'G'||event.ctrlKey && event.key === 'g') {
            Credent();
        }
        //Supprime le fichier actuel de Code Window
        if (event.ctrlKey && event.key === 'D'||event.ctrlKey && event.key === 'd') {
            event.preventDefault();
            const path = filename.innerHTML;
            DeleteFile(path)
        }
        //any search
        if (event.ctrlKey && event.key === 'F'||event.ctrlKey && event.key === 'f') {
            event.preventDefault();
            const div = document.getElementById('Option');
            div.classList.toggle('active');
            toggleFullScreen(document.getElementById('Option'));
            document.getElementById("search_button").addEventListener('click', async function(){
                const word = document.getElementById("word_searchh");
                terminal_input.value+="alooor"
                await AnySearch(word.value);
                terminal_input.value+=word.value
                div.classList.remove('active');
                if (document.fullscreenElement) {
                    await document.exitFullscreen();
                    terminal_input.value+="sssssalooor"
                }
            })
  
        }
        //sort du plein écran
        if (event.key === 'Escape') {
            exitFullScreen();
        }
        //ouvre en grand CodeWindow
        if (event.ctrlKey && event.shiftKey && event.key === 'C'||event.ctrlKey && event.shiftKey && event.key === 'c') {
            event.preventDefault();
            toggleFullScreen(codepanel);
        }
        //ouvre en grand ArchiWindow
        if (event.ctrlKey && event.shiftKey && event.key === 'A'||event.ctrlKey && event.shiftKey && event.key === 'a') {
            event.preventDefault();
            toggleFullScreen(archipanel);
        }
        //ouvre en grand ExecutionWindow
        if (event.ctrlKey && event.shiftKey && event.key === 'R'||event.ctrlKey && event.shiftKey && event.key === 'r') {
            event.preventDefault();
            toggleFullScreen(archipanel);
        }
        //ouvre en grand Terminal Window
        if (event.ctrlKey && event.shiftKey && event.key === 'T' || event.ctrlKey && event.shiftKey && event.key === 't') {
            event.preventDefault();
            toggleFullScreen(terminalpanel);
        }
        //Ouvre en grand Shortcuts window
        if (event.ctrlKey && event.shiftKey && event.key === 'S'||event.ctrlKey && event.shiftKey && event.key === 's') {
            event.preventDefault();
            toggleFullScreen(shortcutspanel);
        }
        //ferme le fichier actuel dans Code Window
        if (event.ctrlKey && event.key === 'K' || event.ctrlKey && event.key === 'k') {
            event.preventDefault();
            CloseFile();
        }
        //shortcut for the IDE_Button
        if (event.ctrlKey && event.altKey && event.key === 'I'|| event.ctrlKey && event.altKey && event.key === 'i'){
            event.preventDefault();
            ideButton.click();
        }
        //shortcut for Execute_Button
        if (event.ctrlKey && event.altKey && event.key === 'E'||event.ctrlKey && event.altKey && event.key === 'e'){
            event.preventDefault();
            shell_ici.click();
        }
        //shortcut for Push
        if (event.ctrlKey && event.altKey && event.key === 'P'||event.ctrlKey && event.altKey && event.key === 'p'){
            event.preventDefault();
            Push_Button.click();
        }
        //shortcut for commit
        if (event.ctrlKey && event.altKey && event.key === 'M'||event.ctrlKey && event.altKey && event.key === 'm'){
            event.preventDefault();
            Commit_Button.click();
        }
        if (event.ctrlKey && event.altKey && event.key === 'L'||event.ctrlKey && event.altKey && event.key === 'l'){
            event.preventDefault();
            Pull_Button.click();
        }
    });



    //affiche le terminal par défault
    await loadDefaultFile();

    //Si il n'y a pas de crédentials de renseignés, les demandes
    if (!cred) {
        await Credent();
    }

});
