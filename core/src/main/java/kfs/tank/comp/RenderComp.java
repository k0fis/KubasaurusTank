package kfs.tank.comp;

import com.badlogic.gdx.graphics.Color;
import kfs.tank.ecs.KfsComp;

public class RenderComp implements KfsComp {
    public Color color;
    public float width, height;
    public int zOrder;

    public RenderComp(Color color, float width, float height) {
        this.color = color;
        this.width = width;
        this.height = height;
    }
}
