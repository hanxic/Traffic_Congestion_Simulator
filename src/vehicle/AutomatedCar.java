/*
 * See abstract parent class for method/variable explanations
 */
package vehicle;

import java.util.Arrays;
import setting.Lane;
import traffic_congestion_simulator.TCSConstant;

/**
 *
 * @author Christine
 */
//Mimics cars with automated acceleration
public class AutomatedCar extends Vehicle implements TCSConstant {

    //Creates a Automated car.
    public AutomatedCar(double[] position, double[] size, double direction) {
        speed[0] = 0;
        speed[1] = 0;
        safety_distance = 0;
        speed_limit = TCSConstant.AUTOMATEDFINALVELOCITY;
        time_moving = 0;
        is_turning = false;
        reaction_time = 0;
        turning_acceleration = 0;
        turn_radius = 0;
        turning_velocity = 0;
        turn_initial_position = new double[2];
        turn_initial_direction = 0;
        turn_safety_angle = 0;

        //may change/dump these two methods later
        this.genRandAcceleration();
        this.genRandDeceleration();

        this.position = position;
        this.size = size;
        this.direction = direction;
    }

    @Override
    public void accelerate(boolean accelerate) {
        double acceleration;
        if (accelerate) {
            acceleration = acceleration_rate;
        } else {
            acceleration = deceleration_rate;
        }

        position[0] += speed[0] * TCSConstant.TIMEINCREMENTS 
                + 1.0 / 2 * acceleration * TCSConstant.TIMEINCREMENTS * TCSConstant.TIMEINCREMENTS * Math.cos(Math.toRadians(direction));
        position[0] = this.rounder(position[0]);
        speed[0] += acceleration * TCSConstant.TIMEINCREMENTS * Math.abs(Math.cos(Math.toRadians(direction)));
        speed[0] = this.rounder(speed[0]);

        position[1] += speed[1] * TCSConstant.TIMEINCREMENTS 
                + 1.0 / 2 * acceleration * TCSConstant.TIMEINCREMENTS * TCSConstant.TIMEINCREMENTS * Math.sin(Math.toRadians(direction));;
        position[1] = this.rounder(position[1]);
        speed[1] += acceleration * TCSConstant.TIMEINCREMENTS * Math.abs(Math.sin(Math.toRadians(direction)));
        speed[1] = this.rounder(speed[1]);

        //just a precaution in case estimating the decelerating to stop gives a negative speed
        if (speed[0] * Math.abs(Math.cos(Math.toRadians(direction))) < 0
                || speed[1] * Math.abs(Math.sin(Math.toRadians(direction))) < 0) {
            speed[0] = 0;
            speed[1] = 1;
        }

        updateSafetyDistance();
        time_moving += TCSConstant.TIMEINCREMENTS;
        is_accelerating = false;

    }

    //For automated car there is no reaction time.
    @Override
    public void genRandReactionTime() {
        reaction_time = 0;
    }

    //Updates car safe distances, so the car will avoid accident.
    @Override
    public void updateSafetyDistance() {
        if (!this.is_turning) {
            safety_distance = Math.pow(this.getDirectionalSpeed(), 2) / (2 * -deceleration_rate);
        } else {
            //safety distance while turning
            //not implemented yet
        }
    }

    //Randomly generating acceleration and deceleration functions:
    //randomly generated from gaussian distribution of average values
    //may change variance if necessary
    @Override
    public void genRandAcceleration() {
        acceleration_rate = Math.abs(rand.nextGaussian() * ACCELERATIONAVGMAX / 10 + ACCELERATIONAVGMAX);
        acceleration_rate = this.rounder(acceleration_rate);
    }

    //deceleration relys on generated acceleration to avoid unrealistic/conflicting rates
    @Override
    public void genRandDeceleration() {
        double scaled_dec_avg_max = DECELERATIONAVGMAX / ACCELERATIONAVGMAX * acceleration_rate;
        deceleration_rate = rand.nextGaussian() * scaled_dec_avg_max / 10 + scaled_dec_avg_max;
        deceleration_rate = this.rounder(deceleration_rate);
    }


