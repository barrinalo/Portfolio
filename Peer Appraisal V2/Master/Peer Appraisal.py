#Author: David Chong Tian Wei
#LOG
#Version 1
#Date: 24th January 2014
#End

import EncrypterDecrypter
from Tkinter import *
import tkMessageBox
import os, sys
import math

class Question:
    def __init__(self,qtype,prompt,descriptors):
        self.qtype = qtype
        self.prompt = prompt
        self.descriptors = descriptors
        self.input = []
        self.quota = []
    def AddInput(self, entry):
        self.input.append(entry)
    def AddQuota(self, entry):
        self.quota.append(entry)

class Student:
    def __init__(self, Nric, Name, Syn):
        self.Nric = Nric
        self.Name = Name
        self.Syn = Syn
class OutputClass:
    def __init__(self):
        self.Nrics = []
        self.Qtitles = []
        self.Answers = []
        self.AnswerWidgets = []
        self.RankingTitles = []
        self.Rankings = []
        self.RankingsWidgets = []
    def AddPerson(self, Nric):
        self.Nrics.append(Nric)
        self.Answers.append([])
        self.AnswerWidgets.append([])
    def AddNormalQuestion(self, Prompt):
        self.Qtitles.append(Prompt)
    def AddRankingQuestion(self, Prompt):
        self.RankingTitles.append(Prompt)

