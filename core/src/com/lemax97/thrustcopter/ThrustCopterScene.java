package com.lemax97.thrustcopter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ThrustCopterScene extends ScreenAdapter {
	int starCount, fuelPercentage;
	float terrainOffset, planeAnimTime, tapDrawTime,  deltaPosition, nextMeteorIn, fuelCount, shieldCount, score;
	//orientation, accelX, accelY, accelZ,
	boolean leftPressed, meteorInScene;
	//rightPressed, middlePressed, isSpacePressed,

	Texture gameOver, fuelIndicator;
	TextureRegion bgRegion, terrainBelow, terrainAbove, tap2,
			tap1, pillarUp, pillarDown, selectedMeteorTexture;
	Animation plane;
	Animation shield;
	Array<TextureAtlas.AtlasRegion> meteorTextures;

	Batch batch;
	Camera camera;

	TextureAtlas atlas;
	BitmapFont font;

	ThrustCopter game;

	Vector2 planeVelocity, scrollVelocity, planePosition, planeDefaultPosition,
			gravity, tmpVector, lastPillarPosition, meteorPosition, meteorVelocity;
	Array<Vector2> pillars;
	Array<Pickup> pickupsInScene;
	Rectangle planeRect, obstacleRect;
	Vector3 touchPosition, pickupTiming;
	Pickup tempPickup;
	ParticleEffect smoke, explosion;
	Music music;
	Sound tapSound, crashSound, spawnSound;

	private static final int TOUCH_IMPULSE=500;
	private static final float TAP_DRAW_TIME_MAX=1.0f;
	private static final Vector2 damping = new Vector2(0.99f, 0.99f);
	private static final int METEOR_SPEED = 60;
	private static final int MAX_FUEL = 114;

	public ThrustCopterScene(ThrustCopter thrustCopter) {
		game = thrustCopter;
		batch = game.batch;
		atlas = game.atlas;
		font = game.font;

		camera = game.camera;
		tmpVector = new Vector2();
		planeVelocity = new Vector2();
		planePosition = new Vector2();
		planeDefaultPosition = new Vector2();
		gravity = new Vector2();
		scrollVelocity = new Vector2();
		lastPillarPosition = new Vector2();
		pillars = new Array<Vector2>();
		planeRect = new Rectangle();
		obstacleRect = new Rectangle();
		meteorTextures = new Array<TextureAtlas.AtlasRegion>();
		selectedMeteorTexture = new TextureRegion();
		meteorPosition = new Vector2();
		meteorVelocity = new Vector2();
		touchPosition = new Vector3();
		pickupTiming = new Vector3();
		pickupsInScene = new Array<Pickup>();
//		camera.setToOrtho(false, 800, 480);



		smoke = game.manager.get("SmokeM", ParticleEffect.class);
		explosion = game.manager.get("Explosion", ParticleEffect.class);

		tap2 = game.atlas.findRegion("tap2");
		tap1 = game.atlas.findRegion("tap1");
		pillarUp = game.atlas.findRegion("rockGrassUp");
		pillarDown = game.atlas.findRegion("rockGrassDown");
		gameOver = game.manager.get("gameover.png", Texture.class);
		fuelIndicator = game.manager.get("life.png", Texture.class);
		bgRegion = game.atlas.findRegion("background");
		meteorTextures.add(game.atlas.findRegion("meteorBrown_med1"));
		meteorTextures.add(game.atlas.findRegion("meteorBrown_med2"));
		meteorTextures.add(game.atlas.findRegion("meteorBrown_small1"));
		meteorTextures.add(game.atlas.findRegion("meteorBrown_small2"));
		meteorTextures.add(game.atlas.findRegion("meteorBrown_tiny1"));
		meteorTextures.add(game.atlas.findRegion("meteorBrown_tiny2"));
		terrainBelow = game.atlas.findRegion("groundGrass");
		terrainAbove = new TextureRegion(terrainBelow);
		terrainAbove.flip(true, true);

		plane = new Animation(0.05f,
				game.atlas.findRegion("planeRed1"),
				game.atlas.findRegion("planeRed2"),
				game.atlas.findRegion("planeRed3"),
				game.atlas.findRegion("planeRed2"));
		plane.setPlayMode(PlayMode.LOOP);

		shield = new Animation(0.1f,
				game.atlas.findRegion("shield1"),
				game.atlas.findRegion("shield2"),
				game.atlas.findRegion("shield3"),
				game.atlas.findRegion("shield2"));
		plane.setPlayMode(PlayMode.LOOP);

		music = game.manager.get("sounds/BMPMus1.mp3", Music.class);
		music.setLooping(true);
		music.setVolume(0.2f);
		music.play();
		tapSound =game.manager.get("sounds/pop.ogg", Sound.class);
		crashSound = game.manager.get("sounds/crash.ogg", Sound.class);
		spawnSound = game.manager.get("sounds/alarm.ogg", Sound.class);
		gameState = GameState.INIT;
		resetScene();
	}

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
		spawnSound.play();
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

	private void checkAndCreatePickup(float delta)
	{
		pickupTiming.sub(delta);
		if (pickupTiming.x <= 0){
			pickupTiming.x = (float) (0.5 + MathUtils.random() * 0.5);
			if (addPickup(Pickup.STAR))
				pickupTiming.x = 1 + (float) MathUtils.random() * 2;
		}
		if (pickupTiming.y <= 0){
			pickupTiming.y = (float) (0.5 + MathUtils.random() * 0.5);
			if (addPickup(Pickup.FUEL))
				pickupTiming.y = 3 + (float) MathUtils.random() * 2;
		}
		if (pickupTiming.z <= 0){
			pickupTiming.z = (float) (0.5 + MathUtils.random() * 0.5);
			if (addPickup(Pickup.SHIELD))
				pickupTiming.z = 10 + (float) MathUtils.random() * 3;
		}
	}

	private boolean addPickup(int pickType)
	{
		Vector2 randomPosition = new Vector2();
		randomPosition.x = 820;
		randomPosition.y = (float) (80 + MathUtils.random() * 320);
		for (Vector2 vec: pillars)
		{
			if (vec.y == 1)
			{
				obstacleRect.set(vec.x, 0, pillarUp.getRegionWidth(), pillarUp.getRegionHeight());
			}
			else
			{
				obstacleRect.set(vec.x, 480 - pillarDown.getRegionHeight(),
						pillarUp.getRegionWidth(), pillarUp.getRegionHeight());
			}
			if (obstacleRect.contains(randomPosition)){
				return false;
			}
		}
		tempPickup = new Pickup(pickType, game.manager);
		tempPickup.pickupPosition.set(randomPosition);
		pickupsInScene.add(tempPickup);
		return true;
	}

	private void pickIt(Pickup pickup)
	{
		pickup.pickupSound.play();
		switch (pickup.pickupType){
			case Pickup.STAR:
				starCount += pickup.pickupValue;
				break;
			case Pickup.SHIELD:
				shieldCount = pickup.pickupValue;
				break;
			case Pickup.FUEL:
				fuelCount += pickup.pickupValue;
				if (fuelCount > MAX_FUEL) fuelCount = MAX_FUEL;
				break;
		}
		pickupsInScene.removeValue(pickup, false);
	}

	@Override
	public void render (float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		updateScene(delta);
		drawScene();
	}

	private void updateScene(float deltaTime) {
//		orientation = Gdx.input.getRotation();
//		accelX = Gdx.input.getAccelerometerX();
//		accelY = Gdx.input.getAccelerometerY();
//		accelZ = Gdx.input.getAccelerometerZ();
//		leftPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
//		rightPressed = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
//		middlePressed = Gdx.input.isButtonPressed(Input.Buttons.MIDDLE);

		if (Gdx.input.justTouched()){
			tapSound.play();
			if (gameState == GameState.INIT){
				gameState = GameState.ACTION;
				return;
			}
			if (gameState == GameState.GAME_OVER){
				gameState = GameState.INIT;
				resetScene();
				return;
			}
			if (fuelCount > 0) {
				touchPosition.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				camera.unproject(touchPosition);
				tmpVector.set(planePosition.x, planePosition.y);
				tmpVector.sub(touchPosition.x, touchPosition.y).nor();
				planeVelocity.mulAdd(tmpVector,
						TOUCH_IMPULSE - MathUtils.clamp(Vector2.dst(touchPosition.x, touchPosition.y,
								planePosition.x, planePosition.y), 0, TOUCH_IMPULSE));
				tapDrawTime = TAP_DRAW_TIME_MAX;
			}
		}

		smoke.setPosition(planePosition.x + 20, planePosition.y + 30);
		smoke.update(deltaTime);

		if (gameState == GameState.INIT || gameState == GameState.GAME_OVER) {
			if (gameState == GameState.GAME_OVER) explosion.update(deltaTime);
			return;
		}

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

		planeRect.set(planePosition.x + 16, planePosition.y, 50, 73);

		//meteor collision checker
		if (meteorInScene){
			meteorPosition.mulAdd(meteorVelocity, deltaTime);
			meteorPosition.x -= deltaPosition;
			if (meteorPosition.x < -10){
				meteorInScene = false;
			}
			if (shieldCount < 0) {
				obstacleRect.set(meteorPosition.x + 2, meteorPosition.y + 2,
						selectedMeteorTexture.getRegionWidth() - 4,
						selectedMeteorTexture.getRegionHeight() - 4);
				if (planeRect.overlaps(obstacleRect)) {
					endGame();
				}
			}
		}

		//pillars collision checker
		for (Vector2 vec: pillars){
			vec.x -= deltaPosition;
			if (vec.x + pillarUp.getRegionWidth()<-10)
			{
				pillars.removeValue(vec, false);
			}

			if (shieldCount <= 0) {
				if (vec.y == 1) {
							obstacleRect.set(vec.x + 10, 0, pillarUp.getRegionWidth() - 20,
									pillarUp.getRegionHeight() - 10);

				} else {
							obstacleRect.set(vec.x + 10, 480 - pillarDown.getRegionHeight() + 10,
									pillarDown.getRegionWidth() - 20, pillarDown.getRegionHeight());

				}
				if (planeRect.overlaps(obstacleRect)) {
					endGame();
				}
			}
		}

		// pickUp's collisions checker
		for (Pickup pickup: pickupsInScene){
			pickup.pickupPosition.x -= deltaPosition;
			if (pickup.pickupPosition.x + pickup.pickupTexture.getRegionWidth()< -10)
				pickupsInScene.removeValue(pickup, false);
			obstacleRect.set(pickup.pickupPosition.x, pickup.pickupPosition.y,
					pickup.pickupTexture.getRegionWidth(), pickup.pickupTexture.getRegionHeight());
			if (planeRect.overlaps(obstacleRect))
				pickIt(pickup);
		}

		//terrain collisions checker
		if (planePosition.y < terrainBelow.getRegionHeight() - 35 ||
				planePosition.y + 73 > 480 - terrainBelow.getRegionHeight() + 35)
		{
			endGame();
		}

		tapDrawTime -= deltaTime;
		nextMeteorIn -= deltaTime;

		checkAndCreatePickup(deltaTime);

		if (lastPillarPosition.x < 400){
			addPillar();
		}

		if (nextMeteorIn <= 0){
			launchMeteor();
		}

		fuelPercentage = (int) (fuelCount * 100 / MAX_FUEL );
		fuelCount -= 6 * deltaTime;

		shieldCount -= deltaTime;
		score += deltaTime;
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

		if (tapDrawTime>0){
			batch.draw(tap2, touchPosition.x - 29.5f, touchPosition.y - 29.5f);
			//29.5 is half width/height of the image
		}
		if (gameState == GameState.INIT){
			batch.draw(tap1, planePosition.x, planePosition.y - 80);
		}
		if (gameState == GameState.GAME_OVER){
			batch.draw(gameOver, 400 - 206, 240 - 80);
		}
		for (Pickup pickup: pickupsInScene){
			batch.draw(pickup.pickupTexture, pickup.pickupPosition.x, pickup.pickupPosition.y);
		}

		batch.draw((TextureRegion) plane.getKeyFrame(planeAnimTime), planePosition.x,
				planePosition.y);
		if (gameState == GameState.ACTION) smoke.draw(batch);

		if (shieldCount > 0){
			game.font.draw(batch, "" + ((int) shieldCount), 390, 450);
			batch.draw((TextureRegion) shield.getKeyFrame(planeAnimTime), planePosition.x - 20, planePosition.y);
		}

		if (meteorInScene){
			batch.draw(selectedMeteorTexture, meteorPosition.x, meteorPosition.y);
		}

		batch.setColor(Color.BLACK);
		batch.draw(fuelIndicator, 10, 350);
		batch.setColor(Color.WHITE);
		batch.draw(fuelIndicator, 10, 350, 0, 0,
				fuelIndicator.getWidth() * fuelPercentage/100, 119);

		game.font.draw(batch, "" + (int) (starCount + score), 700, 450);
		if (gameState == GameState.GAME_OVER) explosion.draw(batch);
		batch.end();
	}


	@Override
	public void dispose () {
		game.dispose();
		music.dispose();
		crashSound.dispose();
		spawnSound.dispose();
		tapSound.dispose();
		gameOver.dispose();
		pillars.clear();
		meteorTextures.clear();
		smoke.dispose();
		explosion.dispose();
	}

	private void resetScene()
	{
		meteorInScene = false;
		nextMeteorIn = (float) MathUtils.random() * 5;
		pickupTiming.x = 1 + (float) MathUtils.random() * 2;
		pickupTiming.x = 3 + (float) MathUtils.random() * 2;
		pickupTiming.x = 1 + (float) MathUtils.random() * 3;
		terrainOffset = 0;
		planeAnimTime = 0;
		tapDrawTime = 0;
		starCount = 0;
		score = 0;
		shieldCount = 15;
		fuelCount = MAX_FUEL;
		fuelPercentage = 100;
		planeVelocity.set(100, 0);
		scrollVelocity.set(5, 0);
		gravity.set(0, -3);
		planeDefaultPosition.set(250 - 88/2, 240 - 73/2);
		obstacleRect.set(0,0,0,0);
		planePosition.set(planeDefaultPosition.x
				, planeDefaultPosition.y);
		pillars.clear();
		pickupsInScene.clear();
		addPillar();
		smoke.setPosition(planePosition.x + 20, planePosition.y + 30);
	}

	private void endGame()
	{
		if (gameState != GameState.GAME_OVER)
		{
			fuelCount = 0;
			crashSound.play();
			gameState = GameState.GAME_OVER;
			explosion.reset();
			explosion.setPosition(planePosition.x + 40, planePosition.y + 40);
		}
	}
}
