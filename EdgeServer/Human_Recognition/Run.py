import os
import glob
import time

def runMLscript ():
    return os.system('python ObjectDetection.py')

def deleteFiles():
    files = glob.glob('./input/*')
    for f in files:
        os.remove(f)

def checkIfDone():
    try:
        #checks for .txt file from drone indicating all pictures have shared
        f = open('./input/potato.txt')
        f.close()

        print('Drone uploaded all pictures! \n Running ML algorthim...')

        runMLscript()

        print('Deleting files...')

        #deleteFiles()

    except FileNotFoundError:
        print('')

def autoCheck () :
    while (True):
        checkIfDone()
        time.sleep(2)

autoCheck()
	