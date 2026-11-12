package com.dokja.voxel;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import java.util.*;

/**
 * Starter 3D voxel sandbox.
 * Controls: WASD / arrows, Space = jump, K = keybind settings.
 * Touch: left-bottom virtual joystick, right-bottom jump button.
 */
public class VoxelSandbox extends ApplicationAdapter implements InputProcessor {
    private PerspectiveCamera camera;
    private ModelBatch batch;
    private ModelBuilder modelBuilder;
    private Environment environment;
    private Model cubeModel, playerModel;
    private final Array<ModelInstance> blocks = new Array<>();
    private ModelInstance player;
    private ShapeRenderer shapes;
    private SpriteBatch uiBatch;
    private BitmapFont font;

    private final Vector3 playerPos = new Vector3(0, 1, 0);
    private final Vector3 move = new Vector3();
    private float verticalVelocity = 0;
    private boolean grounded = true;
    private float joyX, joyY;
    private boolean joystickActive, jumpPressed;
    private int joyPointer = -1;
    private boolean settingsOpen, waitingForBind;
    private String bindingAction = "";
    private final LinkedHashMap<String, Integer> keybinds = new LinkedHashMap<>();
    private final String[] actions = {"Forward", "Backward", "Left", "Right", "Jump", "Settings"};

    private float joyCenterX, joyCenterY, joyRadius;
    private float jumpX, jumpY, jumpSize;

