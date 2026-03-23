package kfs.tank.comp;

import kfs.tank.ecs.KfsComp;

public class PlayerComp implements KfsComp {
    public int score;
    public int kills;
    public float fuel;
    public float maxFuel;
    public float fuelDrainIdle;
    public float fuelDrainMove;
    public float fuelEmptyTimer; // counts up when fuel=0
    public float speedBoostTimer;
    public float shieldTimer;

    public PlayerComp(float maxFuel, float drainIdle, float drainMove) {
        this.fuel = maxFuel;
        this.maxFuel = maxFuel;
        this.fuelDrainIdle = drainIdle;
        this.fuelDrainMove = drainMove;
    }
}
