const ideButton = document.getElementById("idebutton");
const code_display = document.getElementById("code_data");
const terminal_input = document.getElementById("fileInput");
const fileList = document.getElementById("fileList");
const shell = document.getElementById("shell_command");
const filename = document.getElementById("bonjour");
const codepanel = document.getElementById("code");
const terminalpanel = document.getElementById("terminal");
const shortcutspanel = document.getElementById("shortcuts");

let current_project = null;
let to_add = [];


class ConfigWindow {

  constructor(fileList) {
    this.fileList = fileList;
  }

  getFileList() {
    return this.fileList;
  }

}

function displayProjectArchitecture(configWindow) {
  for (const filePath of configWindow.getFileList()) {
    const filePathHTML = document.createElement('li');
    const filePathButtonHTML = document.createElement('button');
    filePathButtonHTML.addEventListener("click", async () => {
      await OpenFile(filePath);
    })
    filePathButtonHTML.innerHTML = filePath;
    filePathHTML.appendChild(filePathButtonHTML);
    fileList.appendChild(filePathHTML);
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
        const configWindow = new ConfigWindow(JSON.parse(result));
        displayProjectArchitecture(configWindow);
        current_project = path;
        terminal_input.value += "\n The project is correctly open, we've charge the pom.xml file in the Code Windows for you.\n";
        // try {
        //   const response = await fetch('http://localhost:8080/api/execFeature/status', {
        //     method: 'POST',
        //     headers: {
        //       'Content-Type': 'application/json'
        //     },
        //     body: path
        //   });
        //   const result = await response.text();
        //   if (response.ok) {
        //     let list;
        //     try {
        //       list = JSON.parse(result);
        //   } catch (error) {
        //       terminal_input.value+=("\nFailed to parse string as JSON:", error);
        //   }
        //   if (Array.isArray(list)) {
        //     const buttonContainer = document.getElementById('buttonContainer');
        //     list.forEach(item => {
        //       const button = document.createElement('button');
        //       button.textContent = item; // Le texte du bouton est l'élément de la liste
        //       button.classList.add('btn'); // Ajouter une classe si nécessaire
        //       button.addEventListener('click', () => {
        //           Git_ADD(button.textContent)
        //       });
        //
        //       buttonContainer.appendChild(button);
        //   });
        // } else {
        //     terminal_input.value += ("The parsed result is not an array.");
        // }
        //
        //
        //   } else {
        //     terminal_input.value += "\n"+result+"\n";
        //   }
        // } catch (error) {
        //   terminal_input.value += "\n"+error+"\n";
        // }
    } else {
      terminal_input.value += "\n"+path.concat("/pom.xml");
    }
  } catch (error) {
    terminal_input.value = error;
  }
}
async function Git_ADD(path){
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
            terminal_input.value+=("\nFailed to parse string as JSON:", error);
        }
        if (Array.isArray(list)) {
          const buttonContainer = document.getElementById('buttonContainer');
          list.forEach(item => {
            const button = document.createElement('button');
            button.textContent = item; // Le texte du bouton est l'élément de la liste
            button.classList.add('btn'); // Ajouter une classe si nécessaire
            button.addEventListener('click', () => {
                Git_ADD()
            });
    
            buttonContainer.appendChild(button);
        });
      } else {
          terminal_input.value +=("The parsed result is not an array.");
      }

            
        } else {
          terminal_input.value += "\n"+result+"\n";
        }
      } catch (error) {
        terminal_input.value += "\n"+error+"\n";
      }
    } else {
      terminal_input.value += "\n" + result + "\n";
    }
  } catch (error) {
    terminal_input.value += "\n" + error + "\n";
  }
}
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
      } else {
        console.error('Failed to fetch ' + result);
      }
    } else {
      terminal_input.value += "\n" + result + "\n";
    }
  } catch (error) {
    terminal_input.value += "\n" + error + "\n";
  }
}

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
    } else {
      terminal_input.value += "\n" + result + "\n";
    }
  } catch (error) {
    terminal_input.value += "\n" + error + "\n";
  }
}

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
        filename.innerHTML="";
        code_display.value="";
        terminal_input.value +="\n The file is correctly deleted.\n"
        if(to_add.find(path))
          {
            to_add = to_add.filter(item => item !== path)
          }

    } else {
      terminal_input.value += "\n" + result + "\n";
    }
  } catch (error) {
    terminal_input.value += "\n" + error + "\n";
  }
}
function CloseFile(){
     code_display.value = ""
     filename.innerHTML=""
}
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
      terminal_input.value +='\n'+'The file is correctly updated.\n'
    } else {
      terminal_input.value += "\n" + result + "\n";
    }
  } catch (error) {
    terminal_input.value += "\n" + error + "\n";
  }
}

