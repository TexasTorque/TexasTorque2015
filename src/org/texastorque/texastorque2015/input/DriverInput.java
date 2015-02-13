package org.texastorque.texastorque2015.input;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.texastorque2015.constants.Constants;
import org.texastorque.torquelib.util.GenericController;
import org.texastorque.torquelib.util.TorqueToggle;

public class DriverInput extends Input {

    GenericController driver;
    OperatorConsole operator;

    TorqueToggle tiltToggle;
    TorqueToggle armOpenToggle;

    private boolean wentToBottom;
    private double toteInTime;
    private boolean toteAvailable;

    public DriverInput() {
        driver = new GenericController(0, GenericController.TYPE_XBOX, 0.2);
        operator = new OperatorConsole(1);

        tiltToggle = new TorqueToggle();
        armOpenToggle = new TorqueToggle();

        wentToBottom = false;
        toteAvailable = false;
    }

    @Override
    public void run() {
        //Drivebase
        calcDrivebase();

        //Elevator
        elevatorOverride = operator.getElevatorOverrideSwitch();
        elevatorFFpOff = operator.getElevatorFFpOffSwitch();

        if (operator.getAutoStackButton()) {
            autoStack = true;
        }
        feederStack = operator.getFeederStackButton();
        autoStack = false;
        feederStack = true;
        elevatorOverride = false;

        //Calculate what all of the subsystems should do either independently or 
        //synchronized for complicated actions.
        if (elevatorOverride) {
            calcElevatorOverride();
            calcArms();
            calcIntake();
        } else if (autoStack) {
            armOpen = false;
            tiltUp = false;
            punchOut = false;

            SmartDashboard.putNumber("POS_TEST", 0);
            if (elevatorPosition == Constants.FloorElevatorLevel1.getDouble() && feedback.isElevatorDone()) {
                SmartDashboard.putNumber("POS_TEST", 1);
                elevatorPosition = Constants.FloorElevatorLevel2.getDouble();
                wentToBottom = true;
                intakeSpeed = 0.0;
                intakesIn = false;
            } else if (elevatorPosition == Constants.FloorElevatorLevel2.getDouble() && wentToBottom && feedback.isElevatorDone()) {
                SmartDashboard.putNumber("POS_TEST", 2);
                autoStack = false;
                toteAvailable = false;
            } else {
                SmartDashboard.putNumber("POS_TEST", 3);
                elevatorPosition = Constants.FloorElevatorLevel1.getDouble();
                intakeSpeed = 1.0;
                intakesIn = true;
            }
        } else if (feederStack) {
            double currentTime = Timer.getFPGATimestamp();

            elevatorPosition = Constants.FloorElevatorLevel3.getDouble();
            armOpen = false;
            punchOut = false;
            tiltUp = false;

            SmartDashboard.putBoolean("toteAvaliable", toteAvailable);

            if (feedback.isToteInSluice() && !toteAvailable) {
                toteInTime = Timer.getFPGATimestamp();
                toteAvailable = true;
                SmartDashboard.putNumber("feederstack", 0);
            } else if (toteAvailable) {
                if (currentTime - Constants.ToteSluiceWaitTime.getDouble() > toteInTime) {
                    SmartDashboard.putNumber("feederstack", 1);
                    intakeSpeed = 1.0;
                    if (currentTime - Constants.ToteSluiceWaitTime.getDouble() - Constants.TotePullBAckTime.getDouble() > toteInTime) {
                        autoStack = true;
                        SmartDashboard.putNumber("feederstack", 2);
                    }
                } else {
                    if (feedback.isElevatorDone()) {
                        intakeSpeed = -1.0;
                        intakesIn = true;
                        SmartDashboard.putNumber("feederstack", 3);
                    } else {
                        intakeSpeed = 0.0;
                        intakesIn = false;
                        SmartDashboard.putNumber("feederstack", 4);
                    }
                }
            }
        } else {
            calcElevator();
            calcArms();
            calcIntake();
        }
    }

//Drivebase
    private void calcDrivebase() {
        /**
         * Left stick controls translation, right stick controls rotation. Both
         * the forward and strafe wheels are utilized for rotation.
         */
        if (driver.getRightBumper()) { //Turn over front of robot
            leftSpeed = -1 * driver.getLeftYAxis() + driver.getRightXAxis() * 0.1;
            rightSpeed = -1 * driver.getLeftYAxis() - driver.getRightXAxis() * 0.1;
            frontStrafeSpeed = -1 * driver.getLeftXAxis() - driver.getRightXAxis() * 121 / 400;
            rearStrafeSpeed = -1 * driver.getLeftXAxis() + driver.getRightXAxis();
        } else if (driver.getLeftBumper()) { //Turn over back of robot
            leftSpeed = -1 * driver.getLeftYAxis() + driver.getRightXAxis() * 0.1;
            rightSpeed = -1 * driver.getLeftYAxis() - driver.getRightXAxis() * 0.1;
            frontStrafeSpeed = -1 * driver.getLeftXAxis() - driver.getRightXAxis();
            rearStrafeSpeed = -1 * driver.getLeftXAxis() + driver.getRightXAxis() * 100 / 841;
        } else { //Turn over center of robot
            leftSpeed = -1 * driver.getLeftYAxis() + driver.getRightXAxis();
            rightSpeed = -1 * driver.getLeftYAxis() - driver.getRightXAxis();
            frontStrafeSpeed = -1 * driver.getLeftXAxis() - driver.getRightXAxis() * 16 / 25;
            rearStrafeSpeed = -1 * driver.getLeftXAxis() + driver.getRightXAxis();
        }
    }

    //Elevator
    private void calcElevator() {
        if (operator.getPlaceButton()) {
            elevatorPosition = Constants.SPElevatorLevel1.getDouble();
        }
    }

    private void calcElevatorOverride() {
        if (operator.getElevatorUpButton()) {
            overrideElevatorMotorSpeed = 0.4;
        } else if (operator.getElevatorDownButton()) {
            overrideElevatorMotorSpeed = -0.4;
        }
    }

    //Arms
    private void calcArms() {
        tiltToggle.calc(operator.getTiltButton());
        tiltUp = tiltToggle.get();

        armOpenToggle.calc(operator.getArmOpenButton());
        if (operator.getPlaceButton()) {
            armOpen = true;
        } else {
            armOpen = armOpenToggle.get();
        }

        punchOut = operator.getPunchButton();
    }

    //Intake
    private void calcIntake() {
        if (operator.getIntakeButton()) {
            intakeSpeed = 1.0;
            intakesIn = true;
        } else {
            intakeSpeed = 0.0;
            intakesIn = false;
        }
    }
}
