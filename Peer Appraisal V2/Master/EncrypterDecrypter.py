#Script to encrypt and decrypt files
#Author: David Chong Tian Wei
#LOG
#Version 1
#Date: 19th January 2014
"""
-Encrypt and Decrypt functions added
    *Unique key for each file, based on NRIC
    *Default key for Course Data File
-Drag and drop for individual execution added
-Code seems to be reusable if this module is imported to the main peer appraisal program
"""
#End

import sys, os, random

def EncryptSubmission(FileName, msg):
    RandSeed = FileName[len(FileName)-13:]#Use NRIC as seed for unique key
    RandSeed = RandSeed[:8]
    RandSeed = RandSeed[1:]
    RandSeed = int(RandSeed)
    random.seed(RandSeed)

    Message = msg    #Read in raw data
    OutputMessage = ""
    for a in Message:
        key = random.randint(0,255)#Generate random byte as a key
        a = ord(a) ^ key
        if a < 10:
            OutputMessage = OutputMessage + "00" + str(a)#patch extra places to allow for decryption to work
        elif a < 100:
            OutputMessage = OutputMessage + "0" + str(a)#patch extra places to allow for decryption to work
        else:
            OutputMessage = OutputMessage + str(a)
    return OutputMessage

def Encrypt(FileName):
    if FileName.find("CourseDataFile") != -1: #Check if file supplied is CourseDataFile
        random.seed(478)#Use default seed
    else:
        RandSeed = FileName[len(FileName)-13:]#Use NRIC as seed for unique key
        RandSeed = RandSeed[:8]
        RandSeed = RandSeed[1:]
        RandSeed = int(RandSeed)
        random.seed(RandSeed)

    FHandle = open(FileName,'r')
    Message = FHandle.read()    #Read in raw data
    FHandle.close()
    OutputMessage = ""
    for a in Message:
        key = random.randint(0,255)#Generate random byte as a key
        a = ord(a) ^ key
        if a < 10:
            OutputMessage = OutputMessage + "00" + str(a)#patch extra places to allow for decryption to work
        elif a < 100:
            OutputMessage = OutputMessage + "0" + str(a)#patch extra places to allow for decryption to work
        else:
            OutputMessage = OutputMessage + str(a)
    return OutputMessage

def Decrypt(FileName):
    if FileName.find("CourseDataFile") != -1: #Check if file supplied is CourseDataFile
        random.seed(478)#Use default seed
    else:
        RandSeed = FileName[len(FileName)-13:]#Use NRIC as seed for unique key
        RandSeed = RandSeed[:8]
        RandSeed = RandSeed[1:]
        RandSeed = int(RandSeed)
        random.seed(RandSeed)

    FHandle = open(FileName,'r')
    Message = FHandle.read()   #Read in raw data
    FHandle.close()
    OutputMessage = ""
    for i in range(0,len(Message),3):
        monomer = Message[i:i + 3]#Tokenise the message into 3digit codons
        key = random.randint(0,255)
        if monomer[0] == '0' and monomer[1] == '0':#Parse out the irrelevant data
            OutputMessage = OutputMessage + chr(int(monomer[2])^key)
        elif monomer[0] == '0':#Parse out the irrelevant data
            OutputMessage = OutputMessage + chr(int(monomer[1:])^key)
        else:
            OutputMessage = OutputMessage + chr(int(monomer[:])^key)
    return OutputMessage
    
if __name__ == '__main__':
    if len(sys.argv) > 1:#Check for command line variables
        arglist = sys.argv
        arglist = arglist[1:]   #Remove self from argument list
        Decision = ""
        Decision = input("Press 'e' then 'enter' to encrypt or 'd' then 'enter' to decrypt")
        if Decision[:1] == 'e': #Remove \n (There should be a more elegant work around...)
            for files in arglist:
                m = Encrypt(files)
                f = open(files,'w')
                f.write(m)
                f.close()
        elif Decision[:1] == 'd': #Remove \n (There should be a more elegant work around...)
            for files in arglist:
                m = Decrypt(files)
                f = open(files,'w')
                f.write(m)
                f.close()
        else:
            print("WTF")
    else:
        input("""To use, please execute this program with the files you wish to encrypt or decrypt as command line arguments.
You can do this by dragging the files onto this program.""")
        
