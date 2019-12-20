package ServoingTest;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

import java.io.IOException;
import java.net.ConnectException;

import javax.inject.Inject;

import com.kuka.common.StatisticTimer;
import com.kuka.common.ThreadUtil;
import com.kuka.common.StatisticTimer.OneTimeStep;
import com.kuka.connectivity.motionModel.smartServoLIN.ISmartServoLINRuntime;
import com.kuka.connectivity.motionModel.smartServoLIN.SmartServoLIN;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.AbstractFrame;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.task.ITaskLogger;

public class ClientTest extends RoboticsAPIApplication {
	
	// robot
	@Inject
    private LBR lbr;
    private ISmartServoLINRuntime smartServoLINRuntime = null;
	
    // socket
	private String serverHost = "172.31.1.151"; // server IP hostname
	private int serverPort = 30001;             // server PORT number (LBR iiwa allows 30000 -> 30010)
	
	// process
    private static final int NUM_RUNS = 600;
	
	@Inject	
	private ITaskLogger log;

	@Override
	public void initialize() {
		
	}

	@Override
	public void run() {
			
		// move to home position
		lbr.move(ptp(getApplicationData().getFrame("/HomePosition")));
		
        // Create a new smart servo linear motion
		AbstractFrame initialPosition = lbr.getCurrentCartesianPosition(lbr.getFlange());
        SmartServoLIN aSmartServoLINMotion = new SmartServoLIN(initialPosition);
        aSmartServoLINMotion.setMinimumTrajectoryExecutionTime(20e-3);
        aSmartServoLINMotion.setTimeoutAfterGoalReach(10); // abort motion after 10s
        getLogger().info("Starting the SmartServoLIN in position control mode");
        lbr.getFlange().moveAsync(aSmartServoLINMotion);
        getLogger().info("Get the runtime of the SmartServoLIN motion");
        smartServoLINRuntime = aSmartServoLINMotion.getRuntime();
        StatisticTimer timing = new StatisticTimer();
        
        int attempts = 0;
        int maxAttempts = 10;
        
        do{
        	try {    
    			// create new client socket and connect to the server
    			TCPSocket clientSocket = new TCPSocket(serverPort, serverHost);
    			clientSocket.write("im a robot");
    	        // Start the smart servo lin sine movement
    	        timing = trackMouseMotion(smartServoLINRuntime, timing, clientSocket);
    	        
    		} catch (ConnectException e) {
    			getLogger().info("Connection refused by Server - " + (maxAttempts - attempts) + " remaining..");
    			attempts++;
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        }while(attempts < maxAttempts );
        
        
        ThreadUtil.milliSleep(1000);

        getLogger().info("Print statistic timing");
        getLogger().info(getClass().getName() + smartServoLINRuntime.toString());

        getLogger().info("Stop the SmartServoLIN motion");
        smartServoLINRuntime.stopMotion();

        // Statistic Timing of sine movement loop
        if (timing.getMeanTimeMillis() > 150){
            getLogger().info("Statistic Timing is unexpected slow, you should try to optimize TCP/IP Transfer");
            getLogger().info("Under Windows, you should play with the registry, see the e.g. user manual");
        }
			


	}
	
	private StatisticTimer trackMouseMotion(ISmartServoLINRuntime smartServoLINRuntime, StatisticTimer timing, TCPSocket clientSocket){
        
		//Frame startPos = smartServoLINRuntime.getCurrentCartesianDestination(lbr.getFlange());
		Frame startPos = lbr.getCurrentCartesianPosition(lbr.getFlange());
		
        getLogger().info("Track the mouse pointer");
        try
        {
            //for (int i = 0; i < NUM_RUNS; ++i)
        	while(true)
            {
                final OneTimeStep aStep = timing.newTimeStep();
                
                // Update the smartservo LIN runtime
                smartServoLINRuntime.updateWithRealtimeSystem();

                // Compute the destination frame
                Frame destFrame = new Frame(startPos);
                int[] goalPosition = getPositionFromServer(clientSocket);
                destFrame.setX(startPos.getX() - goalPosition[0]);
                destFrame.setY(startPos.getY() - goalPosition[1]);
                clientSocket.write("x:" + startPos.getX() + "y:" + startPos.getY());
                clientSocket.write("x:" + destFrame.getX() + "y:" + destFrame.getY());
                // Set new destination
                smartServoLINRuntime.setDestination(destFrame);
                aStep.end();
            }

        }
        catch (Exception e)
        {
            getLogger().error(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return timing;
	}
	
	private int[] getPositionFromServer(TCPSocket clientSocket) throws IOException{
		int[] goalPosition = {0,0,0};
		int[] resolutionOfServerScreen = {1920,1080};
		
        // get the frame data from the server
		//clientSocket.write("im a robot");
		String[] dataIn = clientSocket.read().split(",");
		goalPosition[0] = (Integer.parseInt(dataIn[0])/10);
		goalPosition[1] = (Integer.parseInt(dataIn[1])/10);
		goalPosition[2] = 0; // not used
		//getLogger().info(String.valueOf(goalPosition[0]));
		return goalPosition;
	}
}