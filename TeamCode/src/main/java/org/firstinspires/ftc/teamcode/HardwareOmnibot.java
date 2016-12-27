package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsAnalogOpticalDistanceSensor;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.Servo;
import java.lang.Thread;

import static java.lang.Math.*;

/**
 *Created by Ethan
 */
public class HardwareOmnibot
{
    /* Public OpMode members. */
    public final static double HIGH_SHOOT_SPEED = 0.95;
    public final static double MID_HIGH_SHOOT_SPEED = 0.90;
    public final static double MID_LOW_SHOOT_SPEED = 0.85;
    public final static double LOW_SHOOT_SPEED = 0.80;
    public final static double MAX_SPIN_RATE = 0.6;
    public final static int MIN_COLOR_VALUE = 2;
    public DcMotor leftMotorFore = null;
    public DcMotor rightMotorFore = null;
    public DcMotor leftMotorRear = null;
    public DcMotor rightMotorRear = null;
    public DcMotor sweeperMotor = null;
    public DcMotor liftMotor = null;
    public DcMotor shootMotor1 = null;
    public DcMotor shootMotor2 = null;
    public Servo buttonPush = null;
    public ModernRoboticsI2cGyro robotGyro = null;
    public ModernRoboticsAnalogOpticalDistanceSensor ods1 = null;
    public double ambientLight = 0;
    public ColorSensor colorSensor = null;

    // Might have to lower from max 2800
    private static final int encoderClicksPerSecond = 2800;

    /* local OpMode members. */
    private HardwareMap hwMap  =  null;

    /* Constructor */
    public HardwareOmnibot(){

    }

    public void initColorSensor()
    {
        colorSensor = hwMap.colorSensor.get("color_sensor");
        colorSensor.enableLed(false);
    }

    public void initOds()
    {
        ods1 = hwMap.get(ModernRoboticsAnalogOpticalDistanceSensor.class, "ods1");
        ambientLight = ods1.getLightDetected();
    }

    public double readOds()
    {
        return ods1.getLightDetected();
    }

    public void initGyro()
    {
        // Init Gyro Code
        robotGyro = hwMap.get(ModernRoboticsI2cGyro.class, "robot_gyro");
        robotGyro.setI2cAddress(I2cAddr.create8bit(0x20));
    }

