package kfs.tank.comp;

import kfs.tank.ecs.KfsComp;

public class HealthComp implements KfsComp {
    public int hp;
    public int maxHp;
    public float armor; // 0-1, percentage of damage absorbed
    public float invTimer; // invincibility timer

    public HealthComp(int maxHp) {
        this.hp = maxHp;
        this.maxHp = maxHp;
        this.armor = 0;
    }

    public void takeDamage(int damage) {
        if (invTimer > 0) return;
        int actual = Math.max(1, (int)(damage * (1f - armor)));
        hp = Math.max(0, hp - actual);
    }

    public boolean isDead() {
        return hp <= 0;
    }
}