    @Override public void create() {
        batch = new ModelBatch();
        modelBuilder = new ModelBuilder();
        shapes = new ShapeRenderer();
        uiBatch = new SpriteBatch();
        font = new BitmapFont();

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.1f;
        camera.far = 120f;

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.72f, 0.72f, 0.78f, 1f));
        environment.add(new DirectionalLight().set(0.85f, 0.82f, 0.75f, -1f, -0.8f, -0.3f));

        cubeModel = modelBuilder.createBox(1f, 1f, 1f,
            new Material(ColorAttribute.createDiffuse(new Color(0.34f, 0.68f, 0.32f, 1))),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        playerModel = modelBuilder.createBox(0.65f, 1.8f, 0.65f,
            new Material(ColorAttribute.createDiffuse(new Color(0.25f, 0.55f, 0.95f, 1))),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // Flat starter terrain.
        for (int x = -12; x <= 12; x++) {
            for (int z = -12; z <= 12; z++) {
                ModelInstance block = new ModelInstance(cubeModel, x, -0.5f, z);
                blocks.add(block);
            }
        }
        player = new ModelInstance(playerModel);
        setDefaultKeybinds();
        Gdx.input.setInputProcessor(this);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void setDefaultKeybinds() {
        keybinds.put("Forward", Input.Keys.W);
        keybinds.put("Backward", Input.Keys.S);
        keybinds.put("Left", Input.Keys.A);
        keybinds.put("Right", Input.Keys.D);
        keybinds.put("Jump", Input.Keys.SPACE);
        keybinds.put("Settings", Input.Keys.K);
    }

    @Override public void render() {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);
        updatePlayer(dt);
        updateCamera();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.48f, 0.70f, 0.88f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        batch.begin(camera);
        for (ModelInstance block : blocks) batch.render(block, environment);
        player.transform.setToTranslation(playerPos);
        batch.render(player, environment);
        batch.end();

        drawTouchUI();
    }

    private void updatePlayer(float dt) {
        if (!settingsOpen) {
            float x = joyX, z = -joyY;
            if (isDown("Forward")) z += 1;
            if (isDown("Backward")) z -= 1;
            if (isDown("Left")) x -= 1;
            if (isDown("Right")) x += 1;
            Vector3 direction = new Vector3(x, 0, z);
            if (direction.len2() > 1) direction.nor();
            playerPos.x += direction.x * 5f * dt;
            playerPos.z += direction.z * 5f * dt;

            if ((jumpPressed || isDown("Jump")) && grounded) {
                verticalVelocity = 7.2f;
                grounded = false;
            }
        }
        jumpPressed = false;
        verticalVelocity -= 18f * dt;
        playerPos.y += verticalVelocity * dt;
        if (playerPos.y <= 0.9f) {
            playerPos.y = 0.9f;
            verticalVelocity = 0;
            grounded = true;
        }
    }

    private boolean isDown(String action) {
        Integer key = keybinds.get(action);
        return key != null && Gdx.input.isKeyPressed(key);
    }

    private void updateCamera() {
        camera.position.set(playerPos.x + 7f, playerPos.y + 6f, playerPos.z + 9f);
        camera.lookAt(playerPos.x, playerPos.y, playerPos.z);
        camera.up.set(Vector3.Y);
        camera.update();
    }

    private void drawTouchUI() {
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        joyCenterX = w * 0.16f; joyCenterY = h * 0.19f; joyRadius = Math.min(w, h) * 0.105f;
        jumpX = w * 0.86f; jumpY = h * 0.18f; jumpSize = Math.min(w, h) * 0.075f;

        shapes.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, w, h));
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.28f);
        shapes.circle(joyCenterX, joyCenterY, joyRadius);
        shapes.setColor(0.85f, 0.9f, 1f, 0.65f);
        shapes.circle(joyCenterX + joyX * joyRadius, joyCenterY + joyY * joyRadius, joyRadius * 0.36f);
        shapes.setColor(0.12f, 0.35f, 0.85f, 0.65f);
        shapes.circle(jumpX, jumpY, jumpSize);
        shapes.end();

        font.getData().setScale(Math.max(1f, w / 900f));
        uiBatch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, w, h));
        uiBatch.begin();
        font.setColor(Color.WHITE);
        font.draw(uiBatch, "JUMP", jumpX - jumpSize * 0.45f, jumpY + 5);
        font.draw(uiBatch, "K: Settings", 16, h - 18);
        if (settingsOpen) {
            font.draw(uiBatch, "KEYBIND SETTINGS - tap an action, then press a key", w * 0.18f, h * 0.82f);
            int i = 0;
            for (String action : actions) {
                int key = keybinds.get(action);
                font.draw(uiBatch, action + " : " + Input.Keys.toString(key) +
                    (waitingForBind && bindingAction.equals(action) ? "  < PRESS KEY >" : ""),
                    w * 0.25f, h * (0.72f - i * 0.075f));
                i++;
            }
            font.draw(uiBatch, "Tap SETTINGS again or press ESC to close", w * 0.2f, h * 0.17f);
        }
        uiBatch.end();
    }

    @Override public boolean keyDown(int keycode) {
        if (waitingForBind) {
            keybinds.put(bindingAction, keycode);
            waitingForBind = false;
            bindingAction = "";
            return true;
        }
        if (keycode == Input.Keys.ESCAPE && settingsOpen) { settingsOpen = false; return true; }
        if (keycode == keybinds.get("Settings")) { settingsOpen = !settingsOpen; return true; }
        return false;
    }

    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float y = Gdx.graphics.getHeight() - screenY;
        float x = screenX;
        if (settingsOpen) {
            float startY = Gdx.graphics.getHeight() * 0.72f;
            for (int i = 0; i < actions.length; i++) {
                float rowY = startY - i * Gdx.graphics.getHeight() * 0.075f;
                if (x > Gdx.graphics.getWidth() * 0.18f && x < Gdx.graphics.getWidth() * 0.85f
                    && Math.abs(y - rowY) < Gdx.graphics.getHeight() * 0.04f) {
                    bindingAction = actions[i];
                    waitingForBind = true;
                    return true;
                }
            }
            settingsOpen = false;
            return true;
        }
        if (Math.abs(x - jumpX) < jumpSize * 1.5f && Math.abs(y - jumpY) < jumpSize * 1.5f) {
            jumpPressed = true;
            return true;
        }
        if (x < Gdx.graphics.getWidth() * 0.38f && y < Gdx.graphics.getHeight() * 0.42f) {
            joyPointer = pointer;
            joystickActive = true;
            updateJoystick(x, y);
            return true;
        }
        return false;
    }

    @Override public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer == joyPointer && joystickActive) {
            updateJoystick(screenX, Gdx.graphics.getHeight() - screenY);
            return true;
        }
        return false;
    }

    private void updateJoystick(float x, float y) {
        float dx = (x - joyCenterX) / joyRadius;
        float dy = (y - joyCenterY) / joyRadius;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len > 1) { dx /= len; dy /= len; }
        joyX = dx; joyY = dy;
    }

    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer == joyPointer) {
            joyPointer = -1; joystickActive = false; joyX = joyY = 0;
            return true;
        }
        return false;
    }

    @Override public void resize(int width, int height) {
        if (camera != null) { camera.viewportWidth = width; camera.viewportHeight = height; camera.update(); }
    }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }

    @Override public void dispose() {
        batch.dispose(); uiBatch.dispose(); shapes.dispose(); font.dispose();
        cubeModel.dispose(); playerModel.dispose();
    }
}
