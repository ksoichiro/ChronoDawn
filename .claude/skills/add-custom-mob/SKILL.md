---
name: add-custom-mob
description: Comprehensive guide for adding new mobs (hostile, passive, ambient) to ChronoDawn. Covers entity class, model, renderer, registration, spawn configuration, loot tables, and platform-specific setup across all supported Minecraft versions (1.20.1, 1.21.1, 1.21.2). Use when implementing new creatures, monsters, or entities.
---

# Add Custom Mob to ChronoDawn

**Purpose**: Complete guide for adding a new mob entity to ChronoDawn with full multi-version and multi-loader support.

---

## Quick Checklist

When adding a new mob, complete ALL of the following:

### Java Classes (per version: 1.20.1, 1.21.1, 1.21.2)
- [ ] Entity class (`entities/mobs/MobNameEntity.java`)
- [ ] Model class (`client/model/MobNameModel.java`)
- [ ] Renderer class (`client/renderer/mobs/MobNameRenderer.java`)
- [ ] RenderState class (`client/renderer/mobs/MobNameRenderState.java`) - **1.21.2 only**

### Registration (per version)
- [ ] `ModEntities.java` - Entity type registration
- [ ] `ModItems.java` - Spawn egg registration (see [custom-mob-spawn-egg skill](#spawn-egg))

### Platform-Specific Registration
- [ ] **Fabric** `ChronoDawnFabric.java` - Attribute & spawn placement registration
- [ ] **Fabric** `ChronoDawnClientFabric.java` - Model layer & renderer registration
- [ ] **NeoForge** `ChronoDawnNeoForge.java` - Attribute & spawn placement registration
- [ ] **NeoForge** `ChronoDawnClientNeoForge.java` - Model layer & renderer registration
- [ ] **NeoForge** `ChronoDawnClientNeoForge.java` - Spawn egg color handler

### Resource Files (per version)
- [ ] Texture file (`textures/entity/mobs/mob_name.png`)
- [ ] Loot table (`loot_table/entities/mob_name.json` or `loot_tables/` for 1.20.1)
- [ ] Spawn egg model (`models/item/mob_name_spawn_egg.json`)
- [ ] Language files (`lang/en_us.json`, `lang/ja_jp.json`)
- [ ] Biome spawn configuration (if replacing vanilla mob)

---

## 1. Entity Class

Create in `common-{version}/src/main/java/com/chronodawn/entities/mobs/`

### 1.21.2 Template

```java
package com.chronodawn.entities.mobs;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class MobNameEntity extends Monster {

    public MobNameEntity(EntityType<? extends MobNameEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    @Override
    protected void registerGoals() {
        // Add AI goals here
    }

    // 1.21.2: Use EntitySpawnReason, SynchedEntityData.Builder
    // Example for synched data:
    // @Override
    // protected void defineSynchedData(SynchedEntityData.Builder builder) {
    //     super.defineSynchedData(builder);
    //     builder.define(DATA_FLAGS, (byte)0);
    // }
}
```

### 1.21.1 Template

Key differences from 1.21.2:
- Use `MobSpawnType` instead of `EntitySpawnReason`
- `SynchedEntityData.Builder` pattern same as 1.21.2

### 1.20.1 Template

Key differences:
- Use `MobSpawnType` instead of `EntitySpawnReason`
- `defineSynchedData()` has no parameters, use `this.entityData.define()` directly

```java
@Override
protected void defineSynchedData() {
    super.defineSynchedData();
    this.entityData.define(DATA_FLAGS, (byte)0);
}
```

---

## 2. Model Class

Create in `common-{version}/src/main/java/com/chronodawn/client/model/`

### 1.21.2 Template (RenderState pattern)

```java
package com.chronodawn.client.model;

import com.chronodawn.client.renderer.mobs.MobNameRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class MobNameModel extends EntityModel<MobNameRenderState> {

    private final ModelPart body;

    public MobNameModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(MobNameRenderState state) {
        super.setupAnim(state);
        // Animation logic using state.walkAnimationPos, state.walkAnimationSpeed
    }
}
```

### 1.21.1 / 1.20.1 Template (Entity pattern)

```java
package com.chronodawn.client.model;

import com.chronodawn.entities.mobs.MobNameEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
// ... other imports

public class MobNameModel extends EntityModel<MobNameEntity> {

    public MobNameModel(ModelPart root) {
        // 1.21.1/1.20.1: No super(root) call
        this.body = root.getChild("body");
    }

    // Same createBodyLayer() as 1.21.2

    @Override
    public void setupAnim(MobNameEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Animation logic
    }

    // 1.21.1: renderToBuffer with int color
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay, int color) {
        body.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    // 1.20.1: renderToBuffer with float RGBA
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
```

---

## 3. Renderer Class

Create in `common-{version}/src/main/java/com/chronodawn/client/renderer/mobs/`

### 1.21.2 Template

```java
package com.chronodawn.client.renderer.mobs;

import com.chronodawn.ChronoDawn;
import com.chronodawn.client.model.MobNameModel;
import com.chronodawn.entities.mobs.MobNameEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MobNameRenderer extends MobRenderer<MobNameEntity, MobNameRenderState, MobNameModel> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "textures/entity/mobs/mob_name.png");

    public MobNameRenderer(EntityRendererProvider.Context context) {
        super(context, new MobNameModel(context.bakeLayer(MobNameModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(MobNameRenderState state) {
        return TEXTURE;
    }

    @Override
    public MobNameRenderState createRenderState() {
        return new MobNameRenderState();
    }
}
```

### 1.21.1 / 1.20.1 Template

```java
// MobRenderer has 2 type parameters: <Entity, Model>
public class MobNameRenderer extends MobRenderer<MobNameEntity, MobNameModel> {

    @Override
    public ResourceLocation getTextureLocation(MobNameEntity entity) {
        return TEXTURE;
    }

    // No createRenderState() method needed
}
```

---

## 4. RenderState Class (1.21.2 Only)

Create in `common-1.21.2/src/main/java/com/chronodawn/client/renderer/mobs/`

```java
package com.chronodawn.client.renderer.mobs;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class MobNameRenderState extends LivingEntityRenderState {
    // Add custom state fields if needed for animations
}
```

---

## 5. Entity Registration (ModEntities.java)

```java
public static final RegistrySupplier<EntityType<MobNameEntity>> MOB_NAME = ENTITIES.register(
    "mob_name",
    () -> EntityType.Builder.of(MobNameEntity::new, MobCategory.MONSTER)
        .sized(0.6f, 1.8f)  // width, height
        .clientTrackingRange(8)
        .updateInterval(3)
        .build(ResourceKey.create(Registries.ENTITY_TYPE,
            CompatResourceLocation.create(ChronoDawn.MOD_ID, "mob_name")))
);
```

**1.20.1 difference**: Use `.build("mob_name")` instead of `.build(ResourceKey...)`

---

## 6. Platform-Specific Registration

### Fabric - ChronoDawnFabric.java

```java
// Attributes
FabricDefaultAttributeRegistry.register(ModEntities.MOB_NAME.get(), MobNameEntity.createAttributes());

// Spawn placement (1.21.1/1.21.2)
SpawnPlacements.register(
    ModEntities.MOB_NAME.get(),
    SpawnPlacementTypes.ON_GROUND,
    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
    Monster::checkMonsterSpawnRules
);

// Spawn placement (1.20.1)
SpawnPlacements.register(
    ModEntities.MOB_NAME.get(),
    SpawnPlacements.Type.ON_GROUND,  // Note: different enum
    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
    Monster::checkMonsterSpawnRules
);
```

### Fabric - ChronoDawnClientFabric.java

```java
// Model layer
EntityModelLayerRegistry.registerModelLayer(MobNameModel.LAYER_LOCATION, MobNameModel::createBodyLayer);

// Renderer
EntityRendererRegistry.register(ModEntities.MOB_NAME.get(), MobNameRenderer::new);
```

### NeoForge - ChronoDawnNeoForge.java

```java
// In onEntityAttributeCreation event
event.put(ModEntities.MOB_NAME.get(), MobNameEntity.createAttributes().build());

// In onSpawnPlacementRegister event
event.register(
    ModEntities.MOB_NAME.get(),
    SpawnPlacementTypes.ON_GROUND,
    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
    Monster::checkMonsterSpawnRules,
    SpawnPlacementRegisterEvent.Operation.AND
);
```

### NeoForge - ChronoDawnClientNeoForge.java

```java
// In onRegisterEntityRenderers event
event.registerEntityRenderer(ModEntities.MOB_NAME.get(), MobNameRenderer::new);

// In onRegisterLayerDefinitions event
event.registerLayerDefinition(MobNameModel.LAYER_LOCATION, MobNameModel::createBodyLayer);
```

---

## 7. Resource Files

### Loot Table

**1.21.1 / 1.21.2** (`data/chronodawn/loot_table/entities/mob_name.json`):
```json
{
  "type": "minecraft:entity",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:bone",
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": { "min": 0, "max": 2, "type": "minecraft:uniform" }
            },
            {
              "function": "minecraft:enchanted_count_increase",
              "enchantment": "minecraft:looting",
              "count": { "type": "minecraft:uniform", "min": 0, "max": 1 }
            }
          ]
        }
      ]
    }
  ]
}
```

**1.20.1** (`data/chronodawn/loot_tables/entities/mob_name.json`):
- Directory is `loot_tables` (plural)
- Use `"function": "minecraft:looting_enchant"` instead of `enchanted_count_increase`

### Language Files

```json
"entity.chronodawn.mob_name": "Mob Name",
"item.chronodawn.mob_name_spawn_egg": "Mob Name Spawn Egg"
```

### Biome Spawn Configuration

To replace a vanilla mob in ChronoDawn biomes, edit `worldgen/biome/chronodawn_*.json`:

```json
{
  "spawners": {
    "monster": [
      { "type": "chronodawn:mob_name", "weight": 100, "minCount": 4, "maxCount": 4 }
    ]
  }
}
```

---

## 8. Spawn Egg

**IMPORTANT**: See the `custom-mob-spawn-egg` skill for complete spawn egg implementation checklist.

Quick reminder:
- Register in `ModItems.java`
- Add to `initializeSpawnEggs()` and `populateCreativeTab()`
- Create `models/item/mob_name_spawn_egg.json` with `"parent": "item/template_spawn_egg"`
- Add to NeoForge color handler in `ChronoDawnClientNeoForge.java`

---

## Version API Differences Summary

| Feature | 1.20.1 | 1.21.1 | 1.21.2 |
|---------|--------|--------|--------|
| Spawn type enum | `MobSpawnType` | `MobSpawnType` | `EntitySpawnReason` |
| SynchedEntityData | `defineSynchedData()` no params | `defineSynchedData(Builder)` | `defineSynchedData(Builder)` |
| EntityModel type param | `EntityModel<Entity>` | `EntityModel<Entity>` | `EntityModel<RenderState>` |
| MobRenderer type params | 2 (`Entity, Model`) | 2 (`Entity, Model`) | 3 (`Entity, RenderState, Model`) |
| renderToBuffer color | `float r,g,b,a` | `int color` | N/A (handled by RenderState) |
| EntityType.Builder.build() | `build("name")` | `build(ResourceKey)` | `build(ResourceKey)` |
| SpawnPlacements.Type | `SpawnPlacements.Type` | `SpawnPlacementTypes` | `SpawnPlacementTypes` |
| Loot table directory | `loot_tables/` | `loot_table/` | `loot_table/` |
| Looting enchant function | `looting_enchant` | `enchanted_count_increase` | `enchanted_count_increase` |
| RenderState class | Not needed | Not needed | Required |

---

## Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Entity not spawning | Missing attribute registration | Add to Fabric/NeoForge attribute events |
| Model not rendering | Missing model layer registration | Add to client init (both loaders) |
| Crash on entity spawn | Wrong API for version | Check version differences table above |
| Entity invisible | Missing renderer registration | Add to EntityRendererRegistry |
| Spawn egg issues | See spawn egg skill | Consult `custom-mob-spawn-egg` skill |
