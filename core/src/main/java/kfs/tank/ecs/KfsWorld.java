package kfs.tank.ecs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class KfsWorld implements KfsSystem {

    private long nextId;
    private final Map<Class<? extends KfsComp>, Map<Entity, KfsComp>> components;
    private final List<KfsSystem> updatableSystemBaseList;

    protected KfsWorld() {
        this.nextId = 1;
        this.components = new HashMap<>();
        this.updatableSystemBaseList = new ArrayList<>();
    }

    protected void reset() {
        components.clear();
        this.nextId = 1;
    }

    protected synchronized Entity createEntity() {
        return new Entity(nextId++);
    }

    public void deleteEntity(Entity entity) {
        for (Map.Entry<Class<? extends KfsComp>, Map<Entity, KfsComp>> entry : components.entrySet()) {
            entry.getValue().remove(entity);
        }
    }

    public <T extends KfsComp> void removeComponent(Entity e, Class<T> componentClass) {
        components
            .computeIfAbsent(componentClass, k -> new HashMap<>())
            .remove(e);
    }

    public <T extends KfsComp> void addComponent(Entity e, T component) {
        components
            .computeIfAbsent(component.getClass(), k -> new HashMap<>())
            .put(e, component);
    }

    @SuppressWarnings("unchecked")
    public <T extends KfsComp> T getComponent(Entity e, Class<T> type) {
        return type.cast(components.getOrDefault(type, Map.of()).get(e));
    }

    public List<Entity> getEntitiesWith(Class<? extends KfsComp> c1) {
        return new ArrayList<>(components.getOrDefault(c1, Map.of()).keySet());
    }

    @SuppressWarnings("unchecked")
    public <T extends KfsComp> List<Entity> getEntitiesWith(Class<T> c1, Predicate<T> condition) {
        return components.getOrDefault(c1, Map.of()).entrySet().stream()
            .filter(entry -> condition.test((T) entry.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public List<Entity> getEntitiesWith(Class<? extends KfsComp> c1, Class<? extends KfsComp> c2) {
        Set<Entity> ids1 = components.getOrDefault(c1, Map.of()).keySet();
        Set<Entity> ids2 = components.getOrDefault(c2, Map.of()).keySet();

        List<Entity> result = new ArrayList<>();
        for (Entity id : ids1) {
            if (ids2.contains(id)) result.add(id);
        }
        return result;
    }

    public List<Entity> getEntitiesWith(Class<? extends KfsComp> c1, Class<? extends KfsComp> c2, Class<? extends KfsComp> c3) {
        Set<Entity> ids1 = components.getOrDefault(c1, Map.of()).keySet();
        Set<Entity> ids2 = components.getOrDefault(c2, Map.of()).keySet();
        Set<Entity> ids3 = components.getOrDefault(c3, Map.of()).keySet();

        List<Entity> result = new ArrayList<>();
        for (Entity id : ids1) {
            if (ids2.contains(id) && ids3.contains(id))
                result.add(id);
        }
        return result;
    }

    public void addSys(KfsSystem updatableSystemBase) {
        updatableSystemBaseList.add(updatableSystemBase);
    }

    public void removeSys(KfsSystem updatableSystemBase) {
        updatableSystemBaseList.remove(updatableSystemBase);
    }

    public Iterable<KfsSystem> getSystems() {
        return updatableSystemBaseList;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getSystems(Class<T> type) {
        List<T> lst = new ArrayList<>();
        for (KfsSystem systemBase : updatableSystemBaseList) {
            if (type.isAssignableFrom(systemBase.getClass())) {
                lst.add((T) systemBase);
            }
        }
        return lst;
    }

    @SuppressWarnings("unchecked")
    public <T> T getSystem(Class<T> type) {
        for (KfsSystem system : updatableSystemBaseList) {
            if (type.isAssignableFrom(system.getClass())) {
                return (T) system;
            }
        }
        return null;
    }

    @Override
    public void init() {
        runSystems(KfsSystem::init);
    }

    @Override
    public void update(float delta) {
        runSystems(systemBase -> systemBase.update(delta));
    }

    @Override
    public void render(SpriteBatch batch) {
        runSystems(systemBase -> systemBase.render(batch));
    }

    @Override
    public void done() {
        runSystems(KfsSystem::done);
    }

    protected void runSystems(Consumer<? super KfsSystem> us) {
        updatableSystemBaseList.forEach(us);
    }
}
