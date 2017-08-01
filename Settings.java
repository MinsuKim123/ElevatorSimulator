import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * This public class sets all simulation variables and
 * stores as static variables for access. 
 * (Need GETTER methods or should be initialized as FINAL, 
 *  but couldn't make it in time)
 * 
 * @author Min Kim
 *
 */    
class Settings {
    private static int numOfElevators ; 
    private static int numOfFloors;
    private static int numOfPassengers; 
    private static double frequencyOfRidingElevator;  
    private static int elevatorMaxCapacity;
    private static int LoopCounter; 
    private static int elevatorChoosingAlgorithm; 
    private static int veryAngry;  
    private static int resolution; 
    private static boolean waitBoolean;  
    private static String [] configValue; 
    private static JTextField [] configTextField = new JTextField [9] ;
    private static JButton doIt; 

    public static int numOfElevators()  { return numOfElevators; }
    public static int numOfFloors()     { return numOfFloors; }
    public static int numOfPassengers() { return numOfPassengers; }
    public static double frequencyOfRidingElevator() { return frequencyOfRidingElevator; }  
    public static int elevatorMaxCapacity() { return elevatorMaxCapacity; }
    public static int LoopCounter()     { return LoopCounter;}
    public static int elevatorChoosingAlgorithm() {return elevatorChoosingAlgorithm; }
    public static int veryAngry ()      { return veryAngry; }
    public static int resolution ()     { return resolution; }
    
    /**
     * This method is called only from Simulation main class
     */
    public static void initSettings () {
        waitBoolean = false;
        configValue = new String[] 
                {"5", "10", "1000", "1", "10", "120", "0", "5", "2"};
        String [] configTitle = 
            { "Number of elevators ",
              "Number of floors ",
              "Number of Passengers",
              "Riding frequency for one hour ", 
              "Elevator capacity ", 
              "Simulation Duration in minute", 
              "Elevator call algorithm [0~2]", 
              "Passenger patience ", 
              "Resolution: [1-normal, 2-high]"
            };

        // Frame init
        JFrame initFrame = new JFrame();
        initFrame.setLayout(new BorderLayout());
        initFrame.setSize(1000, 800);

        // title area
        JLabel title = new JLabel ("Configure your simulation !"); 
        title.setFont(new Font("Times", Font.BOLD, 50));
        title.setBackground(Color.CYAN);
        JPanel titlePanel = new JPanel(); 
        titlePanel.setLayout(new GridLayout(1,1));
        titlePanel.add(title);

        // config area
        JPanel initConfig = new JPanel();
        initConfig.setLayout(new GridLayout(configTitle.length,1));
        Font f = new Font ("Times", Font.BOLD, 30);    

        for (int i=0; i < configTitle.length; i++) {
            JLabel label = new JLabel(configTitle[i]+": ");
            label.setFont(f);
            configTextField[i] = new JTextField (configValue[i]); 
            configTextField[i].setFont(f);
            initConfig.add(label); 
            initConfig.add(configTextField[i]);
        }

        // command
        doIt = new JButton ("Do It!");
        doIt.setFont(new Font("Times", Font.BOLD, 30));
        doIt.setActionCommand("DO");
        doIt.addActionListener (new ButtonClickListener());

        JPanel doItPanel = new JPanel();
        doItPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        doItPanel.add(doIt);

        initFrame.add(titlePanel, BorderLayout.NORTH);
        initFrame.add(initConfig, BorderLayout.CENTER);
        initFrame.add(doItPanel, BorderLayout.SOUTH);
        initFrame.setVisible(true);
         
        while (waitBoolean == false ) {
            try {
                 Thread.sleep(200);
            } catch (Exception e) {}
        }
        initFrame.setVisible(false);
    }
    
    static class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();  
            if( command.equals( "DO" ))  {
                try {
                    numOfElevators = Integer.parseInt(configTextField[0].getText());
                    numOfFloors = Integer.valueOf(configTextField[1].getText());
                    numOfPassengers = Integer.valueOf(configTextField[2].getText());
                    frequencyOfRidingElevator = Double.valueOf(configTextField[3].getText()) / 3600; 
                    elevatorMaxCapacity = Integer.valueOf(configTextField[4].getText());
                    LoopCounter  = 60 * Integer.valueOf(configTextField[5].getText()); 
                    elevatorChoosingAlgorithm = Integer.valueOf(configTextField[6].getText());
                    veryAngry = Integer.valueOf(configTextField[7].getText());
                    resolution = Integer.valueOf(configTextField[8].getText());


                    if (numOfPassengers <= 0 || numOfFloors <=0 || numOfElevators <=0
                            || frequencyOfRidingElevator <= 0 || elevatorMaxCapacity <=0 
                            || LoopCounter <=0 || veryAngry <=0 ) 
                        throw new Exception ("The value can't be 0 or lesser.");

                    if (elevatorChoosingAlgorithm < 0 || elevatorChoosingAlgorithm > 2  ) 
                        throw new Exception ("Elevator algoritym is only 0, 1, 2 ");

                    if (resolution < 1 || resolution > 2  ) 
                        throw new Exception ("Resolution is only 1, 2 ");
                    
                    waitBoolean = true;
                    return; 
                } catch (Exception exc) {
                    exc.printStackTrace();
                    doIt.setText("Check input and RETRY");
                    waitBoolean = false; 
                }              
            }
        }
    }

}
