package com.lemax97.thrustcopter.TrdDmsn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import com.lemax97.thrustcopter.BaseScene;
import com.lemax97.thrustcopter.ThrustCopter;

public class BulletSample extends BaseScene {

    public static class MyContactListener extends ContactListener {

        @Override
        public void onContactStarted(btCollisionObject colObj0, btCollisionObject colObj1) {
            if (colObj0.userData == "plane" || colObj1.userData == "plane"){
                Gdx.app.log("ContactCallback", "Plane Collides");
            };
        }
    }

    ModelBatch modelBatch;
    Environment environment;
    PerspectiveCamera camera;
    CameraInputController cameraController;
    Array<ModelInstance> instances = new Array<ModelInstance>();
    Array<Model> models            = new Array<Model>();
    AnimationController controller;
    boolean started = false;

    private btDefaultCollisionConfiguration collisionConfiguration;
    private btCollisionDispatcher dispatcher;
    private btDbvtBroadphase broadphase;
    private btSequentialImpulseConstraintSolver solver;
    private btDiscreteDynamicsWorld world;
    private Array<btMotionState> motionStates = new Array<btMotionState>();
    private Array<btCollisionShape> shapes = new Array<btCollisionShape>();
    private Array<btRigidBody> bodies = new Array<btRigidBody>();
    ModelInstance groundInstance;
    MyContactListener contactListner;
    DirectionalShadowLight shadowLight;
    ModelBatch shadowBatch;

    public BulletSample(ThrustCopter thrustCopter) {
        super(thrustCopter);

        //Create ModelBatch that will render all models using a camera
        modelBatch = new ModelBatch(new DefaultShaderProvider());

        //Create a camera and point it to our model
        camera = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10f, 10f, 12f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far  = 300f;
        camera.update();

        //Create the generic camera input controller to make the app interactive
        cameraController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraController);

        //Set up environment with simple lightning
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
//        environment.set(new ColorAttribute(ColorAttribute.Fog, 0.13f, 0.13f, 0.13f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.8f, 0.3f, -1f));
        shadowLight = new DirectionalShadowLight(1024, 1024,
                60, 60, 1f, 300);
        shadowLight.set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f);
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;
        shadowBatch = new ModelBatch(new DepthShaderProvider());

        Bullet.init();
        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher             = new btCollisionDispatcher(collisionConfiguration);
        broadphase             = new btDbvtBroadphase();
        solver                 = new btSequentialImpulseConstraintSolver();
        world                  = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        world.setGravity(new Vector3(0, -9.8f, 1f));
        ModelBuilder modelBuilder = new ModelBuilder();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glClearColor(0.13f, 0.13f, 0.13f, 1);

        //Respond to user events and update the camera
        cameraController.update();
        controller.update(delta);

        shadowLight.begin(Vector3.Zero, camera.direction);
        shadowBatch.begin(shadowLight.getCamera());
        shadowBatch.render(instances);

        shadowBatch.end();
        shadowLight.end();

        //Draw all model instances using the camera
        modelBatch.begin(camera);
        modelBatch.render(groundInstance, environment);
        modelBatch.render(instances, environment);
        modelBatch.end();

        super.render(delta);
    }

    @Override
    public void dispose() {
        super.dispose();
        groundInstance.model.dispose();
        instances.clear();
        modelBatch.dispose();
        for (Model model : models) model.dispose();
        for (btRigidBody body : bodies) body.dispose();
        for (btMotionState motion : motionStates) motion.dispose();
        for (btCollisionShape shape : shapes) shape.dispose();
        world.dispose();
        collisionConfiguration.dispose();
        dispatcher.dispose();
        broadphase.dispose();
        solver.dispose();
        contactListner.dispose();
        shadowBatch.dispose();
        shadowLight.dispose();
    }
}
