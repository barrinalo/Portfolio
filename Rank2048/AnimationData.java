package com.codelessweb.rank2048;

import com.badlogic.gdx.math.Vector2;

public class AnimationData {
	public int x,y;
	public Vector2 Origin, Target, Dir;
	AnimationData(Vector2 Or, Vector2 Tar, int x, int y) {
		this.x = x;
		this.y = y;
		Origin = Or;
		Target = Tar;
		Dir = Target.sub(Origin);
		Dir.x /= 5.0f;
		Dir.y /= 5.0f;
	}
}
