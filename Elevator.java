import java.util.*;

/**
 * Elevator class modeled real world elevator. 
 * It stores its status information and also passenger objects 
 * who are on board. 
 * 
 * @author Min Kim
 *
 */
public class Elevator {
    public final static int WAITING = 0;
    public final static int MOVING = 1;
    public final static int DOOROPENING = 2;
    public final static int DISEMBARKING = 3;
    public final static int BOARDING = 4;
    public final static int DOORCLOSING = 5;
    public final static String [] STATUS = {"WAITING", "MOVING", "DOOROPENING", "DISEMBARKING", "BOARDING", "DOORCLOSING"};
    
    private int currentStatus;
    private int [] timeSpent = new int [6];
    private ArrayList<Passenger> boardedPassenger ;  
    private boolean [] targetFloor;
    private int direction;  
    private int id;
    private int currentFloor;
    private Building building;
    private int nextDestination;
    
    /**
     * Constructs new empty elevator. Default status is WAITING and 
     * default floor is 1. 
     * Note that elevator WAITING is idle. 
     * 
     * @param b building where elevator resides
     * @param newId elevator ID
     * @param newCapacity elevator capacity 
     */
    public Elevator (Building b, int newId) {
        building = b; 
        currentStatus = WAITING;
        id = newId;
        currentFloor = 1;
        direction = 0; 
        nextDestination = 0;
        this.boardedPassenger = new ArrayList<Passenger> ();
        this.targetFloor = new boolean [Settings.numOfFloors() + 1]; 
        for (int i=0; i < Settings.numOfFloors(); i++) {
            targetFloor[i] = false;
        }
    }
    
    /**
     * Get elevator's nextDestination 
     * @return nextDestination 
     */
    public int nextDestination ()   {   return nextDestination;     }
    
    /**
     * Get elevator's current floor
     * @return currentFloor
     */
    public int currentFloor()       {   return currentFloor;        }

    /**
     * Get elevator's current status. 
     * Check the static final variables to match number and status
     * @return current status
     */
    public int currentStatus()      {   return currentStatus;       }
    
    /**
     * Elevator moving direction. 
     * @return moving direction. 1 for up, 0 for stay, -1 for down 
     */
    public int direction()          {   return direction;           }
    
    /**
     * Get elevator's id
     * @return elevator's identification number 
     */
    public int id()                 {   return id;                  }
    
    /**
     * Get passengers boarded in the elevator 
     * @return ArrayList of passengers boarded 
     */
    public ArrayList<Passenger> boardedPassenger() {   return boardedPassenger;}

    /**
     * Get the time statistics this elevator spent
     * @return an array of time spent 
     */
    public int[] timeSpent()        {   return timeSpent;           }

    /**
     * Board a passenger to this elevator. Boarding is possible only 
     * when its current status is BOARDING. On success, passenger is 
     * added to elevator's ArrayList.
     * @param p Passenger who want to board
     * @return true if succeeded
     */
    public boolean boardPassenger(Passenger p) {
        if (currentStatus != Elevator.BOARDING) return false;
        if (boardedPassenger.size() >= Settings.elevatorMaxCapacity()) return false;
        boardedPassenger.add(p); 
        setTarget(p.targetFloor());
        return true;
    }

    /**
     * Elevator wait for call. When there is call, it moves, 
     * open door, release passengers and get new passengers, 
     * and move to where passengers want.  
     */
    public void elevatorAct() {
        timeSpent[currentStatus] ++;
        switch (currentStatus) {
          case WAITING:
              nextDestination = setNextDestination();
              if (nextDestination != currentFloor || boardedPassenger.size() >0) {
                  currentStatus = MOVING;
              } else if (nextDestination == currentFloor 
                      && building.isWaitingAtFloor(currentFloor)) 
                  currentStatus = DOOROPENING;
              break;

          case MOVING:
              if (currentFloor == nextDestination) {
                  currentStatus = DOOROPENING;
                  break;
              }
              if ((currentFloor == Settings.numOfFloors() && direction ==1) 
                      || (currentFloor == 0 && direction ==-1)) { 
                  direction = -direction; 
              }
              currentFloor += direction; 
              break;
              
          case DOOROPENING:
              if (boardedPassenger == null) currentStatus = BOARDING; 
              else currentStatus = DISEMBARKING; 
              break;

          case DISEMBARKING:
              currentStatus = BOARDING;
              break;
              
          case BOARDING:
              currentStatus = DOORCLOSING; 
              break;
              
          case DOORCLOSING:
              targetFloor [currentFloor] = false;
              nextDestination = setNextDestination();
              currentStatus = (nextDestination==currentFloor 
                      && !building.isWaitingAtFloor(currentFloor)) ? WAITING : MOVING;
        }
    }

    /**
     * Add elevator's target floor and reset next stop 
     * in case the new added floor is nearer
     * @param target 
     */
    public void setTarget(int target) {
        targetFloor[target] = true;
        nextDestination = setNextDestination(); 
    }
    
    /**
     * Calculate elevator's nearest destination based on the 
     * distance and elevator's direction. If there is no available
     * destination then it change its status to WAIT. 
     * 
     * @return elevator's new nearest destination. 
     */
    private int setNextDestination() { 
        int newDestination = -1; 
        int minDistance = 100;
        for (int i=0; i<Settings.numOfFloors(); i++) {
            if (targetFloor[i]) {
                int distance = i - currentFloor ;
                if ((distance > 0 && direction == -1) || (distance < 0 && direction == 1)) {
                    distance = Math.abs(distance) + Settings.numOfFloors();
                } else distance = Math.abs(distance);
                
                if (minDistance > distance) {
                    minDistance = distance; 
                    newDestination = i;
                }
            }
        }
        if (newDestination == -1 ) {
            newDestination = currentFloor;
            direction = 0;
            currentStatus = WAITING;
        } else {
            direction = (newDestination > currentFloor) ? 1 : -1; 
        }
        return newDestination;
    }
    
    /**
     * unboard the passenger from elevator, and put passenger into the building 
     * @param p
     */
    public void unboard (Passenger p) {
        boardedPassenger.remove(p);
        building.passengerEnters(p);
    }
}
