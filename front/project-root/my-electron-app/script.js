const ideButton = document.getElementById("idebutton");
const code_display = document.getElementById("code_data");
const terminal_input = document.getElementById("fileInput");
const shell = document.getElementById("shell_command");
const filename = document.getElementById("bonjour");
const codepanel = document.getElementById("code");
const terminalpanel = document.getElementById("terminal");
const shortcutspanel = document.getElementById("shortcuts");

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
      const response = await fetch(result);
      if (response.ok) {
        const text = await response.text();
        code_display.value = text;
        filename.innerHTML = result;
      } else {
        console.error('Failed to fetch terminal.txt');
      }
    } else {
      terminal_input.value = result;
    }
  } catch (error) {
    terminal_input.value = error;
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
      terminal_input.value = "\n" + result + "\n";
    }
  } catch (error) {
    terminal_input.value = "\n" + error + "\n";
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
      OpenFile(path);
    } else {
      terminal_input.value = "\n" + result + "\n";
    }
  } catch (error) {
    terminal_input.value = "\n" + error + "\n";
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
      const path2 = filename.innerHTML;
      if(path == path2){
        filename.innerHTML="";
        code_display.value="";
      }
    } else {
      terminal_input.value = "\n" + result + "\n";
    }
  } catch (error) {
    terminal_input.value = "\n" + error + "\n";
  }
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
    /*const result = await response.text();
    if (response.ok) {
      OpenFile(path); 
    } else {
      terminal_input.value = "\n" + result + "\n";
    }*/
  } catch (error) {
    terminal_input.value = "\n" + error + "\n";
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

document.addEventListener('DOMContentLoaded', () => {
  console.log('JavaScript loaded');

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
      } else {
        OpenFile(commands[2]);
      }
    } else if (commands[0] === "create") {
      if (commands[1] === "file") {
        CreateFile(commands[2]);
      }
    }
    else if (commands[0] === "delete") {
      if (commands[1] === "file") {
        DeleteFile(commands[2]);
      }
    }
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
      toggleFullScreen(terminalpanelpanel);
    }
    if (event.ctrlKey && event.shiftKey && event.key === 'S') {
      event.preventDefault();
      toggleFullScreen(shortcutspanel);
    }
  });
  loadDefaultFile();
});
