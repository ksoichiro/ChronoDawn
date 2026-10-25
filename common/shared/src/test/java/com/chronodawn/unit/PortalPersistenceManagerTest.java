package com.chronodawn.unit;

import com.chronodawn.core.portal.PortalPersistenceManager;
import com.chronodawn.core.portal.PortalRegistry;
import com.chronodawn.core.portal.PortalState;
import com.chronodawn.core.portal.PortalStateMachine;
import com.chronodawn.data.PortalRegistryData;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PortalPersistenceManagerTest {
    private static final BlockPos FRAME_POS = new BlockPos(0, 220, 32);

    @BeforeAll
    static void initialize() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        PortalPersistenceManager.initialize();
    }

    @AfterEach
    void clearRegistry() {
        PortalRegistry.getInstance().setSavedData(null);
        PortalRegistry.getInstance().clear();
    }

    @Test
    void beforeStart_discardsPreviousWorldEntriesAndSavedDataReference() {
        PortalRegistry registry = PortalRegistry.getInstance();
        PortalRegistryData previousData = new PortalRegistryData();
        registry.setSavedData(previousData);
        registerPortal();
        previousData.setDirty(false);
        // Populate the cached dimension view as well as the position index.
        assertFalse(registry.getPortalsInDimension(Level.END).isEmpty());

        LifecycleEvent.SERVER_BEFORE_START.invoker().stateChanged(null);
        PortalRegistryData newWorldData = new PortalRegistryData();

        assertTrue(registry.getAllPortals().isEmpty());
        assertTrue(registry.getPortalsInDimension(Level.END).isEmpty());
        assertNull(registry.getPortalAt(Level.END, FRAME_POS));
        registerPortal();
        assertFalse(previousData.isDirty(), "New world changes must not dirty the old world's data");
        assertFalse(newWorldData.isDirty());
    }

    @Test
    void stopped_clearsEntriesAndDetachesSavedData() {
        PortalRegistry registry = PortalRegistry.getInstance();
        PortalRegistryData data = new PortalRegistryData();
        registry.setSavedData(data);
        registerPortal();
        data.setDirty(false);

        LifecycleEvent.SERVER_STOPPED.invoker().stateChanged(null);

        assertTrue(registry.getAllPortals().isEmpty());
        assertNull(registry.getPortalAt(Level.END, FRAME_POS));
        registerPortal();
        assertFalse(data.isDirty(), "Stopped world's data must be detached");
    }

    @Test
    void stopping_preservesPortalsForFinalSave_thenNextWorldLoadsSavedPortals() {
        PortalRegistry registry = PortalRegistry.getInstance();
        PortalRegistryData data = new PortalRegistryData();
        registry.setSavedData(data);
        PortalStateMachine portal = registerPortal();

        LifecycleEvent.SERVER_STOPPING.invoker().stateChanged(null);
        assertSame(portal, registry.getPortalAt(Level.END, FRAME_POS));
        CompoundTag saved = data.saveData(new CompoundTag());
        LifecycleEvent.SERVER_STOPPED.invoker().stateChanged(null);
        LifecycleEvent.SERVER_BEFORE_START.invoker().stateChanged(null);
        assertTrue(registry.getAllPortals().isEmpty());

        PortalRegistryData loadedData = new PortalRegistryData();
        loadedData.loadData(saved);
        registry.setSavedData(loadedData);

        PortalStateMachine loaded = registry.getPortalAt(Level.END, FRAME_POS);
        assertNotNull(loaded);
        assertEquals(portal.getPortalId(), loaded.getPortalId());
        assertEquals(PortalState.DEACTIVATED, loaded.getCurrentState());
        assertTrue(registry.getPortalsInDimension(Level.END).contains(loaded.getPortalId()));
        assertFalse(loadedData.isDirty());
        registry.markDirtyForPortal(loaded.getPortalId());
        assertTrue(loadedData.isDirty());
    }

    private static PortalStateMachine registerPortal() {
        PortalStateMachine portal = new PortalStateMachine(UUID.randomUUID(), Level.END, FRAME_POS);
        portal.setState(PortalState.DEACTIVATED);
        PortalRegistry.getInstance().registerPortal(portal);
        return portal;
    }
}
