package com.lemax97.thrustcopter.Box2D;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.lemax97.thrustcopter.BaseScene;
import com.lemax97.thrustcopter.ThrustCopter;

public class ThrustCopterSceneBox2D extends BaseScene {
    float previousCamXPos;
    World world;
    Box2DDebugRenderer debugRenderer;
    private static final boolean DRAW_BOX2D_DEBUG = true;
    Body planeBody, terrainBodyUp, terrainBodyDown, meteorBody, lastPillarBody, bodyA, bodyB, unknownBody, hitBody;
    private static final int touchImpulse = 1000;
    OrthographicCamera box2dCam;

    Vector2 planeVelocity, scrollVelocity, planePosition, planeDefaultPosition,
            gravity, tmpVector, lastPillarPosition, meteorPosition, meteorVelocity;
    Array<Body> pickupsInScene = new Array<Body>();
    Array<Body> pillars = new Array<Body>();
    Array<Body> setForRemoval = new Array<Body>();

    private static final int BOX2D_TO_CAMERA = 100;

    Texture gameOver, fuelIndicator;
    TextureRegion bgRegion, terrainBelow, terrainAbove, tap2,
            tap1, pillarUp, pillarDown, selectedMeteorTexture;
    Animation plane;
    Animation shield;

    public ThrustCopterSceneBox2D(ThrustCopter thrustCopter) {
        super(thrustCopter);
    }

    private void initPhysics() {
        world = new World(new Vector2(5f, -8), true);
        debugRenderer = new Box2DDebugRenderer();
        box2dCam = new OrthographicCamera(8, 4.8f);
        box2dCam.position.set(4, 2.4f, 0);
        previousCamXPos = 4;
        planeBody = createPhysicsObjectFromGraphics((TextureRegion) plane.getKeyFrame(0),
                planePosition, BodyType.DynamicBody);
        terrainBodyUp = createPhysicsObjectFromGraphics(terrainAbove, new Vector2(terrainAbove.getRegionWidth()/2,
                480 - terrainAbove.getRegionHeight() / 2), BodyType.StaticBody);
        terrainBodyDown = createPhysicsObjectFromGraphics(terrainBelow, new Vector2(terrainBelow.getRegionWidth()/2,
               terrainBelow.getRegionHeight()/2), BodyType.StaticBody);
        meteorBody = createPhysicsObjectFromGraphics(selectedMeteorTexture,
                new Vector2(800, 500), BodyType.KinematicBody);
    }

    private Body createPhysicsObjectFromGraphics(TextureRegion region, Vector2 position, BodyType bodyType) {
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = bodyType;
        boxBodyDef.position.x = position.x / BOX2D_TO_CAMERA;
        boxBodyDef.position.y = position.y / BOX2D_TO_CAMERA;
        Body boxBody = world.createBody(boxBodyDef);
        PolygonShape boxPoly = new PolygonShape();
        boxPoly.setAsBox(region.getRegionWidth()/(2 * BOX2D_TO_CAMERA),
                region.getRegionHeight()/(2 * BOX2D_TO_CAMERA));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = boxPoly;
        fixtureDef.density = 1;
        fixtureDef.restitution = 0.2f;
        boxBody.createFixture(fixtureDef);

        boxPoly.dispose();
        boxBody.setUserData(region);
        return boxBody;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        updateSceneBox2D(delta);
        drawSceneBox2D();
        if (DRAW_BOX2D_DEBUG) {
            box2dCam.update();
            debugRenderer.render(world, box2dCam.combined);
        }
    }

    private void drawSceneBox2D() {
    }

    private void updateSceneBox2D(float delta) {
    }
}
