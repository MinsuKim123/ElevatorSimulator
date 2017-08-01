import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*; 
import java.io.*;

/**
 * Simulation class control simulation flows coordinating 
 * Settings, Elevators, and Building class. 
 * It also draws graphics in the class. 
 * 
 * overall contol flow in main method
 * 1. initialize    [Call Settings class]
 * 2. do simulation [Coordinating Passengers, Elevators, and Building]   
 * 3. print report  
 * 
 * @author Min Kim
 */
public class Simulation {
    
    public static int counter =0;
    private static Building building;
    private static int drawSpeed = 200; 
    private static String [] lastResult;
    private static boolean doNotSkip = true;

    private static int xSize; 
    private static int ySize;     
    private static JFrame simFrame;
    private static JPanel graphicPanel;
    private static JPanel controlPanel;
    private static JPanel resultPanel;
    private static JLabel [] graphicLabel;
    
    /**
     *  helper method to initialize variables and 
     *  graphical components
     */
    private static void initialize () {
        Settings.initSettings(); // set = new Settings();
        building = new Building (Settings.numOfFloors(), Settings.numOfElevators()); 
        
        // people enter into the building 
        for (int i=0; i<Settings.numOfPassengers(); i++) {
            Passenger p = new Passenger (i); 
            p.enterIntoBuilding(building);
        }       
        
        // Set GUI components
        simFrame = new JFrame(); 
        simFrame.setLayout(new BorderLayout());
        simFrame.setSize(1000 * Settings.resolution(), 800*Settings.resolution());
        
        xSize = Settings.numOfElevators()+2; 
        ySize = Settings.numOfFloors()+1; 

        graphicPanel = new JPanel();
        graphicPanel.setLayout(new GridLayout (ySize, xSize ));
        graphicPanel.setBorder(BorderFactory.createBevelBorder(1));
        graphicPanel.setBorder(BorderFactory.createBevelBorder(1));
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout ()); 
        int panelSize = (xSize) *  (ySize );

        graphicLabel = new JLabel [ panelSize ];
        Font f = new Font ("Times", Font.BOLD, 15 * Settings.resolution());    
        int labelWidth = 1000 / (xSize +1) * Settings.resolution(); 
        int labelDepth = 800 / (ySize +1) * Settings.resolution(); 

        for (int i=0; i < panelSize; i++) {
            graphicLabel [i] = new JLabel ("");
            graphicLabel [i].setOpaque(true); 
            graphicLabel [i].setFont(f);
            graphicLabel [i].setPreferredSize(new Dimension(labelWidth,labelDepth));
            graphicLabel [i].setForeground(Color.BLACK);
            graphicPanel.add(graphicLabel[i]);
        }

        clearGUI();

        // draw control panel 
        String[] buttons = {"FASTER", "SLOWER", "SKIP"}; // , "STOP"};
        JButton [] controlButton = new JButton [buttons.length]; 
        for (int i=0; i<buttons.length; i++) {
            controlButton [i] = new JButton (buttons[i]);
            controlButton [i].setActionCommand(buttons[i]);
            controlButton [i].setFont(new Font("Arial", Font.BOLD, 20 * Settings.resolution()));
            controlButton [i].setPreferredSize(new Dimension (150 * Settings.resolution(), 50 * Settings.resolution() ));
            controlButton [i].addActionListener(new ButtonClickListener());
            controlPanel.add(controlButton [i]);
        }
        
