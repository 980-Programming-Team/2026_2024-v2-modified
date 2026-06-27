// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants.DriveTeamConstants;
import frc.robot.Constants.OperatorConstants;
import frc.robot.commands.CollectCommand;
import frc.robot.commands.IndexCommand;
import frc.robot.commands.IndexToShooter;
import frc.robot.commands.OuttakeCommand;
import frc.robot.subsystems.AmpShooter;
import frc.robot.subsystems.Collector;
import frc.robot.subsystems.PhotonFinder;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.SwerveSubsystem;

import java.io.File;

import com.pathplanner.lib.auto.AutoBuilder;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a "declarative" paradigm, very
 * little robot logic should actually be handled in the {@link Robot} periodic methods (other than the scheduler calls).
 * Instead, the structure of the robot (including subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer
{

  // The robot's subsystems and commands are defined here...
  private final SwerveSubsystem drivebase = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(),
                                                                         "swerve"));

  private final Shooter shooter = new Shooter();
  public final static Collector collector = new Collector();
  private final AmpShooter ampShooter = new AmpShooter();
  private final PhotonFinder finder = new PhotonFinder();


  private final SendableChooser<Command> autoChooser;


      // Replace with CommandPS4Controller or CommandJoystick if needed
  private final CommandXboxController driver = new CommandXboxController(DriveTeamConstants.driver);

  private final CommandXboxController operator = new CommandXboxController(DriveTeamConstants.operator);

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer()
  {
    // Configure the trigger bindings
    registerCommands();
    configurePathPlanner();
    configureBindings();

    autoChooser = AutoBuilder.buildAutoChooser();
    SmartDashboard.putData("Auto", autoChooser);

    // Applies deadbands and inverts controls because joysticks
    // are back-right positive while robot
    // controls are front-left positive
    // left stick controls translation
    // right stick controls the angular velocity of the robot
    Command driveFieldOrientedAnglularVelocity = drivebase.driveCommand(
        () -> MathUtil.applyDeadband(-driver.getLeftY(), OperatorConstants.LEFT_Y_DEADBAND),
        () -> MathUtil.applyDeadband(-driver.getLeftX(), OperatorConstants.LEFT_X_DEADBAND),
        () -> -driver.getRightX());

    Command driveFieldOrientedDirectAngleSim = drivebase.simDriveCommand(
        () -> MathUtil.applyDeadband(-driver.getLeftY(), OperatorConstants.LEFT_Y_DEADBAND),
        () -> MathUtil.applyDeadband(-driver.getLeftX(), OperatorConstants.LEFT_X_DEADBAND),
        () -> -driver.getRightX());

    drivebase.setDefaultCommand(
        !RobotBase.isSimulation() ? driveFieldOrientedAnglularVelocity : driveFieldOrientedDirectAngleSim);

    shooter.setDefaultCommand(Commands.run(
      () -> shooter.tiltShooter(operator.getRightY()),
      shooter
    ));

    // //ALLOWS POSE2D TO GO ON SMARTDASHBOARD 
    // //TODO NEEDS TO RUN ENTIRE TIME WHILST IN SIMULATION
    // Field2d field2d = new Field2d();
    // Pose2d pose = drivebase.getPose();
    // field2d.setRobotPose(pose);

    // SmartDashboard.putData("2D Pose" , field2d);
    
  }

  public void configurePathPlanner() 
  {



      drivebase.setupPathPlanner();

  }

  private void registerCommands()
  {

          // NamedCommands GO HERE 
      // Example: NamedCommands.registerCommand("ShootNote", new Shooter());

    
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary predicate, or via the
   * named factories in {@link edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for
   * {@link CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller PS4}
   * controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight joysticks}.
   */
  private void configureBindings()
  {

    driver.a().onTrue((Commands.runOnce(drivebase::zeroGyro)));
    operator.rightBumper().whileTrue(new CollectCommand());
    operator.rightTrigger().whileTrue(new IndexToShooter());
    
    // operator.leftBumper().whileTrue(new OuttakeCommand());
    // operator.rightBumper().whileTrue((Commands.runOnce(collector::intake))); //indexIntoShooter
    // operator.leftBumper().whileTrue((Commands.runOnce(collector::off)));
    // // driverXbox.x().whileTrue(Commands.runOnce(drivebase::lock, drivebase).repeatedly());




    //prajbox safety switch on activates climbers on sticks, disables collector
    //hold button to reverse

    

  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand()
  {
    // An example command will be run in autonomous
    return autoChooser.getSelected();
  }

  public void setDriveMode()
  {
    //drivebase.setDefaultCommand();
  }

  public void setMotorBrake(boolean brake)
  {
    drivebase.setMotorBrake(brake);
  }
}
