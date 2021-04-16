package com.lemax97.thrustcopter.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.lemax97.thrustcopter.ThrustCopter;

import static com.lemax97.thrustcopter.ThrustCopter.screenHeight;
import static com.lemax97.thrustcopter.ThrustCopter.screenWidth;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = screenWidth;
		config.height = screenHeight;
		new LwjglApplication(new ThrustCopter(), config);
	}
}
