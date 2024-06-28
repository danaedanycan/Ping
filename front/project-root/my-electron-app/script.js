const Test = document.getElementById("idebutton");
const toto = document.getElementById("code_data");
const fileInput = document.getElementById("fileInput");
const shell = document.getElementById("shell_command");
function getLastLine() {
  const text = fileInput.value;
  const lines = text.split('\n');
  const lastLine = lines[lines.length - 1];
  return `${lastLine}`;
}

async function loadDefaultFile() {
  try {
    const response = await fetch('terminal.txt');
    if (response.ok) {
      const text = await response.text();
      fileInput.value = text;
    } else {
      console.error('Failed to fetch terminal.txt');
    }
  } catch (error) {
    console.error('Error fetching terminal.txt:', error);
  }
}

async function Openproject(path){
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
      try {
        const response = await fetch(result);
        if (response.ok) {
          const text = await response.text();
          toto.value = text;
        } else {
          console.error('Failed to fetch terminal.txt');
        }
      } catch (error) {
        console.error('Error fetching terminal.txt:', error);
      }
    } else {
      toto.value = result;
    }
  } catch (error) {
    toto.value = error;
  }
}


async function OpenFile(path){
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
      try {
        const response = await fetch(result);
        if (response.ok) {
          const text = await response.text();
          toto.value = text;
        } else {
          console.error('Failed to fetch terminal.txt');
        }
      } catch (error) {
        console.error('Error fetching terminal.txt:', error);
      }
    } else {
      toto.value = result;
    }
  } catch (error) {
    toto.value = error;
  }
}

 async function exec_terminal(command){
  try {
    const response = await fetch('http://localhost:8080/api/execute-command', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ command: test })
    });
    const result = await response.text();
    if (response.ok) {
      fileInput.value += "\n"+result+"\n";
    } else {
      fileInput.value += "\n"+result+"\n";
    }
  } catch (error) {
    fileInput.value += "\n"+error+"\n";
  }
 }



document.addEventListener('DOMContentLoaded', () => {
  console.log('JavaScript loaded');



  shell.addEventListener('click', async function() {
    const test = getLastLine();
    exec_terminal(test);
  });

  Test.addEventListener('click', async function() {
    const test = getLastLine();
    const commands = test.split(' ');
    if(commands[0]=="open"){
      if(commands[1]=="project"){
        Openproject(commands[2]);
      }
      else{
        OpenFile(commands[2]);
      }
    }
    
  });


  
  loadDefaultFile();
});
