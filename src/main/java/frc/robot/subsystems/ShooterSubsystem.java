package frc.robot.subsystems;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.MedianFilter;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ShooterSubsystem extends SubsystemBase {

    private static final double BALL_EXISTS_DISTANCE = 3.5; // Inches
    private final CANSparkMax shooterMotor1;
    private final CANSparkMax shooterMotor2;
    private final Solenoid shooterSolenoid;
    private Ultrasonic ballSensor;
    private final CANPIDController shooterController;
    private final CANEncoder shooterEncoder;
    private final NetworkTableInstance ntInstance = NetworkTableInstance.getDefault();
    private final NetworkTable table = ntInstance.getTable("/components/launcher");
    private final NetworkTableEntry rpm = table.getEntry("filtered_rpm");
    private final MedianFilter rangeFilter = new MedianFilter(3);

    public ShooterSubsystem(
            CANSparkMax shooterMotor1,
            CANSparkMax shooterMotor2,
            Solenoid shooterSolenoid,
            CANEncoder encoder,
            Ultrasonic ballSensor) {
        this.shooterMotor1 = shooterMotor1;
        this.shooterMotor2 = shooterMotor2;
        this.shooterSolenoid = shooterSolenoid;
        this.shooterController = shooterMotor1.getPIDController();
        this.shooterEncoder = encoder;

        this.shooterController.setFF(0.00022211);
        this.shooterController.setP(0.000022211);

        Ultrasonic.setAutomaticMode(true);
        ballSensor.setEnabled(true);
        this.ballSensor = ballSensor;

        shooterMotor1.setInverted(true);
        shooterMotor2.follow(shooterMotor1);
    }

    public boolean isBallReady() {
        return rangeFilter.calculate(ballSensor.getRangeInches()) <= BALL_EXISTS_DISTANCE;
    }

    public void activatePiston() {
        shooterSolenoid.set(true);
        rangeFilter.reset();
    }

    public void lowerPiston() {
        shooterSolenoid.set(false);
    }

    public void shootVelocity(double shooterSpeed) {
        shooterController.setReference(shooterSpeed, ControlType.kVelocity);
    }

    public void shootVoltage(double shooterSpeed) {
        shooterMotor1.set(shooterSpeed);
    }

    public void periodic() {
        rpm.setDouble(this.shooterEncoder.getVelocity());
        rangeFilter.calculate(ballSensor.getRangeInches());
    }
}