        simFrame.add(controlPanel, BorderLayout.NORTH);
        simFrame.add(graphicPanel, BorderLayout.CENTER);
        simFrame.setVisible(true);
    }

    /**
     * This helper method redraw the graphic user interface.
     * It is called while initalizing and each drawing.
     */
    private static void clearGUI() {
        int panelSize = (xSize) *  (Settings.numOfFloors()+1);
        for (int i=0; i < panelSize; i++) {
            if (i==0) {
            } else if (i % (xSize) == 0 ) {
                graphicLabel[i].setBackground(Color.RED);
                graphicLabel[i].setText("Floor: " + (Settings.numOfFloors() - i / (xSize) ));
                graphicLabel[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            } else if (i > 0 && i < Settings.numOfElevators()+1) {
                graphicLabel[i].setText("Elv. #" + i);
                graphicLabel[i].setBackground(Color.YELLOW);
            } else {
                graphicLabel[i].setText("");
            }
        }
    }
        
    /**
     * This helper method draw simulation progress status 
     */
    private static void simulationDraw() {
        clearGUI();     

        // Draw each Elevator
        for (int i=0; i<Settings.numOfElevators(); i++) {
            Elevator e = building.elevator.get(i);
            int index = (Settings.numOfFloors() - e.currentFloor()) * xSize + (i+1); 
            StringBuilder sb = new StringBuilder();
            sb.append("<html> Stat:"+Elevator.STATUS[e.currentStatus()]); 
            sb.append("<br>Boarded:"+e.boardedPassenger().size());
            sb.append("<br>Dest:"+e.nextDestination());
            sb.append("</html>");
            graphicLabel[index].setText(sb.toString());
        }

        // Draw Floor status
        int [][] passengersPerFloor = building.numOfPassengersPerFloor(); 
        for (int i=0; i<Settings.numOfFloors(); i++) {
            int index = (Settings.numOfFloors() - i) * xSize + Settings.numOfElevators()+1; 
            StringBuilder sb = new StringBuilder();
            sb.append("<html>Stay:"+ passengersPerFloor[i][0]);
            sb.append("<br>Wait:"+ passengersPerFloor[i][1] + "</html>");
            graphicLabel[index].setText( sb.toString());
        }
        
        try { Thread.sleep(drawSpeed); 
        } catch (Exception e){}
    }

    /**
     * main method controls overall flow 
     * 1. initialize 
     * 2. do simulation (elevator -> elvator passenger -> floor passenger)  
     * 3. print result
     * 
     * simulation part is a loop that each count means 1 second.
     * Each second, all elevator and passenger objects are requested to 
     * change its status. 
     * 
     * Passenger is staying, waiting, boarding, and so on. 
     * Elevator is waiting, moving, disembarking, and so on. 
     * These state is changing one by one, unless special occasion.  
     * 
     * Everything is initiated from a passenger's random decision to move 
     * to another floor. Passenger asks building for a elevator, then 
     * one or many elevator will arrive. 
     * 
     * @param args
     * @throws FileNotFoundException
     */
    public static void main (String [] args) throws FileNotFoundException {
        
        initialize();

        //main loop start
        for (int timer=0; timer < Settings.LoopCounter(); timer ++) {
            graphicLabel[0].setText(timer/60 + "Min" + timer%60 + "Sec");
            counter = 0;

            if (doNotSkip()) simulationDraw(); 
            
            // move elevator
            for (int j=0; j < Settings.numOfElevators(); j++) {
                Elevator e = building.elevator.get(j);
                e.elevatorAct();
            }

            // each passenger at each elevator decide behavior
            for (Elevator e:building.elevator) {
                if (e.boardedPassenger() != null ) {
                    int numOfBoardedPassenger = e.boardedPassenger() !=null ? e.boardedPassenger().size():0;
                    int i=0;
                    while (e.boardedPassenger() !=null && i < numOfBoardedPassenger) {
                        Passenger p = e.boardedPassenger().get(i);
                        p.actAtElevator(e);
                        i++; 

                        // required to maintain loop counter ArrayList.size
                        if (numOfBoardedPassenger < e.boardedPassenger().size()) {
                            numOfBoardedPassenger++;
                        } else if (numOfBoardedPassenger > e.boardedPassenger().size()) {
                            i--;
                            numOfBoardedPassenger--;
                        }
                    }
                }
            }
            
            //each passenger at building decide behavior
            int numOfBuildingPassenger = 
                    building.getPassengers() !=null ? building.getPassengers().size():0;
            int i=0;
            while (building.getPassengers() != null && i < numOfBuildingPassenger) {
                    Passenger p = building.getPassengers().get(i);
                    p.actAtBuilding(building);
                    i++; 
                    // to set loop counter stable, avoiding unintended change by ArrayList.size() change
                    if (numOfBuildingPassenger < building.getPassengers().size()) 
                        numOfBuildingPassenger++;
                    else if (numOfBuildingPassenger > building.getPassengers().size()) {
                        i--;numOfBuildingPassenger--;
                    }
            }
            
            // parity check and halt 
            if (counter != Settings.numOfPassengers()) { 
                System.out.println ("Passenger acting control is wrong : " + counter); 
                System.exit(-1); 
            }
        }
        printResult(); 
    }

    /**
     * This private helper method calculate result, 
     * and print it to console first(detail),
     * and GUI (summary and comparison with previous result)
     * 
     * 
     */
    private static void printResult() {
        ArrayList<Passenger> allPassenger = new ArrayList<Passenger> ();
        allPassenger.addAll(building.getPassengers()); 
        for (int i=0; i<Settings.numOfElevators(); i++) {
            if (building.elevator.get(i).boardedPassenger() != null)
            allPassenger.addAll(building.elevator.get(i).boardedPassenger());
        }

        // print passengers detail to console
        System.out.println("Report for " + Settings.numOfPassengers() + " People :");
        int [] sumOfPeopleTime = new int[6]; 
        System.out.print(" Passenger#   ");        
        for (int i=0; i<6; i++ ) System.out.print(Passenger.STATUS[i].substring(0, 6) + " ");
        System.out.println("\n-----------------------------------------------");
        int numberOfAngry=0, numberOfVeryAngry=0;
        for (Passenger p:allPassenger) {
            System.out.printf(" Passenger#%03d",p.pid());
            int[] eachTimeSpent = p.timeSpent();
            for (int j=0; j<6; j++) {
                sumOfPeopleTime[j] +=eachTimeSpent[j];
                System.out.printf("%7d", eachTimeSpent[j]);
            }
            if (eachTimeSpent[Passenger.WAITING] + eachTimeSpent[Passenger.BOARDING] > eachTimeSpent[Passenger.TRAVELLING])
                numberOfAngry++;            
            if (eachTimeSpent[Passenger.WAITING] + eachTimeSpent[Passenger.BOARDING] >= (eachTimeSpent[Passenger.TRAVELLING] * Settings.veryAngry())
                    && (eachTimeSpent[Passenger.TRAVELLING] * Settings.veryAngry()!=0))
                { numberOfVeryAngry++; System.out.print("Very Angry");}
            System.out.println();
        }

        // print passengers summary to console
        System.out.println("-----------------------------------------------");
        System.out.print(" Passenger#   ");        
        for (int i=0; i<6; i++ ) System.out.print(Passenger.STATUS[i].substring(0, 6) + " ");
        int peopleSum=0;
        System.out.print("\n SUM        :");
        for (int i=0; i<6; i++) {
            System.out.printf("%8d", sumOfPeopleTime[i]);
            peopleSum+= sumOfPeopleTime[i];
        }
        
        // print passenger index 
        System.out.println();
        System.out.println("----- total: " + peopleSum);
        System.out.printf("Percentage of all waiting time [Lesser Better]= %3.1f %%"
                , 100 * (sumOfPeopleTime[Passenger.WAITING] + sumOfPeopleTime[Passenger.BOARDING] ) / (double) peopleSum );
        System.out.printf("\nPercentage of people waited more than travel [Lesser Better] = %3.1f %%"
                , 100*numberOfAngry/(double)Settings.numOfPassengers());
        System.out.printf("\nPercentage of very angry people waited more than 3*travel [Lesser Better] = %3.1f %%"
                , 100*numberOfVeryAngry/((double)Settings.numOfPassengers()));
        System.out.println();

        // print elevators detail to console
        System.out.println("\nReport for " + Settings.numOfElevators() + " Elevator :");        

        int [] sumOfElevatorTime = new int[6]; 
        System.out.print(" Elevators#   "); 
        for (int i=0; i<6; i++ ) System.out.print(Elevator.STATUS[i].substring(0, 4) + " ");        
        System.out.println(); 
        
        for (int i=0; i<Settings.numOfElevators(); i++) {
            Elevator e = building.elevator.get(i);
            System.out.printf(" Elevator#%03d",e.id());
            int[] eachTimeSpent = e.timeSpent();
            for (int j=0; j<6; j++) {
                sumOfElevatorTime[j] +=eachTimeSpent[j];
                System.out.printf("%6d", eachTimeSpent[j]);
            }
            System.out.println();
        }
        
        int s2=0;
        System.out.print(" SUM      :");
        for (int i=0; i<6; i++) {
            System.out.printf("%6d", sumOfElevatorTime[i]);
            s2 += sumOfElevatorTime[i];
        }
        System.out.println("------ total: " + s2);

        // print KPIs for elevators: 
        int energyConsumption = 0;
        energyConsumption = sumOfElevatorTime[Elevator.MOVING] * 1 
                + sumOfElevatorTime[Elevator.DOORCLOSING] * 5
                + sumOfElevatorTime[Elevator.DOOROPENING] * 5;
        double energyConsumPerDistance = ((double)energyConsumption )/ sumOfPeopleTime[Passenger.TRAVELLING]; //sumOfElevatorTime[Elevator.MOVING] ; 
        System.out.println ("Total energy consumption = " + energyConsumption );
        System.out.printf("Energy consumption per travelling distance [Lesser Better] = %3.1f unit", energyConsumPerDistance );
        System.out.printf("\nstop number / travel distance ratio [Lesser Better] = %3.1f %%"
                , 100*sumOfElevatorTime[Elevator.DOOROPENING]/(double) sumOfElevatorTime[Elevator.MOVING]);

        // display information to GUI and file (for last result)
        simFrame.setVisible(false);
        simFrame.remove(graphicPanel);
        simFrame.remove(controlPanel);
        
        String [] resultLabel = {"SIMULATION INFO", "Number of Passengers", "Number of Elevators", "Number of Floors", 
                "Frequency of riding", "Elevator Max Capacity", "Elevator call algorithm", 
                "PASSENGER-----------------", "Travel hour of all Passenger", "Waiting hour of all Passenger", 
                "BUILDING OWNER-----------------", "Travel hour of all Elevator", "Stop and start number of all Elevator",  "Elevator energy consumption",
                "INDEX--------------------", "Number of very ANGRY passenger", "PASSENGER waiting/travel ration", "OWNER energy consumption per moving a floor"  
        };
        
        lastResult = new String [resultLabel.length];
        boolean isFirstRun = false; 
        
        try {
            Scanner openFile = new Scanner (new File("lastresult.out"));
            openFile.useDelimiter("$\n");
            while (openFile.hasNext())  {
                String singleLine = openFile.nextLine ();
                int index = Integer.valueOf(singleLine.substring(0, singleLine.lastIndexOf("!"))); 
                String value = singleLine.substring(singleLine.lastIndexOf("!")+1, singleLine.length()); 
                lastResult [index] = value;
            }
            openFile.close();
        } catch (Exception e) {
            e.printStackTrace();
            isFirstRun = true;
            for (int k=0; k<lastResult.length; k++) {
                lastResult[k] = "No previous result.";
            }
        }
            
        PrintWriter output = null; 
        try {
            output = new PrintWriter (new File ("lastresult.out"));
        } catch (FileNotFoundException e) { e.printStackTrace(); }

        int i = 0; 
        Font f = new Font ("Times", Font.BOLD, 15 * Settings.resolution());    
        resultPanel = new JPanel();
        resultPanel.setLayout(new GridLayout (resultLabel.length, 2 ));

        do {
            JLabel simulLabel = new JLabel (resultLabel[i]);
            simulLabel.setFont(f);
            JLabel result = new JLabel();
            String resultValue;
            switch (resultLabel[i]) {
                case "SIMULATION INFO":         resultValue = "THIS RESULT"; break;
                case "Number of Passengers":    resultValue = String.valueOf(Settings.numOfPassengers());     break;
                case "Number of Elevators":     resultValue = String.valueOf(Settings.numOfElevators());      break; 
                case "Number of Floors":        resultValue = String.valueOf(Settings.numOfFloors());         break;
                case "Frequency of riding":     resultValue = String.valueOf(Settings.frequencyOfRidingElevator() * 3600); break;
                case "Elevator Max Capacity":   resultValue = String.valueOf(Settings.elevatorMaxCapacity()); break;
                case "Elevator call algorithm": resultValue = String.valueOf(Settings.elevatorChoosingAlgorithm()); break;
                case "----":                    resultValue = "------------";       break;
                case "Travel hour of all Passenger":
                    resultValue = String.valueOf(sumOfPeopleTime [Passenger.TRAVELLING]); break;
                case "Waiting hour of all Passenger":
                    resultValue = String.valueOf((sumOfPeopleTime [Passenger.WAITING] + sumOfPeopleTime [Passenger.BOARDING])); break;
                case "Number of very ANGRY passenger":
                    resultValue = String.valueOf(numberOfVeryAngry); break;
                case "Travel hour of all Elevator": 
                    resultValue = String.valueOf(sumOfElevatorTime [Elevator.MOVING]); break;
                case "Stop and start number of all Elevator" : 
                    resultValue = String.valueOf(sumOfElevatorTime [Elevator.DOORCLOSING]); break;
                case "Elevator energy consumption": 
                    resultValue = String.valueOf(energyConsumption); break; 
                case "PASSENGER waiting/travel ration":
                    resultValue = String.valueOf(100 * (sumOfPeopleTime[Passenger.WAITING] + sumOfPeopleTime[Passenger.BOARDING] ) 
                            / sumOfPeopleTime[Passenger.TRAVELLING]); break;
                case "OWNER energy consumption per moving a floor":
                    resultValue = String.valueOf(energyConsumPerDistance); break;                    
                default:                
                    simulLabel.setFont(new Font ("Times", Font.BOLD, 22 * Settings.resolution()));
                    resultValue = "";
            }
            result.setFont(new Font ("Times", Font.BOLD, 20 * Settings.resolution()));
            output.println(i+"!"+(resultValue=="THIS RESULT"? "LAST RESULT" : resultValue));
            
            JLabel lastResultLabel = new JLabel();
            lastResultLabel.setFont(new Font ("Times", Font.BOLD, 20 * Settings.resolution()));            

            if (!isFirstRun) {
                if (i==16 || i==15) {
                    if (Integer.valueOf(lastResult[i]) < Integer.valueOf(resultValue)) lastResult[i] += " BETTER";
                    else if (Integer.valueOf(lastResult[i]) > Integer.valueOf(resultValue)) resultValue += " BETTER";
                } else if (i==17) {
                    lastResult[i] = lastResult[i].substring(0, 5); 
                    resultValue = resultValue.substring(0, 5); 
                    if (Double.valueOf(lastResult[i]) < Double.valueOf(resultValue)) lastResult[i] += " BETTER";
                    else if (Double.valueOf(lastResult[i]) > Double.valueOf(resultValue)) resultValue += " BETTER";                
                }
            }
            result.setText(resultValue);
            lastResultLabel.setText(lastResult [i]);
            
            resultPanel.add(simulLabel); 
            resultPanel.add(result); 
            resultPanel.add(lastResultLabel);
            i++;
        } while (i < resultLabel.length ); 
        output.close();

        JButton exit = new JButton ("EXIT");
        exit.setActionCommand("EXIT");
        exit.setFont(new Font("Arial", Font.BOLD, 20 * Settings.resolution()));
        exit.addActionListener(new ButtonClickListener());
        exit.setPreferredSize(new Dimension (150 * Settings.resolution(), 50 * Settings.resolution() ));

        simFrame.add(resultPanel, BorderLayout.CENTER);
        simFrame.add(exit, BorderLayout.SOUTH);
        simFrame.setVisible(true);
    }

    /**
     * getter for static variable doNotSkip skip GUI simulation display 
     * @return 
     */
    private static boolean doNotSkip () { return doNotSkip; }

    /**
     * make doNotSkip variable false to skip GUI simulation display 
     */
    private static void skip () {doNotSkip = false; }

    /**
     * This static class listen and implement control panel buttons 
     * @author Min Kim
     *
     */
    static class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();  
            if( command.equals( "FASTER" )) { drawSpeed = (drawSpeed > 50) ? drawSpeed -50 : 0 ; }
            if( command.equals( "SLOWER" )) { drawSpeed = (drawSpeed < 1000) ? drawSpeed +50 : 1000 ; }
            if( command.equals( "SKIP" ))  {
                skip(); 
                drawSpeed = 0;
            }
            if ( command.equals("EXIT"))    { System.exit(-1); }
        }
    }
}
