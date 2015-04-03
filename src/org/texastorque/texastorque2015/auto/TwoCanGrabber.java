package org.texastorque.texastorque2015.auto;

import org.texastorque.texastorque2015.constants.Constants;

public class TwoCanGrabber extends AutoMode {

    @Override
    public void run() {
        runCommand(new DriveDistance("touch drive", -1.5, 1, 10, 10));
        leftStingerSpeed = 1.0;
        rightStingerSpeed = 1.0;
        double time = Constants.leftStingerP.getDouble();
        
        wait(time);
        
        leftStingerSpeed = 0.0;
        rightStingerSpeed = 0.0;
        
        wait(0.1);
        
//        leftSpeed = 1.0;
//        rightSpeed = 1.0;
//        
//        wait(1.0);
//        
//        leftSpeed = 0.0;
//        rightSpeed = 0.0;
    }

}