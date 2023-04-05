package de.javaabc.aipopulation.objects;

import de.javaabc.aipopulation.geom.Rot;
import de.javaabc.aipopulation.geom.Vec;
import de.javaabc.aipopulation.util.Tickable;

import java.awt.*;
import java.io.Serializable;

public abstract class MovableObject extends SimulationObject implements Tickable, Serializable {
    protected Vec speed;
    protected Vec acc;
    protected Rot rot;
    protected Rot rotSpeed;
    protected Rot rotAcc;

    public MovableObject(Vec pos, Vec speed, Rot rot, Rot rotSpeed, Color color) {
        super(pos, color);
        this.speed = speed;
        this.acc = Vec.ZERO;
        this.rot = rot;
        this.rotSpeed = rotSpeed;
        this.rotAcc = Rot.ZERO;
    }

    @Override
    public void tick() {
        speed = speed.add(acc);
        rotSpeed = rotSpeed.add(rotAcc);
        pos = pos.add(speed);
        rot = rot.add(rotSpeed);
        bounds = makeBounds();
    }

    public Rot getRot() {
        return rot;
    }
}
