# Documentation of Eclipse sensiNact

The documentation entry point is [`source/index.md`](source/index.md).

## How to compile

### Setup a Python virtual environment

1. Create the environment: `python -m venv venv`
2. Activate it according to your interpreter:
   * Bash: `. .\venv\bin\activate`
   * Powershell: `. .\venv\Scripts\Activate.ps1`
   * CMD: `.\venv\Scripts\activate.bat`
3. Install requirements:
   * `pip install -r requirements.txt`
   * **Note:** the requirements contains a link to a Git repository, you might need to add `--use-pep517` argument.
4. Compile the project with the Sphinx HTML builder:
   * Bash: `make clean html`
   * Powershell: `.\make.ps1 clean && .\make.ps1 html`
   * CMD: `make clean & make html`
5. The documentation HTML project is in the `build/html` folder, starting at [index.html](./build/html/index.html).
