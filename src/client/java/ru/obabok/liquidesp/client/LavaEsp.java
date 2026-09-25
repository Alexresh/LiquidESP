package ru.obabok.liquidesp.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

public class LavaEsp {

    private static Set<BlockPos> lavaPositions = new HashSet<>();
    private static long lastScanTime = 0;

    private static final RenderPipeline BOX_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("liquidesp","pipeline/box"))
            .withDepthStencilState(Optional.empty())
            .build()
    );
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final StagedVertexBuffer stagedBuffer = new StagedVertexBuffer(() -> "Waypoints Buffer", RenderType.SMALL_BUFFER_SIZE);


    private static final LavaEspConfig cfg = LavaEspConfig.get();


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
    private static void addCubeMarker(Matrix4f pose, VertexConsumer buf, float x, float y, float z,
                                      float size, float r, float g, float b, float a) {
        float hs = size / 2.0f;
        float minX = x + 0.5f - hs;
        float maxX = x + 0.5f + hs;
        float minY = y + 0.5f - hs;
        float maxY = y + 0.5f + hs;
        float minZ = z + 0.5f - hs;
        float maxZ = z + 0.5f + hs;

        // DOWN
        buf.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a);

        // UP
        buf.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a);

        // NORTH
        buf.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a);

        // SOUTH
        buf.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a);

        // WEST
        buf.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a);

        // EAST
        buf.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a);
    }

    public static void collect(LevelExtractionContext levelExtractionContext) {
        if (!cfg.enabled){
            lavaPositions.clear();
            return;
        };
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastScanTime > cfg.updateIntervalMs) {
            scanLava(mc.level, mc.player.blockPosition(), cfg.scanRadius);
            lastScanTime = now;
        }
    }

    private static void renderCubes(LevelRenderContext levelRenderContext, StagedVertexBuffer.Draw draw){
        PoseStack matrices = levelRenderContext.poseStack();
        Vec3 camera = levelRenderContext.levelState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        final var builder = stagedBuffer.getVertexBuilder(draw);

        float r = cfg.getColorR();
        float g = cfg.getColorG();
        float b = cfg.getColorB();
        float a = cfg.getColorA();
        float size = cfg.markerSize;

        for (BlockPos pos : lavaPositions) {
//            float dx = (float)(pos.getX() - camPos.x);
//            float dy = (float)(pos.getY() - camPos.y);
//            float dz = (float)(pos.getZ() - camPos.z);

            addCubeMarker(matrices.last().pose(), builder, pos.getX(), pos.getY(), pos.getZ(), size, r, g, b, a);
        }


        //renderFilledBox(, builder, this.waypointState.x(), this.waypointState.y(), this.waypointState.z(), this.waypointState.x() + 1, this.waypointState.y() + 1, this.waypointState.z() + 1, this.waypointState.r(), this.waypointState.g(), this.waypointState.b(), this.waypointState.a());

        matrices.popPose();
    }

    public static void render(LevelRenderContext levelRenderContext) {
        if (lavaPositions.isEmpty()) return;
        VertexFormat formatBinding = BOX_PIPELINE.getVertexFormatBinding(0);

        assert formatBinding != null;

        PrimitiveTopology primitive = BOX_PIPELINE.getPrimitiveTopology();
        StagedVertexBuffer.Draw draw = stagedBuffer.appendDraw(formatBinding, primitive, primitive == PrimitiveTopology.QUADS ? RenderSystem.getProjectionType().vertexSorting() : null);

        renderCubes(levelRenderContext, draw);

        stagedBuffer.upload();

        StagedVertexBuffer.ExecuteInfo info = stagedBuffer.getExecuteInfo(draw);

        if (info != null) {
            draw(Minecraft.getInstance(), info, BOX_PIPELINE);
        }

        stagedBuffer.endFrame();



    }
    private static void draw(Minecraft client, StagedVertexBuffer.ExecuteInfo info, RenderPipeline pipeline) {
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        RenderTarget mainTarget = client.gameRenderer.mainRenderTarget();
        GpuTextureView colorTexture = mainTarget.getColorTextureView();

        assert colorTexture != null;

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "liquidesp render pipeline", colorTexture, Optional.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
            renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());

            renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }
}
