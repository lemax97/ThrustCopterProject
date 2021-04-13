package com.lemax97.thrustcopter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ThrustCopter extends ApplicationAdapter {
	float terrainOffset, planeAnimTime, tapDrawTime, orientation, accelX, accelY, accelZ, deltaPosition, nextMeteorIn;
	boolean leftPressed, rightPressed, middlePressed, isSpacePressed, meteorInScene;
	SpriteBatch batch;
	TextureAtlas atlas;
	Texture gameOver;
	TextureRegion bgRegion, terrainBelow, terrainAbove, tapIndicator, tap1, pillarUp, pillarDown, selectedMeteorTexture;
	Animation plane;
	Array<TextureAtlas.AtlasRegion> meteorTextures;
	OrthographicCamera camera;
	FPSLogger fpsLogger;
	Viewport viewport;
	Vector2 planeVelocity, scrollVelocity, planePosition, planeDefaultPosition,
			gravity, tmpVector, lastPillarPosition, meteorPosition, meteorVelocity;
	Array<Vector2> pillars;
	Rectangle planeRect, obstacleRect;
	Vector3 touchPosition;
	private static final int TOUCH_IMPULSE=500;
	private static final float TAP_DRAW_TIME_MAX=1.0f;
	private static final Vector2 damping = new Vector2(0.99f, 0.99f);
	private static final int METEOR_SPEED = 60;

	static enum GameState{
		INIT, ACTION, GAME_OVER
	}

	GameState gameState;

	private void addPillar(){
		Vector2 pillarPosition = new Vector2();
		if (pillars.size == 0){
			pillarPosition.x = (float) (800 + MathUtils.random() * 600);
		}
		else {
			pillarPosition.x = lastPillarPosition.x + (float) (600 + MathUtils.random() * 600);
		}
		if (MathUtils.randomBoolean()){
			pillarPosition.y = 1;
		}
		else {
			pillarPosition.y = -1;//upside down
		}
		lastPillarPosition = pillarPosition;
		pillars.add(pillarPosition);
	}

	private void launchMeteor(){
		nextMeteorIn = 1.5f + (float) MathUtils.random() * 5;
		if (meteorInScene){
			return;
		}
		meteorInScene = true;
		int id = (int) (MathUtils.random() * meteorTextures.size);
		selectedMeteorTexture = meteorTextures.get(id);
		meteorPosition.x = 810;
		meteorPosition.y = (float) (80 + MathUtils.random() * 320);
		Vector2 destination = new Vector2();
		destination.x -= 10;
		destination.y = (float) (80 + MathUtils.random() * 320);
		destination.sub(meteorPosition).nor();
		meteorVelocity.mulAdd(destination, METEOR_SPEED);
	}

	@Override
	public void create ()
	{
		tmpVector = new Vector2();
		planeVelocity = new Vector2();
		planePosition = new Vector2();
		planeDefaultPosition = new Vector2();
		gravity = new Vector2();
		scrollVelocity = new Vector2();
		lastPillarPosition = new Vector2();
		pillars = new Array<Vector2>();
		planeRect = new Rectangle();
		meteorTextures = new Array<TextureAtlas.AtlasRegion>();
		selectedMeteorTexture = new TextureRegion();
		meteorPosition = new Vector2();
		meteorVelocity = new Vector2();
		obstacleRect = new Rectangle();
		touchPosition = new Vector3();
		fpsLogger = new FPSLogger();
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
//		camera.setToOrtho(false, 800, 480);
		camera.position.set(400, 240, 0);
		viewport = new FitViewport(800, 480, camera);
		atlas = new TextureAtlas(Gdx.files.internal("thrustcopterassets.txt"));
		tapIndicator = atlas.findRegion("tap2");
		tap1 = atlas.findRegion("tap1");
		pillarUp = atlas.findRegion("rockGrassUp");
		pillarDown = atlas.findRegion("rockGrassDown");
		gameOver = new Texture("gameover.png");
		bgRegion = atlas.findRegion("background");
		meteorTextures.add(atlas.findRegion("meteorBrown_med1"));
		meteorTextures.add(atlas.findRegion("meteorBrown_med2"));
		meteorTextures.add(atlas.findRegion("meteorBrown_small1"));
		meteorTextures.add(atlas.findRegion("meteorBrown_small2"));
		meteorTextures.add(atlas.findRegion("meteorBrown_tiny1"));
		meteorTextures.add(atlas.findRegion("meteorBrown_tiny2"));
		terrainBelow = atlas.findRegion("groundGrass");
		terrainAbove = new TextureRegion(terrainBelow);
		terrainAbove.flip(true, true);

		plane = new Animation(0.05f,
				atlas.findRegion("planeRed1"),
				atlas.findRegion("planeRed2"),
				atlas.findRegion("planeRed3"),
				atlas.findRegion("planeRed2"));
		plane.setPlayMode(PlayMode.LOOP);

		gameState = GameState.INIT;

		resetScene();

	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		fpsLogger.log();
		updateScene();
		drawScene();
	}

	private void updateScene() {
//		orientation = Gdx.input.getRotation();
//		accelX = Gdx.input.getAccelerometerX();
//		accelY = Gdx.input.getAccelerometerY();
//		accelZ = Gdx.input.getAccelerometerZ();
//		leftPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
//		rightPressed = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
//		middlePressed = Gdx.input.isButtonPressed(Input.Buttons.MIDDLE);

		if (Gdx.input.justTouched()){
			if (gameState == GameState.INIT){
				gameState = GameState.ACTION;
				return;
			}
			if (gameState == GameState.GAME_OVER){
				gameState = GameState.INIT;
				resetScene();
				return;
			}
			touchPosition.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPosition);
			tmpVector.set(planePosition.x, planePosition.y);
			tmpVector.sub(touchPosition.x, touchPosition.y).nor();
			planeVelocity.mulAdd(tmpVector,
					TOUCH_IMPULSE - MathUtils.clamp(Vector2.dst(touchPosition.x, touchPosition.y,
					planePosition.x, planePosition.y), 0, TOUCH_IMPULSE));
			tapDrawTime=TAP_DRAW_TIME_MAX;
		}
		if (gameState == GameState.INIT || gameState == GameState.GAME_OVER) {
			return;
		}
		float deltaTime = Gdx.graphics.getDeltaTime();
		tapDrawTime -= deltaTime;
		planeAnimTime += deltaTime;
		planeVelocity.scl(damping);
		planeVelocity.add(gravity);
		planeVelocity.add(scrollVelocity);
		planePosition.mulAdd(planeVelocity, deltaTime);
		deltaPosition = planePosition.x - planeDefaultPosition.x;
		terrainOffset -= deltaPosition;
		planePosition.x = planeDefaultPosition.x;
		if (terrainOffset *-1 > terrainBelow.getRegionWidth()){
			terrainOffset = 0;
		}
		if (terrainOffset > 0){
			terrainOffset = -terrainBelow.getRegionWidth();
		}
		if (planePosition.y < terrainBelow.getRegionHeight() - 35 ||
				planePosition.y + 73 > 480 - terrainBelow.getRegionHeight() + 35){
			if (gameState != GameState.GAME_OVER){
				gameState = GameState.GAME_OVER;
			}
		}

		planeRect.set(planePosition.x + 16, planePosition.y, 50, 73);

		for (Vector2 vec: pillars){
			vec.x -= deltaPosition;
			if (vec.x + pillarUp.getRegionWidth()<-10)
			{
				pillars.removeValue(vec, false);
			}
			if (vec.y == 1)
			{
				obstacleRect.set(vec.x + 10, 0, pillarUp.getRegionWidth() - 20,
						pillarUp.getRegionHeight() - 10);
			}
			else
			{
				obstacleRect.set(vec.x + 10, 480 - pillarDown.getRegionHeight() + 10,
						pillarDown.getRegionWidth() - 20, pillarDown.getRegionHeight());
			}
			if (planeRect.overlaps(obstacleRect))
			{
				if (gameState != GameState.GAME_OVER){
					gameState = GameState.GAME_OVER;
				}
			}
		}
		if (lastPillarPosition.x < 400){
			addPillar();
		}
		if (meteorInScene){
			meteorPosition.mulAdd(meteorVelocity, deltaTime);
			meteorPosition.x -= deltaPosition;
			if (meteorPosition.x < -10){
				meteorInScene = false;
			}
			obstacleRect.set(meteorPosition.x + 2, meteorPosition.y + 2,
					selectedMeteorTexture.getRegionWidth() - 4,
					selectedMeteorTexture.getRegionHeight() - 4);
			if (planeRect.overlaps(obstacleRect)){
				if (gameState != GameState.GAME_OVER){
					gameState = GameState.GAME_OVER;
				}
			}
		}
		nextMeteorIn -= deltaTime;
		if (nextMeteorIn <= 0){
			launchMeteor();
		}
	}

	private void drawScene()
	{
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.disableBlending();
		batch.draw(bgRegion, 0, 0);
		batch.enableBlending();
		for (Vector2 vec: pillars){
			if (vec.y == 1){
				batch.draw(pillarUp, vec.x, 0);
			}
			else {
				batch.draw(pillarDown, vec.x, 480 - pillarDown.getRegionHeight());
			}
		}
		batch.draw(terrainBelow, terrainOffset, 0);
		batch.draw(terrainBelow, terrainOffset + terrainBelow.getRegionWidth(), 0);
		batch.draw(terrainAbove, terrainOffset, 480 - terrainAbove.getRegionHeight());
		batch.draw(terrainAbove, terrainOffset + terrainAbove.getRegionWidth(),
				480 - terrainAbove.getRegionHeight());
		if (meteorInScene){
			batch.draw(selectedMeteorTexture, meteorPosition.x, meteorPosition.y);
		}
		batch.draw((TextureRegion) plane.getKeyFrame(planeAnimTime), planePosition.x,
				planePosition.y);
		if (tapDrawTime>0){
			batch.draw(tapIndicator, touchPosition.x - 29.5f, touchPosition.y - 29.5f);
			//29.5 is half width/height of the image
		}
		if (gameState == GameState.INIT){
			batch.draw(tap1, planePosition.x, planePosition.y - 80);
		}
		if (gameState == GameState.GAME_OVER){
			batch.draw(gameOver, 400 - 206, 240 - 80);
		}
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void dispose () {
		batch.dispose();
		gameOver.dispose();
		atlas.dispose();
		pillars.clear();
	}

	private void resetScene()
	{
		terrainOffset = 0;
		planeAnimTime = 0;
		scrollVelocity.set(4, 0);
		planeVelocity.set(100, 0);
		gravity.set(0, -3);
		planeDefaultPosition.set(250 - 88/2, 240 - 73/2);
		planePosition.set(planeDefaultPosition.x
				, planeDefaultPosition.y);
		pillars.clear();
		addPillar();
		meteorInScene = false;
		nextMeteorIn = (float) MathUtils.random() * 5;
	}
}
