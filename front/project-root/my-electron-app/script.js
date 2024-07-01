const ideButton = document.getElementById("idebutton");
const code_display = document.getElementById("code_data");
const terminal_input = document.getElementById("fileInput");
const shell = document.getElementById("shell_command");
const filename = document.getElementById("bonjour");
const codepanel = document.getElementById("code");
const terminalpanel = document.getElementById("terminal");
const shortcutspanel = document.getElementById("shortcuts");
const Commit_Button = document.getElementById("commit_button");
const Push_Button = document.getElementById("push_button");
let current_project = null;
let to_add = [];

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
                terminal_input.value += ("\nFailed to parse string as JSON:", error);
                alert("\nFailed to parse string as JSON: "+ error)
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
                alert("The parsed result is not an array.")
            }


        } else {
            terminal_input.value += "\n" + result + "\n";
            alert(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        alert(error)
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
            GitStatus();
            alert("ADD is succesfull We remove it from the files to add list.")
        } else {
            terminal_input.value += "\n" + result + "\n";
            alert(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        alert(error)
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
            alert("Commit is Successfull ! You can Push !")
        } else {
            terminal_input.value += "\efzfen" + result + "\n";
            alert("Commit is not Successfull !")
        }
    } catch (error) {
        terminal_input.value += "\nggrr" + error + "\n";
        alert(error)
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
          alert("Commit is successfuly tag!")
      } else {
          terminal_input.value += "\n" + result + "\n";
          alert(result)
      }
  } catch (error) {
      terminal_input.value += "\n" + error + "\n";
      alert(error)
  }
}
//push
async function GitPush() {
  try {
      const response = await fetch('http://localhost:8080/api/execFeature/Gitpush', {
          method: 'POST',
          headers: {
              'Content-Type': 'application/json'
          },
          body:current_project
      });
      const result = await response.text();
      if (response.ok) {
          alert("Push is Successfull! Congratulation !")
      } else {
        alert("Push isn't Successfull, maybe you need to PULL, or set correctly your git credentials")
      }
  } catch (error) {
      terminal_input.value += "\n" + error + "\n";
      alert(error)
  }
}

