package kfs.tank.comp;

import kfs.tank.ecs.KfsComp;

public class EnemyComp implements KfsComp {
    public EnemyType type;
    public AIState state;
    public float aggroRange;
    public float attackRange;
    public float fireRate;
    public float fireCooldown;
    public float damage;

    public EnemyComp(EnemyType type, float aggroRange, float attackRange, float fireRate, float damage) {
        this.type = type;
        this.state = AIState.IDLE;
        this.aggroRange = aggroRange;
        this.attackRange = attackRange;
        this.fireRate = fireRate;
        this.damage = damage;
    }
}