    @Override
    public void setTurningConstants(double[] destination, boolean accelerate) {
        //slighty dimished acceleration rate to more realistically model a turn
        //also acceleration is assumed to be constant
        if (accelerate) {
            turning_acceleration = 2.0 / 3.0 * this.acceleration_rate;
        } else {
            turning_acceleration = 3.0 / 4.0 * this.deceleration_rate;
        }

        turning_velocity = this.getDirectionalSpeed() * 3.0 / 4.0;

        //radius of quarter circle that is being used to model the turn
        turn_radius = Math.abs(destination[0] - this.position[0]);

        //initial values stored so turn() can be called many times and edit Vehicle vairables
        turn_initial_position[0] = this.position[0];
        turn_initial_position[1] = this.position[1];
        turn_initial_direction = this.direction;

    }

    @Override
    public void turn(int direction, double[] destination, boolean accelerate) {
        //  this will run one time when turn() is first called, sets the constants 
        //and positions car halfway in the intersection to prepare to begin turn
        if (!this.is_turning) {

            /*
            if (this.isTravelingHorizontal()) {
                position[0] += size[0] * Math.cos(Math.toRadians(this.direction)) / 2;
            } else {
                position[1] += size[0] * Math.sin(Math.toRadians(this.direction)) / 2;
            }
             */
            this.setTurningConstants(destination, accelerate);
            //for testing:
            //System.out.println("I ran. Position: " + Arrays.toString(position));
        }
        is_turning = true;

        //the change of angle is used to model acceleration
        //the change is calculated based on one time increment of turning
        turning_velocity += turning_acceleration * time_increments;
        if (turning_velocity < 0) {
            turning_velocity = 0;
        }

        double angle_increment = turning_velocity * time_increments / turn_radius;
        //testing:
        //System.out.println("Angle inc: " + Math.toDegrees(angle_increment));

        //keeps track of how far car has travelled in the intersection
        double[] turn_pos = {Math.abs(Math.abs(position[0]) - Math.abs(turn_initial_position[0])),
            Math.abs(Math.abs(position[1]) - Math.abs(turn_initial_position[1]))};

        //starting facing left or right
        if (this.turn_initial_direction == 0 || this.turn_initial_direction == 180) {
            //  keeps track of what reference angle should be used for each position component
            //and whether each position component should be positively or negatively incremented
            int neg_or_pos = (int) (Math.cos(Math.toRadians(this.turn_initial_direction))
                    * Math.sin(Math.toRadians(this.turn_initial_direction + direction)));

            //changes dirrection of car based on calculated incremental angle change
            this.direction += neg_or_pos * Math.toDegrees(angle_increment);

            //updates position components for one time increment of turning
            position[0] += Math.cos(Math.toRadians(this.turn_initial_direction))
                    * (turn_radius * Math.abs(Math.cos(Math.toRadians(this.direction
                            - neg_or_pos * 90))) - turn_pos[0]);

            position[1] += Math.sin(Math.toRadians(this.turn_initial_direction + direction)) * (turn_radius - turn_pos[1] - Math.abs(turn_radius
                    * Math.sin(Math.toRadians(this.direction - neg_or_pos * 90))));

            //starting facing up or down
        } else {
            int neg_or_pos = (int) (-1 * Math.cos(Math.toRadians(this.turn_initial_direction + direction))
                    * Math.sin(Math.toRadians(this.turn_initial_direction)));

            this.direction += neg_or_pos * Math.toDegrees(angle_increment);

            position[0] += Math.cos(Math.toRadians(this.turn_initial_direction + direction)) * (turn_radius - turn_pos[0] - Math.abs(turn_radius
                    * Math.cos(Math.toRadians(this.direction - neg_or_pos * 90))));

            position[1] += Math.sin(Math.toRadians(this.turn_initial_direction))
                    * (turn_radius * Math.abs(Math.sin(Math.toRadians(this.direction
                            - neg_or_pos * 90))) - turn_pos[1]);

        }

        time_moving += time_increments;

        //checks if the car has finished the turn or not
        if (direction == -90 && this.direction <= turn_initial_direction - 90
                || direction == 90 && this.direction >= turn_initial_direction + 90) {

            //rounds angle down/up to be exactly 0, 90, 180, or 270
            if (this.direction >= 360) {
                this.direction = 0;
            } else if (this.direction < -1) {
                this.direction = 270;
            } else {
                this.direction = turn_initial_direction + direction;
            }

            //  moves car slightly forward in whatever direction it is facing so 
            //the whole car is past limit line (with back tires on it)
            if (this.isTravelingHorizontal()) {
                position[0] = destination[0] + size[0] * Math.cos(Math.toRadians(this.direction));
                position[1] = destination[1];
            } else {
                position[1] = destination[1] + size[0] * Math.sin(Math.toRadians(this.direction));
                position[0] = destination[0];
            }

            is_turning = false;
        }

    }
    
    
    @Override
    public void updateTurnSafetyAngle(){
        
    }
    

