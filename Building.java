import java.util.ArrayList;
import java.util.*;

/**
 * Building class is a model of real world building, 
 * which has elevators and where people reside in. 
 * The object stores several elevator objects and passenger objects. 
 * When people push elevator call button, the building receive the call  
 * and assign adequate elevators to receive the call. 
 * 
 * Note that building does not have state information of how many passenger 
 * stays on each floor unless it surveys to all passengers. 
 * It is same as real world building.  
 *  
 * @author Min Kim
 *
 */
public class Building {
    
    ArrayList<Passenger> stayingPassengers = new ArrayList<Passenger> ();
    ArrayList<Elevator> elevator = new ArrayList<Elevator> (); 
    
	private int numOfFloor;
	private int numOfElevator;
	
	/**
	 * constructs a building 
	 * @param aNumOfFloor number of floors the building have 
	 * @param aNumOfElevator number of elvators the building have
	 * @param capacity elevators max capacity
	 */
	public Building (int aNumOfFloor, int aNumOfElevator) {
        numOfFloor = aNumOfFloor;
        numOfElevator = aNumOfElevator;
		for (int i=0; i<aNumOfElevator; i++) {
			this.elevator.add(new Elevator (this, i));
		}
	}

	/**
	 * Getter for all passengers in the building.
	 * @return ArrayList of all passengers 
	 */
	public ArrayList<Passenger> getPassengers () {  
	    return stayingPassengers; 
    } 
	
	/**
	 * Add a passenger to the building
	 * @param p passenger
	 */
	public void passengerEnters (Passenger p) {	  
	    stayingPassengers.add(p); 
	}
	
    public void pushButton(int pushedFloor, int direction) {
        int nearestElevator = selectElevator(pushedFloor, direction, Settings.elevatorChoosingAlgorithm()); 
        if (nearestElevator != -1) elevator.get(nearestElevator).setTarget(pushedFloor);
    }
    
    /**
     * This private method returns available elevator when there is elevator call. 
     * 
     * @param pushedFloor
     * @param direction
     * @param 0 for any waiting elevator, 1 for distance calculation, 2 for distance 
     *        calculation but avoiding idle elevator
     * @return
     */
    private int selectElevator (int pushedFloor, int direction, int algorithm) {
        int minGap = numOfFloor*2+2;
        int chosenElevator = -1; 
        switch (algorithm) {
        
        // any 'WAITING' elevator in sequence. If no idle elevator, return 0  
        case 0: 
            for (int i=0; i< numOfElevator; i++) {
                if (elevator.get(i).currentStatus() == Elevator.WAITING ) return i;
            }
            return 0;
                
         // Calculate distance, considering elevator moving direction 
        case 1:  
            for (int i=0; i< numOfElevator; i++) {
                Elevator e = elevator.get(i);
                int distance = 0;             
                int gap = e.currentFloor() - pushedFloor; 
                
                if (gap > 0 && e.direction() < 0) {
                    gap = Math.abs(gap) + numOfFloor;
                } else gap = Math.abs(gap);
                
                if (gap < minGap) {
                    minGap = gap; 
                    chosenElevator = i;
                }
            }
            return chosenElevator; 

        // avoid using idle elevator because staring and stopping costs a lot
        case 2: 
            for (int i=0; i< numOfElevator; i++) {
                Elevator e = elevator.get(i);
                int distance = 0; 
                int gap = e.currentFloor() - pushedFloor; 

                if (gap > 0 && e.direction() < 0) {
                    gap = Math.abs(gap) + numOfFloor;
                } else gap = Math.abs(gap);
                
                // Waiting elevator has very low precedence
                if (e.currentStatus() == Elevator.WAITING) gap = gap * 10;  

                if (gap < minGap) {
                    minGap = gap; 
                    chosenElevator = i;
                }
            }
            return chosenElevator; 
        }
        return 0;
    }
  
    /**
     * Passenger try to board elevator. 
     * @param p
     * @param elevatorNumber
     * @return true if succeed to board
     */
	public boolean boardToElevator(Passenger p, int elevatorNumber) {
		boolean test = elevator.get(elevatorNumber).boardPassenger (p);
		if (test) {stayingPassengers.remove(p);
		}
		return test; 
	}

	/**
	 * ask if any elevator stopped for the requester
	 * @param boardingFloor
	 * @param targetFloor
	 * @return
	 */
	public int whichElevatorAvailable (int boardingFloor, int targetFloor) {
		for (int i=0; i<numOfElevator; i++) {
			if (elevator.get(i).currentFloor() == boardingFloor
			        && (targetFloor - boardingFloor) * elevator.get(i).direction() >= 0
			        )  return i;
		}
		return -1; 
	}

	/**
	 * calculate and return how many passengers are waitng or staying at each floor
	 * @return 
	 */
	public int[][] numOfPassengersPerFloor() {
	    int [][] numberOfPassengers = new int [Settings.numOfFloors()][2];
	    Iterator<Passenger> iter = stayingPassengers.iterator();  
	    while (iter.hasNext()) {
	        Passenger p = iter.next(); 
	        if (p.currentStatus() == Passenger.STAYING) numberOfPassengers [p.currentFloor()][0]++;  
	        else if (p.currentStatus() == Passenger.WAITING 
                    || p.currentStatus() == Passenger.BOARDING) numberOfPassengers [p.currentFloor()][1]++;  
	    }
	    return numberOfPassengers; 
	}
	
	/**
	 * check if there are any passenger waiting at given floor
	 * @param floor
	 * @return
	 */
   public boolean isWaitingAtFloor (int floor) {    
        Iterator<Passenger> iter = stayingPassengers.iterator();  
        while (iter.hasNext()) {
            Passenger p = iter.next(); 
            if (p.currentFloor() == floor && 
                    (p.currentStatus() == Passenger.WAITING 
                    || p.currentStatus() == Passenger.BOARDING)) return true; 
        }
        return false; 
    }
}
