import os
import zipfile
from optparse import OptionParser

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
storeFileName = "Chroma"


parser = OptionParser(description="Chroma Profile Creator", version="%prog 1.0")
parser.add_option('--name', dest='storeFileName', action='store', default="Chroma",
                  help="Name of .razerchroma file")
(options, args) = parser.parse_args()

storeFileName += ".zip"
CHROMAFILE = storeFileName.replace(".zip", '.razerchroma')

def main():
    PROJ_DIR = BASE_DIR
    filepath = os.path.join(PROJ_DIR, "output")
    zipf = zipfile.ZipFile(storeFileName, 'w')
    try:
        os.listdir(filepath)
    except FileNotFoundError:
        print("Folder 'output' not found.")
        exit()
    try:
        for file in os.listdir(filepath):
            if ".xml" in file:
                zipf.write(os.path.join(filepath, file), file)
    except Exception as e:
        print("FUCK " + str(e))

    zipf.close()
    print(os.listdir(PROJ_DIR))
    for file in os.listdir(PROJ_DIR):
        if file == CHROMAFILE:
            os.remove(file)
        elif storeFileName == file:
            os.rename(file, CHROMAFILE)

main()