    /**
     * This method will give the point at which the driver (robot in this case)
     * will start to break;
     *
     * @param x_value
     * @param y_value
     * @param destination
     * @return
     */
    @Override
    public double[] estimateBreakingPoint(double x_value, double y_value) {
        // general equation (deceleration_rate * destination - acceleration_rate * position + 1 / 2 * velocity * velocity) / (deceleration_rate - acceleration_rate)
        double[] breakingPoint = new double[2];
        breakingPoint[0] = (deceleration_rate * Math.abs(Math.cos(Math.toRadians(direction))) * x_value - acceleration_rate * Math.abs(Math.cos(Math.toRadians(direction))) * position[0] + 1 / 2 * Math.pow(speed[0], 2)) / (deceleration_rate * Math.abs(Math.cos(Math.toRadians(direction))) - acceleration_rate * Math.abs(Math.cos(Math.toRadians(direction))));
        breakingPoint[1] = (deceleration_rate * Math.abs(Math.sin(Math.toRadians(direction))) * y_value - acceleration_rate * Math.abs(Math.sin(Math.toRadians(direction))) * position[1] + 1 / 2 * Math.pow(speed[1], 2)) / (deceleration_rate * Math.abs(Math.sin(Math.toRadians(direction))) - acceleration_rate * Math.abs(Math.sin(Math.toRadians(direction))));

        return breakingPoint;
    }

    /**
     * This method will give the point at which the driver (robot in this case)
     * will start to break;
     *
     * @param v
     * @param destination
     * @return
     */
    @Override
    public double[] estimateBreakingPoint(Vehicle v) {
        // general equation (deceleration_rate * destination - acceleration_rate * position + 1 / 2 * velocity * velocity) / (deceleration_rate - acceleration_rate)
        double[] destination = new double[2];
        for (int i = 0; i < 2; i++) {
            destination[0] = v.getPosition()[0] + safety_distance * Math.abs(Math.cos(Math.toRadians(direction)));
            destination[1] = v.getPosition()[1] + safety_distance * Math.abs(Math.sin(Math.toRadians(direction)));
        }
        double[] breakingPoint = new double[2];
        breakingPoint[0] = (deceleration_rate * Math.abs(Math.cos(Math.toRadians(direction))) * destination[0] - acceleration_rate * Math.abs(Math.cos(Math.toRadians(direction))) * position[0] + 1 / 2 * Math.pow(speed[0], 2)) / (deceleration_rate * Math.abs(Math.cos(Math.toRadians(direction))) - acceleration_rate * Math.abs(Math.cos(Math.toRadians(direction))));
        breakingPoint[1] = (deceleration_rate * Math.abs(Math.sin(Math.toRadians(direction))) * destination[1] - acceleration_rate * Math.abs(Math.sin(Math.toRadians(direction))) * position[1] + 1 / 2 * Math.pow(speed[1], 2)) / (deceleration_rate * Math.abs(Math.sin(Math.toRadians(direction))) - acceleration_rate * Math.abs(Math.sin(Math.toRadians(direction))));

        return breakingPoint;
    }

