package ru.obabok.liquidesp.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public class LavaEsp {

    private static Set<BlockPos> lavaPositions = new HashSet<>();
    private static long lastScanTime = 0;

    public static void render(WorldRenderContext context) {
        LavaEspConfig cfg = LavaEspConfig.get();
        if (!cfg.enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastScanTime > cfg.updateIntervalMs) {
            scanLava(mc.level, mc.player.blockPosition(), cfg.scanRadius);
            lastScanTime = now;
        }

        if (lavaPositions.isEmpty()) return;
        Vec3 camPos = context.camera().getPosition();


        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);


        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float r = cfg.getColorR();
        float g = cfg.getColorG();
        float b = cfg.getColorB();
        float a = cfg.getColorA();
        float size = cfg.markerSize;

        for (BlockPos pos : lavaPositions) {
            float dx = (float)(pos.getX() - camPos.x);
            float dy = (float)(pos.getY() - camPos.y);
            float dz = (float)(pos.getZ() - camPos.z);
            addCubeMarker(buffer, dx, dy, dz, size, r, g, b, a);
        }

        MeshData mesh = buffer.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
    private static void scanLava(Level level, BlockPos center, int scanRadius) {
        Set<BlockPos> found = new HashSet<>();
        BlockPos.betweenClosedStream(
                center.offset(-scanRadius, -scanRadius, -scanRadius),
                center.offset(scanRadius, scanRadius, scanRadius)
        ).forEach(pos -> {
            FluidState fluid = level.getFluidState(pos);
            if (fluid.isSource() && fluid.getType().isSame(Fluids.LAVA)) {
                found.add(pos.immutable());
            }
        });
        lavaPositions = found;
    }
    private static void addCubeMarker(BufferBuilder buf, float x, float y, float z,
                                      float size, float r, float g, float b, float a) {
        float hs = size / 2.0f;
        float minX = x + 0.5f - hs;
        float maxX = x + 0.5f + hs;
        float minY = y + 0.5f - hs;
        float maxY = y + 0.5f + hs;
        float minZ = z + 0.5f - hs;
        float maxZ = z + 0.5f + hs;

        // DOWN
        buf.addVertex(minX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(maxX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(maxX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(minX, minY, maxZ).setColor(r, g, b, a);

        // UP
        buf.addVertex(minX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(minX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(maxX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(maxX, maxY, minZ).setColor(r, g, b, a);

        // NORTH
        buf.addVertex(minX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(minX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(maxX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(maxX, minY, minZ).setColor(r, g, b, a);

        // SOUTH
        buf.addVertex(minX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(maxX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(maxX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(minX, maxY, maxZ).setColor(r, g, b, a);

        // WEST
        buf.addVertex(minX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(minX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(minX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(minX, maxY, minZ).setColor(r, g, b, a);

        // EAST
        buf.addVertex(maxX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(maxX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(maxX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(maxX, minY, maxZ).setColor(r, g, b, a);
    }
}
