package de.javaabc.aipopulation.objects;

import de.javaabc.aipopulation.geom.Rot;
import de.javaabc.aipopulation.geom.Vec;
import de.javaabc.aipopulation.util.Tickable;

import java.awt.*;
import java.io.Serializable;

/**
 * A {@link SimulationObject} that can move.
 *
 * @author Timo Friedl
 */
public abstract class MovableObject extends SimulationObject implements Tickable, Serializable {
    /**
     * the translational speed of this object in px / tick
     */
    protected Vec speed;

    /**
     * the translational acceleration of this object in px / tick^2
     */
    protected Vec acc;

    /**
     * the angle of this object in radians
     */
    protected Rot rot;

    /**
     * the angular velocity of this object in radians / tick
     */
    protected Rot rotSpeed;

    /**
     * the angular acceleration of this object in radians / tick^2
     */
    protected Rot rotAcc;

    /**
     * Creates a new movable object.
     *
     * @param pos      the position of this object on screen in px
     * @param speed    the translational speed of this object in px / tick
     * @param rot      the angle of this object in radians
     * @param rotSpeed the angular velocity of this object in radians / tick
     * @param color    the color of this object
     */
    protected MovableObject(Vec pos, Vec speed, Rot rot, Rot rotSpeed, Color color) {
        super(pos, color);
        this.speed = speed;
        this.acc = Vec.ZERO;
        this.rot = rot;
        this.rotSpeed = rotSpeed;
        this.rotAcc = Rot.ZERO;
    }

    @Override
    public void tick() {
        /* Note that this is not a physically accurate way of computing kinematics.
           There are more numerically stable options. */

        // Increment speed by acceleration
        speed = speed.add(acc);
        rotSpeed = rotSpeed.add(rotAcc);

        // Increment position by speed
        pos = pos.add(speed);
        rot = rot.add(rotSpeed);

        // Create bounds of this object
        bounds = makeBounds();
    }

    public Rot getRot() {
        return rot;
    }
}