async function CreateFolder(path){
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
      terminal_input.value+="\n The folder is correctly created.\n"
    } else {
      terminal_input.value += "\n" + result + "\n";
    }
  } catch (error) {
    terminal_input.value += "\n" + error + "\n";
  }

}

async function DeleteFolder(path){
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
      terminal_input.value+="\n The folder is correctly deleted.\n"
    } else {
      terminal_input.value += "\n" + result + "\n";
    }
  } catch (error) {
    terminal_input.value += "\n" + error + "\n";
  }

}
async function Move(src,dst){
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
      terminal_input.value +='\n'+'The file is correctly updated.\n'
      if(filename.innerHTML==src){
        CloseFile();
      } 
    } else {
      terminal_input.value += "\n" + result + "\n";
    }
  } catch (error) {
    terminal_input.value += "\n" + error + "\n";
  }
}
function getLastLine() {
  const text = terminal_input.value;
  const lines = text.split('\n');
  const lastLine = lines[lines.length - 1];
  return `${lastLine}`;
}

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
async function Get_credentials(){
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

async function Set_credentials(username, password){
  try {
    const response = await fetch('http://localhost:8080/api/set/credentials', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body:JSON.stringify({ identifiant: username, key: password })
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

document.addEventListener('DOMContentLoaded', async () => {

  let cred = await Get_credentials();
  if(cred){
  console.log('JavaScript loaded');
  fullscreenDiv.classList.remove('active');
  shell.addEventListener('click', async function () {
    const test = getLastLine();
    exec_terminal(test);
  });

  ideButton.addEventListener('click', async function () {
    const test = getLastLine();
    const commands = test.split(' ');
    if (commands[0] === "open") {
      if (commands[1] === "project") {
        Openproject(commands[2]);
      } else if(commands[1] === "file") {
        OpenFile(commands[2]);
      }
      else{
        terminal_input.value +="\n"+"I can't open a "+commands[1]+" Did you want to say 'open project [absolute_path]' ?\nOr maybe 'open file [absolute_path]'"
      }
    } else if (commands[0] === "create") {
      if (commands[1] === "file") {
        CreateFile(commands[2]);
      }
      else if(commands[1]==="folder"){
        CreateFolder(commands[2]);
      }
      else{
        terminal_input.value +="\n"+"I can't create a "+commands[1]+" Did you want to say 'open folder [absolute_path]' ?\nOr maybe 'open file [absolute_path]'"

      }
    }
    else if (commands[0] === "delete") {
      if (commands[1] === "file") {
        DeleteFile(commands[2]);
      }
      else if(commands[1]==="folder"){
        DeleteFolder(commands[2]);
      }
      
      else{
        terminal_input.value +="\n"+"I can't delete a "+commands[1]+" Did you want to say 'open folder [absolute_path]' ?\nOr maybe 'open file [absolute_path]'"

      }
    }
    else if(commands[0]==="move"){
      Move(commands[1],commands[2])
    }
    else {

      terminal_input.value +="\n I dont' know this command, if you want to to know available commands enter 'help' and click on Ide Action"

    }
    terminal_input.value +="\n"
  });
  document.addEventListener('keydown', function (event) {

    if (event.ctrlKey && event.key === 'S') {
      const path = filename.innerHTML;
      const content = code_display.value;
      UpdateFile(path, content);
    }
    if (event.ctrlKey && event.key === 'D') {
      event.preventDefault();
      const path = filename.innerHTML;
      DeleteFile(path)
    }
    if (event.ctrlKey && event.shiftKey && event.key === 'C') {
      event.preventDefault();
      toggleFullScreen(codepanel);
    }
    if (event.key === 'Escape') {
      exitFullScreen();
    }
    if (event.ctrlKey && event.shiftKey && event.key === 'T') {
      event.preventDefault();
      toggleFullScreen(terminalpanel);
    }
    if (event.ctrlKey && event.shiftKey && event.key === 'S') {
      event.preventDefault();
      toggleFullScreen(shortcutspanel);
    }
    if (event.ctrlKey && event.key === 'K') {
      event.preventDefault();
      CloseFile();
    }
  });


  
  loadDefaultFile();
}

else{
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
    }  });
}

});
