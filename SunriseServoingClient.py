import socket
import sys
import tkinter
from tkinter import *
from ctypes import windll, Structure, c_long, byref

mouseDown = False
HOST = '172.31.1.147'  # The server's hostname or IP address
PORT = 30004           # The port used by the server
REFRESH_RATE = 100     # frequency that commands are sent to server


class point(Structure):
    _fields_ = [("x", c_long), ("y", c_long)]
    
def queryMousePosition():
    pt = point()
    windll.user32.GetCursorPos(byref(pt))
    return { "x": pt.x, "y": pt.y}

def startServer():
    lbl.configure(text="Connecting to Server..")
    pos = queryMousePosition()
    print(pos)
    # Create a TCP/IP socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # Connect the socket to the port where the server is listening
    server_address = (HOST, PORT)
    print ( 'connecting to %s port %s' % server_address )
    try:
        sock.connect(server_address)
        lbl.configure(text='connection to %s port %s success' % server_address )
    except socket.error:
        print ("Couldnt connect with the socket-server.")
        lbl.configure(text="Connection failed")
        #sys.exit(1)
    
def cyclicTask():
    global SCREEN_WIDTH
    global SCREEN_HEIGHT
    window.after(REFRESH_RATE, cyclicTask)
    if mouseDown:
        pt = point()
        pos = windll.user32.GetCursorPos(byref(pt))
        print( pt.x/SCREEN_WIDTH*100, ",", 100-pt.y/SCREEN_HEIGHT*100)

def onMouseUp(event):
    global mouseDown
    print("mouse up")
    mouseDown = False

    
def onMouseDown(event):
    global mouseDown
    print("mouse down")
    mouseDown = True
    
# initialise gui
window = Tk()
window.title("Sunrise Servoing Client")
window.geometry('1000x800')
window.config(background = "#aa5aaF")
window.bind("<ButtonPress-1>", onMouseDown)
window.bind("<ButtonRelease-1>", onMouseUp)

SCREEN_WIDTH = window.winfo_screenwidth()
SCREEN_HEIGHT = window.winfo_screenheight()

lbl = Label(window, text="Click and hold to move the LBR", fg = "blue", bg = "yellow", font = "Verdana 10 bold")
lbl.grid(column=0, row=0)

lblPos = Label(window, text="Current command: ", justify=LEFT)
lblPos.grid(column=0, row=1)

btn = Button(window, text="Connect to server", bg="orange", fg="red", command=startServer,justify=LEFT)
btn.grid(column=0, row=2)

window.after(REFRESH_RATE, cyclicTask)
window.mainloop()




