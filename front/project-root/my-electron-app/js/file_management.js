import { code_display, terminal_input, filename } from '../script.js'; // Corrigez le chemin si nécessaire

async function Openproject(path) {
  try {
    const response = await fetch('http://localhost:8080/api/open/project', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ path: path }) // Corrigez la structure de la requête si nécessaire
    });
    const result = await response.text();
    if (response.ok) {
      const response = await fetch(result);
      if (response.ok) {
        const text = await response.text();
        code_display.value = text;
      } else {
        console.error('Failed to fetch terminal.txt');
      }
    } else {
      code_display.value = result;
    }
  } catch (error) {
    code_display.value = error;
  }
}

async function OpenFile(path) {
  try {
    const response = await fetch('http://localhost:8080/api/open/file', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ path: path }) // Corrigez la structure de la requête si nécessaire
    });
    const result = await response.text();
    if (response.ok) {
      const response = await fetch(result);
      if (response.ok) {
        const text = await response.text();
        code_display.value = text;
        let names = result.split('/');
        filename.value = names[names.length - 1];
      } else {
        console.error('Failed to fetch ' + result);
      }
    } else {
      terminal_input.value = "\n" + result + "\n";
    }
  } catch (error) {
    code_display.value = "\n" + error + "\n";
  }
}

async function CreateFile(path) {
  try {
    const response = await fetch('http://localhost:8080/api/create/file', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ path: path }) // Corrigez la structure de la requête si nécessaire
    });
    const result = await response.text();
    if (response.ok) {
      OpenFile(path); // Appeler la fonction OpenFile pour ouvrir le fichier nouvellement créé
    } else {
      terminal_input.value = "\n" + result + "\n";
    }
  } catch (error) {
    terminal_input.value = "\n" + error + "\n";
  }
}

export {
  OpenFile,
  Openproject,
  CreateFile
};
