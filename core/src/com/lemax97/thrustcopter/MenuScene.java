package com.lemax97.thrustcopter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class MenuScene extends ScreenAdapter {

    Stage stage;
    Skin skin;
    Image screenBg, title;
    Label helpTip, soundTitle;
    Table table, options;
    TextButton playButton, optionsButton, exitButton, backButton;
    CheckBox muteCheckBox;
    Slider volumeSlider;
    ThrustCopter game;

    public MenuScene(ThrustCopter thrustCopter) {
        game = thrustCopter;
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        screenBg = new Image(game.atlas.findRegion("background"));
        title    = new Image(game.manager.get("title.png", Texture.class));
        helpTip  = new Label("Tap around the plane to move it!", skin);
        helpTip.setColor(Color.NAVY);
        table    = new Table().debug();
        playButton = new TextButton("PLAY GAME", skin);
        table.add(playButton).padBottom(10);
        table.row();
        optionsButton = new TextButton("SOUND OPTIONS", skin);
        table.add(optionsButton).padBottom(10);
        table.row();
        table.add(new TextButton("LEADERBOARD", skin)).padBottom(10);
        table.row();
        exitButton = new TextButton("EXIT GAME", skin);
        table.add(exitButton);
        table.setPosition(400, -200);

        options = new Table().debug();
        soundTitle = new Label("SOUND OPTIONS", skin);
        soundTitle.setColor(Color.NAVY);
        options.add(soundTitle).padBottom(25).colspan(2);
        options.row();
        muteCheckBox = new CheckBox("MUTE ALL", skin);
        options.add(muteCheckBox).padBottom(10).colspan(2);
        options.row();
        options.add(new Label("VOLUME", skin)).padBottom(10).padRight(10);
        volumeSlider= new Slider(0, 2, 0.2f, false, skin);
        options.add(volumeSlider).padTop(10).padBottom(20);
        options.row();
        backButton = new TextButton("BACK", skin);
        options.add(backButton).colspan(2).padTop(20);
        options.setPosition(400, -200);
        muteCheckBox.setChecked(!game.soundEnabled);
        volumeSlider.setValue(game.soundVolume);

        stage.addActor(screenBg);
        stage.addActor(title);
        stage.addActor(helpTip);
        stage.addActor(table);
        stage.addActor(options);

       playButton.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               game.setScreen(new ThrustCopterScene(game));
           }
       });

       optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMenu(false);
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.soundVolume = volumeSlider.getValue();
            }
        });

        muteCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.soundEnabled = !muteCheckBox.isChecked();
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMenu(true);
            }
        });
    }

    @Override
    public void show() {
        title.setPosition(400 - title.getWidth() / 2, 450);
        helpTip.setPosition(400 - title.getWidth() / 2, 450);

        MoveToAction actionMove = Actions.action(MoveToAction.class);
        actionMove.setPosition(400 - title.getWidth() / 2, 320);
        actionMove.setDuration(2);
        actionMove.setInterpolation(Interpolation.elasticOut);
        title.addAction(actionMove);

        showMenu(true);
    }

    private void showMenu(boolean flag) {
        MoveToAction actionMove1 = Actions.action(MoveToAction.class);//out
        actionMove1.setPosition(400, -200);
        actionMove1.setDuration(1);
        actionMove1.setInterpolation(Interpolation.swingIn);

        MoveToAction actionMove2 = Actions.action(MoveToAction.class);//in
        actionMove2.setPosition(400, 190);
        actionMove2.setDuration(1.5f);
        actionMove2.setInterpolation(Interpolation.swing);

        if (flag) {
            table.addAction(actionMove2);
            options.addAction(actionMove1);
        }else {
            options.addAction(actionMove2);
            table.addAction(actionMove1);
        }
    }
}
