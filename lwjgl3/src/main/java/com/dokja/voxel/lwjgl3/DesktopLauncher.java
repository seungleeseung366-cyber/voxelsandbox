package com.dokja.voxel.lwjgl3;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.dokja.voxel.VoxelSandbox;
public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Voxel Sandbox");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        new Lwjgl3Application(new VoxelSandbox(), config);
    }
}
