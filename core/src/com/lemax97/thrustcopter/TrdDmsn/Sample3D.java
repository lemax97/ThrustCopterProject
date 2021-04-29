package com.lemax97.thrustcopter.TrdDmsn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.lemax97.thrustcopter.BaseScene;
import com.lemax97.thrustcopter.ThrustCopter;

public class Sample3D extends BaseScene {

    ModelBatch modelBatch;
    Environment environment;
    PerspectiveCamera camera;
    CameraInputController cameraController;
    ModelInstance instance;
    public Array<ModelInstance> instances = new Array<ModelInstance>();
    AnimationController controller;

    public Sample3D(ThrustCopter thrustCopter) {
        super(thrustCopter);

        // Create ModelBatch that will render all models using a camera
        modelBatch = new ModelBatch(new DefaultShaderProvider());

        //Create a camera and point it to our model
        camera = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        camera.position.set(0f, 4f, 3f);
        camera.position.set(4f, 3f, 3f);
//        camera.position.set(0f, -4f, 3f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 300f;
        camera.update();

        //Create Environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.8f, 0.3f, -1f));

        //Create camera controller
        cameraController = new  CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraController);

        //Create primitives
        ModelBuilder modelBuilder = new ModelBuilder();
        /*
        Model model = modelBuilder.createBox(1f, 1f, 1f,
                new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                Usage.Position | Usage.Normal);
        instance = new ModelInstance(model);
        instances.add(instance);

        model = modelBuilder.createSphere(1f, 1f, 1f, 8, 8,
                new Material(ColorAttribute.createDiffuse(Color.RED)),
                Usage.Position | Usage.Normal);
        instance = new ModelInstance(model);
        instance.transform.setToTranslation(2, 0, 0);
        instances.add(instance);

        model = modelBuilder.createCone(1f, 1f, 1f, 6,
                new Material(ColorAttribute.createDiffuse(Color.BLUE)),
                Usage.Position | Usage.Normal);
        instance = new ModelInstance(model);
        instance.transform.setToTranslation(-2, 0, 0);
        instances.add(instance);

        model = modelBuilder.createCylinder(1f, 1.5f, 1f, 6,
                new Material(ColorAttribute.createDiffuse(Color.ORANGE)),
                Usage.Position | Usage.Normal);
        instance = new ModelInstance(model);
        instance.transform.setToTranslation(0, 0, -2);
        instances.add(instance);

        model = modelBuilder.createArrow(new Vector3(0, 0, 0), new Vector3(0, 0, 1),
                new Material(ColorAttribute.createDiffuse(Color.YELLOW)),
                Usage.Position | Usage.Normal);
        instance = new ModelInstance(model);
        instance.transform.setToTranslation(0, 0, 2);
        instances.add(instance);*/

        //Loading plane model
        game.manager.load("3D/planeanim1.g3db", Model.class);
        game.manager.finishLoading();
        Model model = game.manager.get("3D/planeanim1.g3db", Model.class);
        ModelInstance plane = new ModelInstance(model);
        plane.transform.setToTranslation(0, 0, -1f);
        instances.add(plane);

        //Create 3d animation
        controller = new AnimationController(plane);
        controller.setAnimation("Scene", -1);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(0.13f, 0.13f, 0.13f, 1);

        // Respond to user events and update the camera
        cameraController.update();
        controller.update(delta);

        // Draw all model instances using the camera
        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();
        super.render(delta);
    }

    @Override
    public void dispose() {
        for (ModelInstance mi : instances){
            mi.model.dispose();
        }
        instances.clear();
        modelBatch.dispose();
    }
}
