#************************************
# PROJECT: SunriseServoingTest
# BY:      Tom Evison
# DATE:    10/12/2019
#                                   
# tested running Python 3.7.3
#************************************

import socket
from ctypes import windll, Structure, c_long, byref

class point(Structure):
    _fields_ = [("x", c_long), ("y", c_long)]

def server_program():
    # get the hostname
    host = socket.gethostname()
    port = 8080  # initiate port no above 1024

    server_socket = socket.socket()  # get instance
    server_socket.bind((host, port))  # bind host address and port together
    while True:
        print("Waiting for connection on: {}:{} ".format(host,port))
        server_socket.listen(2)
        
        conn, address = server_socket.accept()  # accept new connection
        print("Connection from: " + str(address))
        while True:
            # receive data stream. it won't accept data packet greater than 1024 bytes
            data = conn.recv(1024).decode()
            if not data:
                break
            
            print("from connected user: " + str(data))

            # get mouse pointer and send to client
            pt = point()
            windll.user32.GetCursorPos(byref(pt))
            data = "{},{}".format(pt.x,pt.y)
            conn.send(data.encode())  # send data to the client

        conn.close()  # close the connection
        print("Connection closed.. ")

if __name__ == '__main__':
    server_program()
