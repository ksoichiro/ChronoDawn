package com.chronodawn.build

import net.querz.nbt.io.NBTDeserializer
import net.querz.nbt.io.NBTSerializer
import net.querz.nbt.io.NamedTag
import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.ListTag
import net.querz.nbt.tag.StringTag

/**
 * Replaces block names in NBT structure file palettes.
 * Uses the same NBT library (Querz NBT) as the upstream
 * minecraft-mod-gradle-scripts plugin for future migration compatibility.
 */
class NbtBlockReplacer {

    /**
     * Block entity string fields that name a block placed into the world, but live outside
     * the palette so `replacePaletteBlocks` never sees them:
     * - `final_state`: what a vanilla jigsaw block turns into once the structure resolves.
     * - `ReplaceWith`: ChronoDawn's own field on `boss_room_boundary_marker`, holding the
     *   block the marker becomes after it has served its purpose.
     */
    private static final List<String> BLOCK_ID_FIELDS = ['final_state', 'ReplaceWith']

    final Map<String, String> blockMappings

    NbtBlockReplacer(Map<String, String> blockMappings) {
        this.blockMappings = blockMappings
    }

    /**
     * Replace block names in the palette of an NBT structure file, and replace
     * matching item IDs stored inside container block entities (chests, barrels,
     * etc.) using the same mapping. Structure NBT keeps block entity data inline
     * as `blocks[].nbt`, and item stacks carry `id` at the stack root (with
     * nested `item.id` in Data Components layouts); both locations are handled.
     * Block entity fields that name a block placed later (see BLOCK_ID_FIELDS) are rewritten too.
     * The 1.20.1 format conversion runs downstream and will see the replaced IDs.
     *
     * @param input gzip-compressed NBT bytes
     * @return gzip-compressed NBT bytes with replaced block/item names
     */
    byte[] replace(byte[] input) {
        def namedTag = new NBTDeserializer(true).fromBytes(input)
        def root = namedTag.getTag() as CompoundTag

        replacePaletteBlocks(root)
        replaceBlockEntityData(root)

        return serializeToBytes(namedTag)
    }

    private void replacePaletteBlocks(CompoundTag root) {
        if (!root.containsKey("palette")) return
        def palette = root.getListTag("palette")
        if (palette == null) return
        for (int i = 0; i < palette.size(); i++) {
            def entry = palette.get(i) as CompoundTag
            if (entry.containsKey("Name")) {
                def name = entry.getString("Name")
                def replacement = blockMappings.get(name)
                if (replacement != null) {
                    entry.put("Name", new StringTag(replacement))
                }
            }
        }
    }

    private void replaceBlockEntityData(CompoundTag root) {
        // Structure NBT: blocks[].nbt holds the block entity payload inline.
        if (root.containsKey("blocks")) {
            def blocks = root.getListTag("blocks")
            if (blocks != null) {
                for (int i = 0; i < blocks.size(); i++) {
                    def block = blocks.get(i) as CompoundTag
                    if (!block.containsKey("nbt")) continue
                    def be = block.get("nbt")
                    if (be instanceof CompoundTag) {
                        replaceItemsList(be as CompoundTag)
                        replaceBlockIdFields(be as CompoundTag)
                    }
                }
            }
        }
        // Some NBT dialects expose a top-level block_entities list — handle defensively.
        if (root.containsKey("block_entities")) {
            def blockEntities = root.getListTag("block_entities")
            if (blockEntities != null) {
                for (int i = 0; i < blockEntities.size(); i++) {
                    def be = blockEntities.get(i) as CompoundTag
                    replaceItemsList(be)
                    replaceBlockIdFields(be)
                }
            }
        }
    }

    private void replaceBlockIdFields(CompoundTag blockEntity) {
        for (String field : BLOCK_ID_FIELDS) {
            if (!blockEntity.containsKey(field)) continue
            def tag = blockEntity.get(field)
            if (!(tag instanceof StringTag)) continue

            // Values may carry block state properties, e.g. "minecraft:oak_stairs[facing=north]".
            // Only the id is mapped; the property suffix is preserved verbatim.
            def value = (tag as StringTag).getValue()
            int bracket = value.indexOf('[')
            def id = bracket >= 0 ? value.substring(0, bracket) : value
            def replacement = blockMappings.get(id)
            if (replacement != null) {
                def suffix = bracket >= 0 ? value.substring(bracket) : ''
                blockEntity.put(field, new StringTag(replacement + suffix))
            }
        }
    }

    private void replaceItemsList(CompoundTag blockEntity) {
        if (!blockEntity.containsKey("Items")) return
        def items = blockEntity.getListTag("Items")
        if (items == null) return
        for (int j = 0; j < items.size(); j++) {
            def stack = items.get(j) as CompoundTag
            replaceItemStackId(stack)
        }
    }

    private void replaceItemStackId(CompoundTag stack) {
        // Structure-NBT flat form: id at stack root.
        if (stack.containsKey("id")) {
            def idTag = stack.get("id")
            if (idTag instanceof StringTag) {
                def id = (idTag as StringTag).getValue()
                def replacement = blockMappings.get(id)
                if (replacement != null) {
                    stack.put("id", new StringTag(replacement))
                }
            }
        }
        // Data Components form: item.id nested under stack.item.
        if (stack.containsKey("item")) {
            def inner = stack.get("item")
            if (inner instanceof CompoundTag) {
                def item = inner as CompoundTag
                if (item.containsKey("id")) {
                    def id = item.getString("id")
                    def replacement = blockMappings.get(id)
                    if (replacement != null) {
                        item.put("id", new StringTag(replacement))
                    }
                }
            }
        }
    }

    /**
     * Serialize a NamedTag to gzip-compressed bytes.
     * Matches the pattern used by DefaultNbtConverter in minecraft-mod-gradle-scripts.
     */
    private static byte[] serializeToBytes(NamedTag namedTag) {
        def bos = new ByteArrayOutputStream()
        new NBTSerializer(true).toStream(namedTag, bos)
        return bos.toByteArray()
    }
}
