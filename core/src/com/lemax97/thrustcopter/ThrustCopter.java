package com.lemax97.thrustcopter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;


public class ThrustCopter extends Game {

    SpriteBatch batch;
    TextureAtlas atlas;
    BitmapFont font;
    OrthographicCamera camera;
    FPSLogger fpsLogger;
    Viewport viewport;
    AssetManager manager = new AssetManager();

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
        setScreen(new ThrustCopterScene(this));
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
        batch.dispose();
        atlas.dispose();
        manager.dispose();
    }
}
