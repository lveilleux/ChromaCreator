import os
import zipfile

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FILENAME = "Chroma.zip"
CHROMAFILE = FILENAME.replace(".zip", '.razerchroma')

def main():
    PROJ_DIR = BASE_DIR
    filepath = os.path.join(PROJ_DIR, "output")
    zipf = zipfile.ZipFile(FILENAME, 'w')
    print(PROJ_DIR)
    print(filepath)
    try:
        os.listdir(filepath)
    except FileNotFoundError:
        print("Folder 'output' not found.")
        exit()
    try:
        print("FILES:\n")
        for file in os.listdir(filepath):
            print(file)
            zipf.write(os.path.join(filepath, file), file)
    except Exception as e:
        print("FUCK " + str(e))

    zipf.close()
    print(os.listdir(PROJ_DIR))
    for file in os.listdir(PROJ_DIR):
        if FILENAME == file:
            os.rename(file, CHROMAFILE)

main()
