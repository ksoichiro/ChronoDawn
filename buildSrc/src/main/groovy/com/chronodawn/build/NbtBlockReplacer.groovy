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

    final Map<String, String> blockMappings

    NbtBlockReplacer(Map<String, String> blockMappings) {
        this.blockMappings = blockMappings
    }

    /**
     * Replace block names in the palette of an NBT structure file.
     *
     * @param input gzip-compressed NBT bytes
     * @return gzip-compressed NBT bytes with replaced block names
     */
    byte[] replace(byte[] input) {
        def namedTag = new NBTDeserializer(true).fromBytes(input)
        def root = namedTag.getTag() as CompoundTag

        if (root.containsKey("palette")) {
            def palette = root.getListTag("palette")
            if (palette != null) {
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
        }

        return serializeToBytes(namedTag)
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
