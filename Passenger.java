
/**
 * This class imitates real world elevator passenger behavior. 
 * They sometimes decide to ride elevator, then 
 * call, wait, board, set destination, travel and unboard in order.
 * @author Min Kim
 *
 */
public class Passenger {

    private int currentStatus; 
    private int[] timeSpent = new int[6];
    private int pId;
    private int targetFloor; 
    private int currentFloor; 
    private int boardingElevatorNum;
	
	public static final int STAYING = 0;
	public static final int WAITING = 1;
	public static final int BOARDING = 2;
	public static final int SETDEST = 3;
	public static final int TRAVELLING= 4;
	public static final int UNBOARDING= 5;
	public static final String [] STATUS = {"STAYING", "WAITING", "BOARDING", "SETDEST", "TRAVELLING", "UNBOARDING"};
	
	/**
	 * Passenger behavior when they are at the building.
	 * They decide to move, call and wait, and try to board.
	 * @param b building where they stay
	 */
	public void actAtBuilding (Building b) {
		switch (currentStatus) {
		  case UNBOARDING:
		      if (targetFloor == -1) currentStatus = STAYING; 
		      break;

		  case STAYING:
		      increaseTime();
			  if (Math.random() < Settings.frequencyOfRidingElevator()) {
					currentStatus = WAITING;
					do { 
					    targetFloor = (int) (Math.random() * Settings.numOfFloors());
					} while (targetFloor==currentFloor);  
					b.pushButton(currentFloor, targetFloor > currentFloor ? 1:-1); 
			  }
			  break;
			  
		  case WAITING:  
              increaseTime();
			  boardingElevatorNum = b.whichElevatorAvailable(currentFloor, targetFloor);  
              if (boardingElevatorNum == -1) break;
              else currentStatus = BOARDING;
              break;
              
		  case BOARDING: 
			  boolean boardSuccess = b.boardToElevator(this, boardingElevatorNum );
              increaseTime();
			  if (boardSuccess) {
				  currentStatus=SETDEST;
				  currentFloor=-1;
			  }
			  break;
		}
	}

	/**
     * Passenger behavior when they are at the elevator
     * @param e Elevator where they boarded
     */

	public void actAtElevator (Elevator e) {
		increaseTime();
		switch (currentStatus) {
		  case SETDEST:
			  e.setTarget(targetFloor);
			  currentStatus = TRAVELLING;
			  break;
			  
		  case TRAVELLING:
		      if (e.currentFloor() == targetFloor && e.currentStatus() == Elevator.DOOROPENING) 
		          currentStatus = UNBOARDING;
		      break;
		      
		  case UNBOARDING:
		      if (e.currentFloor() == targetFloor && (e.currentStatus() == Elevator.DISEMBARKING || 
		               e.currentStatus() == Elevator.BOARDING )) {
    			  e.unboard(this); 
    			  currentFloor = targetFloor;
    			  targetFloor = -1; 
              }
			  break; 
		}
	}
	
	/**
	 * constructs a passenger object
	 * @param id 
	 */
	public Passenger (int id) {
		pId=id; 
		currentFloor = id % Settings.numOfFloors(); 
		currentStatus = STAYING;
	}
	
	/**
	 * a passenger enter into building
	 * @param b building
	 */
	public void enterIntoBuilding (Building b) {
		currentStatus = STAYING;
		b.passengerEnters(this);
	}
	
	/**
	 * tracking how passenger spend their time
	 */
	public void increaseTime () {
		timeSpent[currentStatus] ++;
		Simulation.counter++;
	}
	
	// Getter methods
	public int currentStatus() {   return currentStatus; 	}
	public int pid()           {   return pId;				}
	public int currentFloor()  {   return currentFloor; 	}
    public int targetFloor()   {   return targetFloor;      }
    public int[] timeSpent()   {   return timeSpent;        }
}
