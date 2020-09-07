# ElevatorSimulator

Harvard Extension School '2016년 상반기 

Java Class Term Project 

설치 방법, Compile 방법 등은 TermProject-mKim.pdf 를 읽어볼 것 

# Background
* Many people are frustrated with the dumb elevator, which moves by incomprehensible logic.
* When you call the elevator from the ground level and there is available one at 3th floor, but the other one at 20th floor starts moving to you. 
* This looks very stupid and I discussed why they worked so stupid like this.  
* Someone says that elevator manufacturer cares only for building owners, who pays to them and wants to reduce electricity consumption. The manufacturers does not care resident's conviniences. So elevator logic is smart enough now, in the aspect of energy consumption. 
* Other says that elevator logic is very difficult than it looks, because elevator can’t anticipate user’s actions, and user always change their action. This opinion means that elevator is dumb and it can not be enhanced. 
* I was always curious if I was smart enough to enhance the elevator. This simulation program will conduct various test of elevator movement. The results is for minimizing passengers waiting time, and also elevator’s electricity consumption minimize


# Objective
* Assuming you are a building owner, or building constructor, who want to make very effective inbuilding transportation system. Your challenge is to satisfy residents by
minimizing their waiting time while keep the elevator’s energy consumption low enough.
* This program will help you find out optimal elevator number, capacity, and logic. You can input manipulate variables such as number of elevators and floors, number of passengers staying,frequency of boarding elevator, and 6 more options.
* And you will get simulation report describing how successful your elevator system was, in the aspect of customer satisfaction and energy effecienty. 

# How to run
+ 5 Files :
  + Building.java
  + Elevator.java
  + Passenger.java
  + Settings.java
  + Simulation.java
+ Locate all java file into same directory 
+ Compile :
  + javac *.java
+ Run :
  + java –cp . Simulation
+ This program generates ‘lastresult.out’ in the directory. Be sure to delete this file if your
program stop unexpectedly. Or the program may not produce report. 
