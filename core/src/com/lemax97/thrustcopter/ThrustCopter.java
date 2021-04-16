package com.lemax97.thrustcopter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import matsemann.LoadingScreen;


public class ThrustCopter extends Game {

    public SpriteBatch batch;
    public TextureAtlas atlas;
    public BitmapFont font;
    public OrthographicCamera camera;
    public FPSLogger fpsLogger;
    public Viewport viewport;
    public AssetManager manager = new AssetManager();

    public static final int screenWidth = 800;
    public static final int screenHeight = 480;

    public ThrustCopter() {
        fpsLogger = new FPSLogger();

        camera = new OrthographicCamera();
        camera.position.set(400, 240, 0);
        viewport = new FitViewport(800, 480, camera);

    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new LoadingScreen(this));
    }

    @Override
    public void render() {
        fpsLogger.log();
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        atlas.dispose();
        batch.dispose();
        manager.dispose();
    }
}
