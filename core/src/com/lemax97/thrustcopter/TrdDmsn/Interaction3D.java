package com.lemax97.thrustcopter.TrdDmsn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.lemax97.thrustcopter.BaseScene;
import com.lemax97.thrustcopter.ThrustCopter;

public class Interaction3D extends BaseScene {

    ModelBatch modelBatch;
    Environment environment;
    PerspectiveCamera camera;
    CameraInputController cameraController;
    public Array<ModelInstance> instances = new Array<ModelInstance>();
    Vector3 position = new Vector3();
    Vector3 center = new Vector3();
    Vector3 dimensions = new Vector3();
    BoundingBox bounds = new BoundingBox();
    float radius;
    int visibleCount, selected = -1, selecting = -1;
    ModelInstance selectedInstance;

    public Interaction3D(ThrustCopter thrustCopter) {
        super(thrustCopter);

        // Create ModelBatch that will render all models using a camera
        modelBatch = new ModelBatch(new DefaultShaderProvider());

        //Create a camera and point it to our model
        camera = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 3f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 300f;
        camera.update();

        //Create the generic camera input controller to make the app interactive
        cameraController = new  CameraInputController(camera);

        //Set up environment with simple lightning
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.8f, 0.3f, -1f));

        //Loading plane model
        game.manager.load("3D/planeanim1.g3db", Model.class);
        game.manager.finishLoading();

        Model model = game.manager.get("3D/planeanim1.g3db", Model.class);
        ModelInstance plane;
        for (int i = -5; i < 5; i++) {
            plane = new ModelInstance(model);
            instances.add(plane);
            plane.transform.setToTranslation(i * 6, 0, -1f);
        }
        plane = new ModelInstance(model);
        plane.calculateBoundingBox(bounds);
        bounds.getCenter(center);
        bounds.getDimensions(dimensions);
        radius = dimensions.len() / 2f;

        plane.transform.setToTranslation(0, 0, -1f);
        instances.add(plane);

        InputAdapter myAdapter = new InputAdapter(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                selecting = getObject(screenX, screenY);
                return selecting > 0;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return selecting > 0;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (selecting >= 0) {
                    if (selecting == getObject(screenX, screenY))
                        setSelected(selecting);
                    selecting = -1;
                    return true;
                }
                return false;
            }
        };
        Gdx.input.setInputProcessor(new InputMultiplexer(myAdapter, cameraController));
    }

    private void setSelected(int value) {
        if (selected == value) return;
        if (selected >= 0) {
            selectedInstance = instances.get(value);
        }
        selected = value;
    }

    private int getObject(int screenX, int screenY) {
        Ray ray = camera.getPickRay(screenX, screenY);
        int result = -1;
        float distance = -1;
        for (int i = 0; i < instances.size; i++) {
            final ModelInstance instance = instances.get(i);
            instance.transform.getTranslation(position);
            position.add(center);
            float dist2 = ray.origin.dst2(position);
            if (distance >= 0f && dist2 > distance) continue;
            if (Intersector.intersectRaySphere(ray, position, radius, null)) {
                result = i;
                distance = dist2;
            }
        }
        return result;
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(0.13f, 0.13f, 0.13f, 1);

        if (selectedInstance!= null){
            selectedInstance.transform.rotate(Vector3.Z, 5);
        }

        // Respond to user events and update the camera
        cameraController.update();
        visibleCount = 0;
        // Draw all model instances using the camera
        modelBatch.begin(camera);
        for (final ModelInstance instance : instances){
            if (isVisible(instance)){
                modelBatch.render(instance, environment);
                visibleCount++;
            }
        }
        modelBatch.end();
//        Gdx.app.log("VC", ""+ visibleCount);
        super.render(delta);
    }

    private boolean isVisible(ModelInstance instance) {
        instance.transform.getTranslation(position);
//        return camera.frustum.pointInFrustum(position);
        position.add(center);
//        return camera.frustum.boundsInFrustum(position, dimensions);
        return camera.frustum.sphereInFrustum(position, radius);
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