//open a project and charge Files to add in GitWindow
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

            current_project = path;
            terminal_input.value += "\n The project is correctly open !\n";
            alert("The project is correctly open !")
            GitStatus();
        } else {
            terminal_input.value += "\n" + result+"\n";
            alert(result);
        }
    } catch (error) {
        terminal_input.value += "\n" + error+"\n";
        alert(error)
    }
}
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
                const text = await response.text();
                code_display.value = text;
                filename.innerHTML = result;
                alert("File "+result+" is open in Code Window")
            } else {
              terminal_input.value += "\n" + result + "\n";
                alert(result)
            }
        } else {
            terminal_input.value += "\n" + result + "\n";
            alert(result)
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
            OpenFile(result);
            to_add.push(result);
            terminal_input.value += "\n The file is correctly created.\n";
            alert("The file is correctly created.")
            GitStatus();
        } else {
            terminal_input.value += "\n" + result + "\n";
            alert(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        alert(error)
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
            alert("The file is correctly deleted.")
            if (to_add.find(path)) {
                to_add = to_add.filter(item => item !== path)
            }
            GitStatus();
        } else {
            terminal_input.value += "\n" + result + "\n";
            alert(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        alert(error)
    }
}
//close A file from CodeWindow
function CloseFile() {
    code_display.value = ""
    filename.innerHTML = ""
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
            alert("The file is correctly updated.")
            GitStatus();
        } else {
            terminal_input.value += "\n" + result + "\n";
            alert(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        alert(error)
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
            alert("The folder is correctly created.")
        } else {
            terminal_input.value += "\n" + result + "\n";
            alert(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        alert(error)
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
            alert("The folder is correctly deleted.")
            GitStatus();
        } else {
            terminal_input.value += "\n" + result + "\n";
            alert(result)
        }
    } catch (error) {
        terminal_input.value += "\n" + error + "\n";
        alert(error)
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
            if (filename.innerHTML == src) {
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
            const text = await response.text();
            terminal_input.value = text;
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
        if (response.ok) {
            return true;
        } else {
            return false;
        }
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
        if (response.ok) {
            return true;
        } else {
            return false;
        }
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
            Openproject(commands[2]);
            //un file
        } else if (commands[1] === "file") {
            OpenFile(commands[2]);
        }
        else {
            terminal_input.value += "\n" + "I can't open a " + commands[1] + " Did you want to say 'open project [absolute_path]' ?\nOr maybe 'open file [absolute_path]'"

          } 

          //pour créer
    } else if (commands[0] === "create") {
      //file
        if (commands[1] === "file") {
            CreateFile(commands[2]);
        }
        //folder
        else if (commands[1] === "folder") {
            CreateFolder(commands[2]);
        }
        else {
            terminal_input.value += "\n" + "I can't create a " + commands[1] + " Did you want to say 'open folder [absolute_path]' ?\nOr maybe 'open file [absolute_path]'"
            alert("I can't create a " + commands[1] + " Did you want to say 'open folder [absolute_path]' ?\nOr maybe 'open file [absolute_path]'")
        }
    }
    else if (commands[0] === "delete") {
        if (commands[1] === "file") {
            DeleteFile(commands[2]);
        }
        else if (commands[1] === "folder") {
            DeleteFolder(commands[2]);
        }

        else {
            terminal_input.value += "\n" + "I can't delete a " + commands[1] + " Did you want to say 'open folder [absolute_path]' ?\nOr maybe 'open file [absolute_path]'"

        }
    }
    else if (commands[0] === "move") {
        Move(commands[1], commands[2])
    }
    else {
        terminal_input.value += "\n I dont' know this command, if you want to to know available commands enter 'help' and click on Ide Action"
    }
    terminal_input.value += "\n"
}
// actions 
document.addEventListener('DOMContentLoaded', async () => {
  // regarde si il y a des credentials
    let cred = await Get_credentials();
   
        console.log('JavaScript loaded');
        fullscreenDiv.classList.remove('active');
        // execute la shell command
        shell.addEventListener('click', async function () {
            const test = getLastLine();
            exec_terminal(test);
        });
        //execute l'ide command
        ideButton.addEventListener('click', async function () {
            IDE_EXEC();
        });

        //commit
        Commit_Button.addEventListener('click', async function () {
            const commit_mess = document.getElementById("commit_message")
            GitCommit(commit_mess.value);
        });
        //push
        Push_Button.addEventListener('click', async function () {
          const tag_mess = document.getElementById("tag_message")
          //tag si il y a un messag dans tag
          if(tag_mess.value!=""  )
              GitTag();
          GitPush();
      });

      //Raccourcis
        document.addEventListener('keydown', function (event) {
            //Sauvegarde le fichier actuel dans CodeWindow
            if (event.ctrlKey && event.key === 'S') {
                const path = filename.innerHTML;
                const content = code_display.value;
                UpdateFile(path, content);
            }
            //Supprime le fichier actuel de Code Window
            if (event.ctrlKey && event.key === 'D') {
                event.preventDefault();
                const path = filename.innerHTML;
                DeleteFile(path)
            }
            //ouvre en grand CodeWindow
            if (event.ctrlKey && event.shiftKey && event.key === 'C') {
                event.preventDefault();
                toggleFullScreen(codepanel);
            }
            //sort du plein écran
            if (event.key === 'Escape') {
                exitFullScreen();
            }
            //ouvre en grand Terminal Window
            if (event.ctrlKey && event.shiftKey && event.key === 'T') {
                event.preventDefault();
                toggleFullScreen(terminalpanel);
            }
            //Ouvre en grand Shortcuts window
            if (event.ctrlKey && event.shiftKey && event.key === 'S') {
                event.preventDefault();
                toggleFullScreen(shortcutspanel);
            }
            //ferme le fichier actuel dans Code Window
            if (event.ctrlKey && event.key === 'K') {
                event.preventDefault();
                CloseFile();
            }
        });


        //affiche le terminal par défault
        loadDefaultFile();

        //Si il n'y a pas de crédentials de renseignés, les demandes
        if (!cred) {
        var div = document.getElementById('fullscreenDiv');
        div.classList.toggle('active');
        toggleFullScreen(document.getElementById('fullscreenDiv'));
        const username = document.getElementById("identifiant");
        const pasword = document.getElementById("motDePasse");
        document.getElementById('save_cred').addEventListener('click', async function () {
            try {
                const result = await Set_credentials(username.value, pasword.value);
                if (result) {
                    fullscreenDiv.classList.remove('active');
                    if (document.fullscreenElement) {
                        document.exitFullscreen();
                    }
                } else {
                    console.log('Failed to set credentials');
                }
            } catch (error) {
                console.error('Error setting credentials:', error);
            }
        });
    }

});