class Application:
    def __init__(self):
        self.State = "Login"    #Prepare program state value
        self.Questions = []
        self.Students = []
        self.SyndicateMembers = []
        self.DataContainer = OutputClass()

        self.Root = Tk()
        self.Root.title("Peer Appraisal")#Set the title
        self.Root.geometry("640x480")

        #Prepare login page widgets
        self.LoginText = StringVar()
        self.LoginButton = Button(self.Root, None, text="Login", command=self.ProcessLogin,font=("Helvetica", "16"))
        self.LoginTextbox = Entry(self.Root, font=("Helvetica", "16"),textvariable=self.LoginText)
        self.LoginLabel = Label(self.Root, text="Please enter your NRIC to Login",font=("Helvetica", "32"))
        #End

        #Main Page widgets
        self.MainFrame = Frame(self.Root)
        self.MainCanvas = Canvas(self.MainFrame)
        self.DisplayFrame = Frame(self.MainCanvas)
        self.MainCanvasYScroller = Scrollbar(self.MainFrame, orient="vertical", command=self.MainCanvas.yview)
        self.MainCanvasXScroller = Scrollbar(self.MainFrame, orient="horizontal", command=self.MainCanvas.xview)

        self.MainCanvas.configure(yscrollcommand=self.MainCanvasYScroller.set, xscrollcommand=self.MainCanvasXScroller.set)

        self.InfoFrame = Frame(self.Root)
        self.InfoCanvas = Canvas(self.InfoFrame)
        self.InfoDisplay = Frame(self.InfoCanvas)
        self.InfoCanvasYScroller = Scrollbar(self.InfoFrame, orient="vertical", command=self.InfoCanvas.yview)
        self.InfoCanvasXScroller = Scrollbar(self.InfoFrame, orient="horizontal", command=self.InfoCanvas.xview)

        self.InfoCanvas.configure(yscrollcommand=self.InfoCanvasYScroller.set,xscrollcommand=self.InfoCanvasXScroller.set)
        self.Descriptors = Message(self.InfoDisplay, font=("Helvetica","12"))

        #End

        #Pack Widgets
        self.LoginLabel.pack(side=TOP, expand=YES, fill=X)
        self.LoginTextbox.pack(side=TOP, expand=YES)
        self.LoginButton.pack(side=TOP, expand=YES)

        self.ReadCourseDataFile()
        self.Root.mainloop()

    def MainFrameResize(self, event):
        self.MainCanvas.configure(scrollregion=self.MainCanvas.bbox("all"))
    def InfoFrameResize(self, event):
        self.InfoCanvas.configure(scrollregion=self.InfoCanvas.bbox("all"))
    def ResizeDescriptors(self, event):
        self.Descriptors["width"] = event.width*0.9
    def UpdateDescriptors(self,event):
        if self.State == "Main":
            for a in range(0,len(self.DataContainer.AnswerWidgets)):
                for i in range(0,len(self.DataContainer.AnswerWidgets[a])):
                    if id(event.widget) == id(self.DataContainer.AnswerWidgets[a][i]):
                        Prompttitle = self.DataContainer.Qtitles[i]
                        for q in self.Questions:
                            if q.prompt == Prompttitle:
                                Descriptorstr = q.prompt + "\n\n"
                                Descriptorstr = Descriptorstr + q.descriptors
                                if len(q.quota) == len(q.input) and q.qtype == "selection":
                                    count = []
                                    total = 0
                                    for quota in q.quota:
                                        count.append(math.floor((int(quota)/100.0*len(self.DataContainer.Nrics))+0.5))
                                        total = total + count[len(count)-1]
                                    c = len(count) - 1
                                    if total > len(self.DataContainer.Nrics):
                                        while total > len(self.DataContainer.Nrics):
                                            count[c] = count[c] - 1
                                            total = total - 1
                                            c = c - 1
                                            if c < 0: c = len(count) - 1
                                    elif total < len(self.DataContainer.Nrics):
                                        while total < len(self.DataContainer.Nrics):
                                            count[c] = count[c] + 1
                                            total = total + 1
                                            c = c - 1
                                            if c < 0: c = len(count) - 1

                                    for val in self.DataContainer.Answers:
                                        for c in range(0,len(q.input)):
                                            if q.input[c] == val[i].get():
                                                count[c] = count[c] - 1

                                    for z in range(0,len(q.input)):
                                        Descriptorstr = Descriptorstr + chr(10)
                                        Descriptorstr = Descriptorstr + q.input[z] + " - "
                                        Descriptorstr = Descriptorstr + str(count[z]) + " remaining"
                                self.Descriptors["text"]=Descriptorstr
                                return
            for a in range(0,len(self.DataContainer.RankingsWidgets)):
                if id(self.DataContainer.RankingsWidgets[a]) == id(event.widget):
                    Prompttitle = self.DataContainer.RankingTitles[a]
                    for q in self.Questions:
                        if q.prompt == Prompttitle:
                            self.Descriptors["text"]=q.descriptors
                            return
    def UpdateInputs(self):
        print("placeholder")
    def Submit(self):
        Errormsg = ""
        AllFilled = True
        for i in range(0,len(self.DataContainer.Qtitles)):
            for q in self.Questions:
                if q.prompt == self.DataContainer.Qtitles[i] and q.qtype == "selection":
                    for j in range(0,len(self.DataContainer.Answers)):
                        if self.DataContainer.Answers[j][i].get() == "":
                            AllFilled = False
                            personname = ""
                            for n in self.Students:
                                if self.DataContainer.Nrics[j] == n.Nric:
                                    personname = n.Name
                            Errormsg = Errormsg + q.prompt + " for " + personname + " was not filled in" + chr(10)
        for i in range(0,len(self.DataContainer.RankingTitles)):
            if self.DataContainer.Rankings[i].get() == "":
                AllFilled = False
                Errormsg = Errormsg + self.DataContainer.RankingTitles[i] + " was not filled in" + chr(10)

        QuotasFulfilled = True
        for i in range(0,len(self.DataContainer.Qtitles)):
            for q in self.Questions:
                if q.prompt == self.DataContainer.Qtitles[i] and q.qtype == "selection" and len(q.quota) == len(q.input):
                    count = []
                    total = 0
                    for quota in q.quota:
                        count.append(math.floor((int(quota)/100.0*len(self.DataContainer.Nrics))+0.5))
                        total = total + count[len(count)-1]
                    c = len(count) - 1
                    if total > len(self.DataContainer.Nrics):
                        while total > len(self.DataContainer.Nrics):
                            count[c] = count[c] - 1
                            total = total - 1
                            c = c - 1
                            if c < 0: c = len(count) - 1
                    elif total < len(self.DataContainer.Nrics):
                        while total < len(self.DataContainer.Nrics):
                            count[c] = count[c] + 1
                            total = total + 1
                            c = c - 1
                            if c < 0: c = len(count) - 1

                    for val in self.DataContainer.Answers:
                        for c in range(0,len(q.input)):
                            if q.input[c] == val[i].get():
                                count[c] = count[c] - 1
                    for c in count:
                        if c != 0:
                            QuotasFulfilled = False
                            Errormsg = Errormsg + "The quota for " + q.prompt + " is unfulfilled" + chr(10)
                            break

        if AllFilled and QuotasFulfilled:
            msg = ""
            for i in range(0,len(self.DataContainer.Qtitles)):
                for j in range(0,len(self.DataContainer.Nrics)):
                    msg = msg + self.DataContainer.Qtitles[i] + "|" + self.DataContainer.Nrics[j] + "|" + self.DataContainer.Answers[j][i].get() + chr(10)
            for i in range(0,len(self.DataContainer.RankingTitles)):
                msg = msg + self.DataContainer.RankingTitles[i] + "|" + self.DataContainer.Rankings[i].get() + chr(10)
            f = open(os.path.dirname(os.path.realpath(sys.argv[0])) + "/" + self.LoginText.get() + ".txt","w")
            f.write(EncrypterDecrypter.EncryptSubmission((self.LoginText.get() + ".txt"),msg))
            f.close()
            tkMessageBox.showinfo("Peer Appraisal Completed", "Thank you for completing the peer appraisal.  Please upload the text file named after your NRIC to Learnet or email it to the clerk")
            self.Root.destroy()
        else:
            tkMessageBox.showerror("Invalid Data", Errormsg)

    def ProcessLogin(self):
        if len(self.LoginText.get()) == 9 and (self.LoginText.get()[0] == "S" or self.LoginText.get()[0] == "s"):#Check for valid NRIC
            loginic = self.LoginText.get()
            if loginic[0] == "s":
                loginic = "S" + loginic[1:]
            if ord(loginic[8]) >= 97 and ord(loginic[8]) <= 122:
                loginic = loginic[0:8] + chr((ord(loginic[8]) ^ 32))
            self.LoginText.set(loginic)
            #Get user's syndicate
            Loginsyn = ""
            for s in self.Students:
                if s.Nric == loginic:
                    Loginsyn = s.Syn
                    break
            if Loginsyn == "":
                tkMessageBox.showerror("NRIC not on list","Your NRIC is not on the list, please approach the TSWC Clerk for help");
                return;
            #Create Student Labels and populate datacontainer with student data
            counter = 1
            for s in self.Students:
                if str(s.Syn) == str(Loginsyn) and s.Nric != loginic:
                    Label(self.DisplayFrame,text=s.Name,font=("Helvetica","12")).grid(row=counter,column=0,padx=10,pady=5)
                    self.DataContainer.AddPerson(s.Nric) #Add Nric to DataContainer
                    counter = counter + 1

            #Generate Input fields and populate datacontainer with question data
            counter = 1
            counter2 = len(self.DataContainer.Nrics) + 2
            for q in self.Questions:
                if q.qtype == "selection":
                    Label(self.DisplayFrame,text=q.prompt,font=("Helvetica","12")).grid(row=0,column=counter,padx=10,pady=5)
                    self.DataContainer.AddNormalQuestion(q.prompt)
                    for i in range(0,len(self.DataContainer.Nrics)):
                        SomeTextVar = StringVar()
                        self.DataContainer.Answers[i].append(SomeTextVar)
                        SomeOptionMenu = OptionMenu(self.DisplayFrame, SomeTextVar,*q.input)
                        SomeOptionMenu.grid(row=1+i,column=counter)
                        SomeOptionMenu.bind("<Enter>", self.UpdateDescriptors)
                        SomeOptionMenu.bind("<Leave>", self.UpdateDescriptors)
                        self.DataContainer.AnswerWidgets[i].append(SomeOptionMenu)

                    counter = counter + 1
                elif q.qtype == "open":
                    Label(self.DisplayFrame,text=q.prompt,font=("Helvetica","12")).grid(row=0,column=counter,padx=10,pady=5)
                    self.DataContainer.AddNormalQuestion(q.prompt)
                    for i in range(0,len(self.DataContainer.Nrics)):
                        SomeTextVar = StringVar()
                        self.DataContainer.Answers[i].append(SomeTextVar)
                        SomeTextField = Entry(self.DisplayFrame, textvariable=SomeTextVar, font=("Helvetica","10"))
                        SomeTextField.bind("<FocusIn>", self.UpdateDescriptors)
                        SomeTextField.grid(row=1+i,column=counter)
                        self.DataContainer.AnswerWidgets[i].append(SomeTextField)
                    counter = counter + 1
                elif q.qtype == "ranking":
                    Label(self.DisplayFrame,text=q.prompt,font=("Helvetica","12")).grid(row=counter2,column=0,padx=10,pady=5)

                    SomeTextVar = StringVar()
                    self.DataContainer.Rankings.append(SomeTextVar)
                    SomeList = []
                    for i in range(0,len(self.Students)):
                        SomeList.append(self.Students[i].Name)
                    SomeOptionMenu = OptionMenu(self.DisplayFrame, SomeTextVar, *SomeList)
                    SomeOptionMenu.grid(row=counter2,column=1)
                    SomeOptionMenu.bind("<Enter>", self.UpdateDescriptors)
                    SomeOptionMenu.bind("<Leave>", self.UpdateDescriptors)
                    self.DataContainer.RankingsWidgets.append(SomeOptionMenu)
                    counter2 = counter2 + 1
                    self.DataContainer.AddRankingQuestion(q.prompt)

            Button(self.DisplayFrame, None,text="Submit",font=("Helvetica","16"),command=self.Submit).grid(row=counter2,column=0)

            self.Root.bind_class("Entry", "<Button>", self.UpdateDescriptors)

            self.LoginLabel.pack_forget()
            self.LoginTextbox.pack_forget()
            self.LoginButton.pack_forget()

            self.InfoFrame.place(relheight=1.0,relwidth=0.25,relx=0.75)
            self.MainFrame.place(relheight=1.0,relwidth=0.75)

            self.MainCanvasYScroller.pack(fill=Y,side=RIGHT)
            self.MainCanvasXScroller.pack(fill=X,side=BOTTOM)
            self.MainCanvas.pack(fill=BOTH,side=TOP,expand=1)
            self.MainCanvas.create_window((0,0),window=self.DisplayFrame,anchor="nw",tags="self.DisplayFrame")


            self.InfoCanvasYScroller.pack(fill=Y,side=RIGHT)
            self.InfoCanvasXScroller.pack(fill=X,side=BOTTOM)
            self.InfoCanvas.pack(fill=BOTH,side=TOP,expand=1)
            self.InfoCanvas.create_window((0,0),window=self.InfoDisplay,anchor="nw",tags="self.InfoDisplay")
            self.Descriptors.pack()

            self.DisplayFrame.bind("<Configure>", self.MainFrameResize)
            self.InfoDisplay.bind("<Configure>", self.InfoFrameResize)
            self.InfoFrame.bind("<Configure>",self.ResizeDescriptors)

            self.State = "Main"

        else:
            tkMessageBox.showerror("Invalid NRIC", "Your nric is invalid.")
    def ReadCourseDataFile(self):
        self.CourseData = EncrypterDecrypter.Decrypt(os.path.dirname(os.path.realpath(sys.argv[0])) + "/CourseDataFile.txt")
        Beginning = 0
        Difference = 0
        msg = ""
        while  (Beginning + Difference) < len(self.CourseData):
            if self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] == "|":
                msg = self.CourseData[Beginning:(Beginning + Difference)]
                Beginning = Beginning + Difference + 1
                Difference = 0
                if len(msg) == 9 and msg[0] == "S":
                    nric = msg
                    while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                        Difference = Difference + 1
                    name = self.CourseData[Beginning:(Beginning + Difference)]
                    Beginning = Beginning + Difference + 1
                    Difference = 0
                    while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != chr(10):
                        Difference = Difference + 1
                    syn = self.CourseData[Beginning:(Beginning + Difference)]
                    Beginning = Beginning + Difference + 1
                    Difference = 0
                    self.Students.append(Student(nric,name,syn))
                if msg == "Student Info":
                    Beginning = Beginning + 1
                if msg == "Questions":
                    Beginning = Beginning + 1
                if msg == "Question Type":
                    while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                        Difference = Difference + 1
                    msg = self.CourseData[Beginning:(Beginning + Difference)]
                    Beginning = Beginning + Difference + 2
                    Difference = 0
                    if msg == "selection":
                        qType = msg
                        while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                            Difference = Difference + 1
                        Beginning = Beginning + Difference + 1
                        Difference = 0
                        while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                            Difference = Difference + 1
                        qPrompt = self.CourseData[Beginning:(Beginning + Difference)]
                        Beginning = Beginning + Difference + 2
                        Difference = 0
                        while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                            Difference = Difference + 1
                        Beginning = Beginning + Difference + 1
                        Difference = 0
                        while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                            Difference = Difference + 1
                        qDescriptors = self.CourseData[Beginning:(Beginning + Difference)]
                        Beginning = Beginning + Difference + 2
                        Difference = 0

                        self.Questions.append(Question(qType,qPrompt,qDescriptors))

                        while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                            Difference = Difference + 1
                        Beginning = Beginning + Difference + 1
                        Difference = 0
                        while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                            Difference = Difference + 1
                        InputParam = self.CourseData[Beginning:(Beginning + Difference)]
                        Beginning = Beginning + Difference + 2
                        Difference = 0
                        InputParam = InputParam.split(",")
                        for x in InputParam:
                            self.Questions[len(self.Questions)-1].AddInput(x)
                        while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                            Difference = Difference + 1
                        Beginning = Beginning + Difference + 1
                        Difference = 0
                        while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                            Difference = Difference + 1
                        qQuota = self.CourseData[Beginning:(Beginning + Difference)]
                        Beginning = Beginning + Difference + 2
                        Difference = 0
                        qQuota = qQuota.split(",")
                        if len(qQuota) == len(InputParam):
                            for x in qQuota:
                                self.Questions[len(self.Questions)-1].AddQuota(x)
                    else:
                        qType = msg
                        while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                            Difference = Difference + 1
                        Beginning = Beginning + Difference + 1
                        Difference = 0
                        while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                            Difference = Difference + 1
                        qPrompt = self.CourseData[Beginning:(Beginning + Difference)]
                        Beginning = Beginning + Difference + 2
                        Difference = 0
                        while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                            Difference = Difference + 1
                        Beginning = Beginning + Difference + 1
                        Difference = 0
                        while self.CourseData[(Beginning + Difference): (Beginning + Difference + 1)] != "|":
                            Difference = Difference + 1
                        qDescriptors = self.CourseData[Beginning:(Beginning + Difference)]
                        Beginning = Beginning + Difference + 2
                        Difference = 0

                        self.Questions.append(Question(qType,qPrompt,qDescriptors))

            else:
                Difference = Difference + 1

if __name__ == '__main__':
    Application()