    public void resetGyro()
    {
        // Reset Gyro Code
        robotGyro.calibrate();
        while(!robotGyro.isCalibrating()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }

    public double readGyro()
    {
        // Read Gyro Code
        double heading = (double)robotGyro.getHeading();
        heading = abs(heading - 360.0);
        return heading;
    }

    // xPower: -1.0 to 1.0 power in the X axis
    // yPower: -1.0 to 1.0 power in the Y axis
    // spin: -1.0 to 1.0 power to spin, reduced to 20%
    public void drive(double xPower, double yPower, double spin, double angleOffset)
    {
        // Read Gyro Angle Here
        double reducedSpin = spin * MAX_SPIN_RATE;
        double gyroAngle = readGyro() + angleOffset;
        double leftFrontAngle = toRadians(45.0 + gyroAngle);
        double rightFrontAngle = toRadians(315.0 + gyroAngle);
        double leftRearAngle = toRadians(135.0 + gyroAngle);
        double rightRearAngle = toRadians(225.0 + gyroAngle);

        if(abs(yPower) < 0.1)
        {
            yPower = 0.0;
        }
        if(abs(xPower) < 0.1)
        {
            xPower = 0.0;
        }
        if(abs(spin) < MAX_SPIN_RATE)
        {
            reducedSpin = 0.0;
        }

        double LFpower = (xPower * cos(leftFrontAngle) + yPower * sin(leftFrontAngle))/sqrt(2) + reducedSpin;
        double LRpower = (xPower * cos(leftRearAngle) + yPower * sin(leftRearAngle))/sqrt(2) + reducedSpin;
        double RFpower = (xPower * cos(rightFrontAngle) + yPower * sin(rightFrontAngle))/sqrt(2) + reducedSpin;
        double RRpower = (xPower * cos(rightRearAngle) + yPower * sin(rightRearAngle))/sqrt(2) + reducedSpin;

        double maxPower = max(1.0, max(max(LFpower, LRpower),
                max(RFpower, RRpower)));

        LFpower /= maxPower;
        RFpower /= maxPower;
        RFpower /= maxPower;
        RRpower /= maxPower;

        leftMotorFore.setPower(LFpower);
        rightMotorFore.setPower(RFpower);
        leftMotorRear.setPower(LRpower);
        rightMotorRear.setPower(RRpower);
    }

    public void resetDriveEncoders()
    {
        int sleepTime = 0;
        int encoderCount = leftMotorFore.getCurrentPosition();

        rightMotorFore.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftMotorRear.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightMotorRear.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftMotorFore.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        while((encoderCount != 0) && (sleepTime < 1000)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
            sleepTime += 10;
            encoderCount = leftMotorFore.getCurrentPosition();
        }

        leftMotorFore.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotorFore.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftMotorRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotorRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }
    /* Initialize standard Hardware interfaces */
    public void init(HardwareMap ahwMap) {
        // Save reference to Hardware map
        hwMap = ahwMap;

        // Define and Initialize Motors
        leftMotorFore = hwMap.dcMotor.get("MotorLF");
        rightMotorFore  = hwMap.dcMotor.get("MotorRF");
        leftMotorRear = hwMap.dcMotor.get("MotorLR");
        rightMotorRear = hwMap.dcMotor.get("MotorRR");
        sweeperMotor = hwMap.dcMotor.get("SweeperMotor");
        liftMotor = hwMap.dcMotor.get("LiftMotor");
        shootMotor1 = hwMap.dcMotor.get("Shoot1");
        shootMotor2 = hwMap.dcMotor.get("Shoot2");
        buttonPush = hwMap.servo.get("ServoButton");

        leftMotorFore.setDirection(DcMotor.Direction.FORWARD);
        rightMotorFore.setDirection(DcMotor.Direction.FORWARD);
        leftMotorRear.setDirection(DcMotor.Direction.FORWARD);
        rightMotorRear.setDirection(DcMotor.Direction.FORWARD);

        sweeperMotor.setDirection(DcMotor.Direction.REVERSE);
        liftMotor.setDirection(DcMotor.Direction.FORWARD);
        shootMotor1.setDirection(DcMotor.Direction.FORWARD);
        shootMotor2.setDirection(DcMotor.Direction.REVERSE);

        // Set all motors to zero power
        leftMotorFore.setPower(0);
        rightMotorFore.setPower(0);
        leftMotorRear.setPower(0);
        rightMotorRear.setPower(0);
        sweeperMotor.setPower(0);
        liftMotor.setPower(0);
        shootMotor1.setPower(0);
        shootMotor2.setPower(0);

        resetDriveEncoders();
        shootMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shootMotor2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        sweeperMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        liftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        leftMotorFore.setMaxSpeed(encoderClicksPerSecond);
        rightMotorFore.setMaxSpeed(encoderClicksPerSecond);
        leftMotorRear.setMaxSpeed(encoderClicksPerSecond);
        rightMotorRear.setMaxSpeed(encoderClicksPerSecond);
        shootMotor1.setMaxSpeed(encoderClicksPerSecond);
        shootMotor2.setMaxSpeed(encoderClicksPerSecond);

        initGyro();
        initOds();
        initColorSensor();
    }

    /***
     *
     * waitForTick implements a periodic delay. However, this acts like a metronome with a regular
     * periodic tick.  This is used to compensate for varying processing times for each cycle.
     * The function looks at the elapsed cycle time, and sleeps for the remaining time interval.
     *
     * @param periodMs  Length of wait cycle in mSec.
     * @throws InterruptedException
     */
    /*
    public void waitForTick(long periodMs) throws InterruptedException {

        long  remaining = periodMs - (long)period.milliseconds();

        // sleep for the remaining portion of the regular cycle period.
        if (remaining > 0)
            Thread.sleep(remaining);

        // Reset the cycle clock for the next pass.
        period.reset();
    }
    */
}

