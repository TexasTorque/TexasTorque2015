package org.texastorque.texastorque2015.auto;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.texastorque.texastorque2015.input.Input;
import org.texastorque.texastorque2015.constants.Constants;

public abstract class AutoMode extends Input {

    public class DriveDistance extends AutoCommand {

        private double distance;
        private double doneRange;

        public DriveDistance(String name, double distance, double tolerance, double doneCycles) {
            super(name, doneCycles);

            this.distance = distance;
            this.doneRange = tolerance;

            drivebaseControlled = true;
            driveDistance = distance;
            driveAngle = 0.0;
            
            feedback.resetDriveEncoders();
        }

        @Override
        public boolean isDone() {
            if (Math.abs((feedback.getLeftDrivePosition() + feedback.getRightDrivePosition()) / 2 - distance) <= doneRange) {
                doneCycles++;
            }
            return doneCycles >= minDoneCycles;
        }

        @Override
        public void stop() {
            drivebaseControlled = false;
            leftSpeed = 0.0;
            rightSpeed = 0.0;
            feedback.resetDriveEncoders();
        }
    }

    public class PickupTote extends AutoCommand {

        private int step;
        private boolean done;

        public PickupTote(String name, double doneCycles) {
            super(name, doneCycles);
            step = 0;
            done = false;
        }

        @Override
        public void run() {
            switch (step) {
                case 0:
                    elevatorPosition = Constants.FloorElevatorLevel2.getDouble();
                    if (feedback.getElevatorHeight() == Constants.FloorElevatorLevel2.getDouble()) {
                        intakeSpeed = 1.0;

                        step++;
                    }
                    break;
                case 1:
                    elevatorPosition = Constants.FloorElevatorLevel1.getDouble();
                    if (feedback.getElevatorHeight() == Constants.FloorElevatorLevel1.getDouble()) {
                        step++;
                    }
                    break;
                case 2:
                    elevatorPosition = Constants.FloorElevatorLevel2.getDouble();
                    done = true;
                    break;
                default:
            }
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public void reset() {
            super.reset();
            step = 0;
        }

        @Override
        public void stop() {
            intakeSpeed = 0.0;
            step = 0;
        }
    }

    public class TurnAngle extends AutoCommand {

        private double angle;
        private double doneRange;

        public TurnAngle(String name, double angle, double tolerance, double doneCycles) {
            super(name, doneCycles);

            this.angle = angle;
            this.doneRange = tolerance;

            drivebaseControlled = true;
            driveAngle = angle;
            driveDistance = 0.0;
            
            feedback.resetGyro();
        }

        @Override
        public boolean isDone() {
            if (Math.abs(feedback.getAngle() - angle) <= doneRange) {
                doneCycles++;
            }
            return doneCycles > minDoneCycles;
        }

        @Override
        public void stop() {
            drivebaseControlled = false;
            leftSpeed = 0.0;
            rightSpeed = 0.0;
            feedback.resetGyro();
        }
    }

    protected void runCommand(AutoCommand command) {
        command.reset();
        while (!command.isDone()) {
            command.run();
            wait(0.01);
        }
        command.stop();
    }

    protected void wait(double time) {
        try {
            Thread.sleep((long) (time * 1000));
        } catch (InterruptedException ex) {
        }
    }
}
