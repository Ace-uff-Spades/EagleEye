œ# file: rfcomm-server.py
# auth: Albert Huang <albert@csail.mit.edu>
# desc: simple demonstration of a server application that uses RFCOMM sockets
#
# $Id: rfcomm-server.py 518 2007-08-10 07:20:07Z albert $

from bluetooth import *
import io
import time
import os
import sys
import re
from PIL import Image
from os import path

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("", PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

advertise_service( server_sock, "SampleServer",
                   service_id = uuid,
                   service_classes = [uuid],
                   profiles = [ SERIAL_PORT_PROFILE ], 
#                   protocols = [ OBEX_UUID ] 
                    )
#, SERIAL_PORT_CLASS ],
                   
print("Waiting for connection on RFCOMM channel %d" % port)

client_sock, client_info = server_sock.accept()
print("Accepted connection from ", client_info)

newPid = os.fork()


if newPid==0:
    try:
        imageNumber = 0
        while True:
            data = client_sock.recv(1024)
            pictureData = b''
            if data=='finished':
                closeFile = open('outputData/potato.txt','w+')
                closeFile.close()
            elif data=='sendpictures':
                print("downloading picture %d..."%imageNumber)
                a=0
                while True:
                    client_sock.send('ok'.encode())
                    newData=client_sock.recv(1024)
                    if newData=='done':
                        break
                    pictureData = pictureData+newData
                im = Image.open(io.BytesIO(pictureData))
                im.save('outputData/image{%d}.jpg'%imageNumber)
                imageNumber=imageNumber+1
                client_sock.send('done'.encode())
            else:
                print("Creating Locations File...")
                while True:
                    client_sock.send('ok'.encode())
                    newData = client_sock.recv(1024)
                    if newData=='done':
                        break
                    if path.exists('outputData/locationFile.txt'):
                        locationFile=open('outputData/locationFile.txt','a')
                        locationFile.write(newData+", ")
                        locationFile.close()
                    else:
                        locationFile2=open('outputData/locationFile.txt','w+')
                        locationFile2.write(newData+", ")
                        locationFile2.close()
    except IOError:
         pass
else:
    try:
        while True:
            if path.exists("inputData/output.txt"):
                file = open("inputData/output.txt","r")
                contents = file.read()
                list = re.findall(r"[\w']+",contents)
                for i in list:
                     client_sock.send(i.encode())
                     time.sleep(2)
                os.remove("inputData/output.txt")
                client_sock.send('listView'.encode())
    except IOError:
        pass
print("disconnected")
client_sock.close()
server_sock.close()
print("all done")