    @Override
    public boolean getAutomated() {
        return true;
    }

    /*
    public double getDistanceFromTurningVehicle(Vehicle2 front_car){
        //no implementation yet
        
        return distance;
    }
     
 
    public double getDistanceFromLimitLine (Lane lane){
        //not implemented yet
        
        return distance;
    }
     */
    //returns exact time needed to decelerate to stop
    public double timeToStop() {
        double speed = this.getDirectionalSpeed();
        double time = -speed / this.deceleration_rate;
        return time;
    }

    //total number of increments needed to decelerate to stop
    public int incrementsToStop() {
        double increments = this.timeToStop() / time_increments;
        return (int) Math.floor(increments);
    }

    //returns exact time needed to accelerate to speed_limit
    public double timeToSpeedLimit() {
        double speed = this.getDirectionalSpeed();
        double time = -(speed_limit - speed) / this.deceleration_rate;
        return time;
    }

    //total number of increments needed to accelerate to speed_limit
    public int incrementsToSpeedLimit() {
        double increments = this.timeToSpeedLimit() / time_increments;
        return (int) Math.floor(increments);
    }


    //Obsolete functions, explained in abstract Vehicle class
    //still might find a use for code/logic in other classes
    /*
    public void accelerateToSpeed(double speed) throws InterruptedException {
        double speed_dir = this.getDirectionalSpeed();
        while (speed_dir < speed) {
            accelerate(time_increments, acceleration_rate);
            speed_dir = this.getDirectionalSpeed();
        }
    }

    public void decelerateToSpeed(double speed) throws InterruptedException {
        double speed_dir = this.getDirectionalSpeed();
        while (speed_dir > speed) {
            accelerate(time_increments, deceleration_rate);
            speed_dir = this.getDirectionalSpeed();
        }
    }

    public void accelerateToSpeedLimit() throws InterruptedException {
        accelerateToSpeed(speed_limit);
    }

    public void decelerateToStop() throws InterruptedException {
        decelerateToSpeed(0);
    }

    public void travelDistanceToStop(double distance) throws InterruptedException {
        double initial_pos = this.getDirectionalPos();
        while (distance > safety_distance) {
            if (this.getDirectionalSpeed() < speed_limit) {
                accelerate(time_increments, acceleration_rate);
            } else {
                accelerate(time_increments, 0);
            }
            distance -= Math.abs(initial_pos - this.getDirectionalPos());
            initial_pos = this.getDirectionalPos();
        }
        this.decelerateToStop();
    }
     */
    public void decelerate(double[] pos) {
        double ax = -Math.pow(speed[0], 2) / (2 * pos[0] - position[0]);
        double ay = -Math.pow(speed[1], 2) / (2 * pos[1] - position[1]);

        double deltaPosX = (speed[0] * TCSConstant.TIMEINCREMENTS + 1.0 / 2 * ax * TCSConstant.TIMEINCREMENTS * TCSConstant.TIMEINCREMENTS);
        position[0] += deltaPosX;
        position[0] = this.rounder(position[0]);
        speed[0] += this.rounder(ax * TCSConstant.TIMEINCREMENTS);

        double deltaPosY = (speed[1] * TCSConstant.TIMEINCREMENTS + 1.0 / 2 * ax * TCSConstant.TIMEINCREMENTS * TCSConstant.TIMEINCREMENTS);
        position[1] += deltaPosY;
        position[1] = this.rounder(position[1]);
        speed[1] += this.rounder(ay * TCSConstant.TIMEINCREMENTS);
        updateSafetyDistance();
        time_moving += TCSConstant.TIMEINCREMENTS;
    }
}
