__author__ = 'Tulasi'

import socket

UDP_IP = "232.36.124.65"
UDP_PORT = 3665

sock = socket.socket(socket.AF_INET, # Internet
                 socket.SOCK_DGRAM) # UDP
#sock.bind((UDP_IP, UDP_PORT))
sock.bind(("", UDP_PORT))

while True:
    data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
    print "received message:", data

