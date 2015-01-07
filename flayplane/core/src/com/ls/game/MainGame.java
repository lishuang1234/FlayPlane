package com.ls.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class MainGame extends ApplicationAdapter {
	private static final float PLANE_JUMP = 350;// 飞机起跳高度
	private static final float GRAVITY = -20;// 飞机向下坠落速度
	private static final float PLANE_START_X = 50;
	private static final float PLANE_START_Y = 240;
	SpriteBatch spriteBatch;
	OrthographicCamera planeCamera;
	OrthographicCamera stateCamera;
	Texture backgroundTexture;
	TextureRegion rockGroudRegion;
	float groundOffset;
	TextureRegion cellingRegion;
	TextureRegion rockUpRegion;
	TextureRegion rockDownRegion;
	Animation planeAnimation;
	TextureRegion readyRegion;
	TextureRegion gameOveRegion;
	BitmapFont font;
	Vector2 planePositionVector2 = new Vector2();// 飞机位置
	Vector2 planeViocityVector2 = new Vector2();// 飞机速度
	float planeStatetime = 0;
	Vector2 planeGravityVector2 = new Vector2();// 飞机下降
	Array<Rock> rocks = new Array<MainGame.Rock>();
	GameState gameState = GameState.Start;// 游戏初始状态
	int score = 0;
	Rectangle planeRectangle = new Rectangle();
	Rectangle rockRectangle = new Rectangle();

	Music music;
	Sound sound;

	AudioRecorder recorder;
	short[] shortPCM;

	@Override
	public void create() {
		spriteBatch = new SpriteBatch();
		planeCamera = new OrthographicCamera();
		planeCamera.setToOrtho(false, 800, 480);// 游戏飞机移动相机
		stateCamera = new OrthographicCamera();
		stateCamera.setToOrtho(false, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());// 游戏状态显示相机
		stateCamera.update();

		font = new BitmapFont(Gdx.files.internal("flayplane/arial.fnt"));
		backgroundTexture = new Texture(
				Gdx.files.internal("flayplane/background.png"));
		rockGroudRegion = new TextureRegion(new Texture(
				Gdx.files.internal("flayplane/ground.png")));// 底部岩石障碍物
		cellingRegion = new TextureRegion(rockGroudRegion);// 顶部岩石障碍物
		cellingRegion.flip(true, true);

		rockUpRegion = new TextureRegion(new Texture(
				Gdx.files.internal("flayplane/rock.png")));
		rockDownRegion = new TextureRegion(rockUpRegion);
		rockDownRegion.flip(false, true);// 岩石障碍物图片

		Texture frame1 = new Texture(Gdx.files.internal("flayplane/plane1.png"));
		frame1.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		Texture frame2 = new Texture(Gdx.files.internal("flayplane/plane2.png"));
		Texture frame3 = new Texture(Gdx.files.internal("flayplane/plane3.png"));// 三种飞机状态图
		planeAnimation = new Animation(0.05f, new TextureRegion(frame1),
				new TextureRegion(frame2), new TextureRegion(frame3),
				new TextureRegion(frame2));// 飞机动画
		planeAnimation.setPlayMode(PlayMode.LOOP);

		readyRegion = new TextureRegion(new Texture(
				Gdx.files.internal("flayplane/ready.png")));
		gameOveRegion = new TextureRegion(new Texture(
				Gdx.files.internal("flayplane/gameover.png")));// 游戏状态贴图

		music = Gdx.audio.newMusic(Gdx.files.internal("flayplane/music.mp3"));
		music.setLooping(true);
		music.play();// 游戏背景音乐

		sound = Gdx.audio.newSound(Gdx.files.internal("flayplane/explode.wav"));// 碰撞效果音效

		recorder = Gdx.audio.newAudioRecorder(22050, true);// 麦克风音频获取

		resetWorld();
	}

	private void resetWorld() {
		// TODO Auto-generated method stub
		score = 0;
		groundOffset = 0;
		planePositionVector2.set(PLANE_START_X, PLANE_START_Y);
		planeViocityVector2.set(0, 0);
		planeGravityVector2.set(0, GRAVITY);
		planeCamera.position.x = 400;
		rocks.clear();

		for (int i = 0; i < 5; i++) {
			boolean isDown = MathUtils.randomBoolean();
			rocks.add(new Rock(700 + i * 500, isDown ? 480 - rockDownRegion
					.getRegionHeight() : 0, isDown ? rockDownRegion
					: rockUpRegion));// 初始化岩石障碍物
		}

	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getAudioVoice();
		updateWorld();
		drawWorld();

	}

	/** 绘制界面 */
	private void drawWorld() {
		// TODO Auto-generated method stub
		planeCamera.update();
		spriteBatch.setProjectionMatrix(planeCamera.combined);// 设置投影矩形
		spriteBatch.begin();
		spriteBatch.draw(backgroundTexture, planeCamera.position.x
				- rockGroudRegion.getRegionWidth() / 2, 0);// 绘制游戏主题背景
		for (Rock rock : rocks) {
			spriteBatch.draw(rock.imageRegion, rock.positionVector2.x,
					rock.positionVector2.y);
		}
		spriteBatch.draw(rockGroudRegion, groundOffset, 0);// 下岩石障碍物
		spriteBatch.draw(rockGroudRegion,
				groundOffset + rockGroudRegion.getRegionWidth(), 0);

		spriteBatch.draw(cellingRegion, groundOffset,
				480 - cellingRegion.getRegionHeight());// 上层岩石障碍物
		spriteBatch.draw(cellingRegion, groundOffset+cellingRegion.getRegionWidth(),
				480 - cellingRegion.getRegionHeight());

		spriteBatch.draw(planeAnimation.getKeyFrame(planeStatetime),
				planePositionVector2.x, planePositionVector2.y);// 绘制飞机
		spriteBatch.end();

		spriteBatch.setProjectionMatrix(stateCamera.combined);// 绘制状态图片
		spriteBatch.begin();
		if (gameState == GameState.Start) {
			spriteBatch.draw(
					readyRegion,
					Gdx.graphics.getWidth() / 2 - readyRegion.getRegionWidth()
							/ 2,
					Gdx.graphics.getHeight() / 2
							- readyRegion.getRegionHeight() / 2);
		}
		if (gameState == GameState.GameOver) {
			spriteBatch.draw(
					gameOveRegion,
					Gdx.graphics.getWidth() / 2
							- gameOveRegion.getRegionWidth() / 2,
					Gdx.graphics.getHeight() / 2
							- gameOveRegion.getRegionHeight() / 2);
		}
		if (gameState == GameState.GameOver || gameState == GameState.Running) {
			font.draw(spriteBatch, "" + score, Gdx.graphics.getWidth() / 2,
					Gdx.graphics.getHeight() - 60);
		}
		spriteBatch.end();
	}

	/** 更新飞机状态 */
	private void updateWorld() {
		// TODO Auto-generated method stub
		float deltaTime = Gdx.graphics.getDeltaTime();
		planeStatetime += deltaTime;
		 if (Gdx.input.isTouched(0)) {// 通过手势控制游戏状态
	//	if (Math.abs(shortPCM[0]) > 9000) {
			if (gameState == GameState.Start) {
				gameState = GameState.Running;
			}
			if (gameState == GameState.Running) {
				planeViocityVector2.set(350, PLANE_JUMP);
			}
			if (gameState == GameState.GameOver) {
				gameState = GameState.Start;
				resetWorld();
			}
		}
		// }
		if (gameState != GameState.Start) {
			planeViocityVector2.add(planeGravityVector2);// 添加向下向量
		}
		planePositionVector2.mulAdd(planeViocityVector2, deltaTime);
		planeCamera.position.x = planePositionVector2.x + 350;// 移动视角相机与飞机一起移动

		if (planeCamera.position.x - groundOffset - 400 > rockGroudRegion
				.getRegionWidth()) {
			groundOffset += rockGroudRegion.getRegionWidth();// 对上下部分的岩石障碍物进行延展
		}
		planeRectangle.set(planePositionVector2.x + 20, planePositionVector2.y,
				planeAnimation.getKeyFrames()[0].getRegionWidth() - 20,
				planeAnimation.getKeyFrames()[0].getRegionHeight());// 将飞机包裹矩形

		for (Rock rock : rocks) {
			if (planeCamera.position.x - rock.positionVector2.x - 400 > rock.imageRegion
					.getRegionWidth()) {// 岩石障碍物从屏幕上消失
				boolean isDown = MathUtils.randomBoolean();
				rock.positionVector2.x += 5 * 500;// 将消失的岩石移到最后面
				rock.positionVector2.y = isDown ? 480 - rock.imageRegion
						.getRegionHeight() : 0;
				rock.imageRegion = isDown ? rockDownRegion : rockUpRegion;
				rock.counted = false;
			}

			rockRectangle
					.set(rock.positionVector2.x
							+ (rock.imageRegion.getRegionWidth() - 30) / 2 + 20,
							rock.positionVector2.y, 20,
							rock.imageRegion.getRegionHeight() - 10);// 设置岩石障碍物矩形
			if (planeRectangle.overlaps(rockRectangle)) {// 飞机与岩石碰撞
				if (gameState != GameState.GameOver)
					sound.play();
				gameState = GameState.GameOver;
				planeViocityVector2.x = 0;// 飞机速度为0
			}
			if (rock.positionVector2.x < planePositionVector2.x
					&& !rock.counted) {// 飞机飞越岩石障碍计分
				score++;
				rock.counted = true;

			}
		}

		if (planePositionVector2.y < rockGroudRegion.getRegionHeight() - 20
				|| planePositionVector2.y
						+ planeAnimation.getKeyFrames()[0].getRegionHeight() > 480 - rockGroudRegion
						.getRegionHeight() + 20) {// 飞机触碰底部或者顶部障碍物
			if (gameState != GameState.GameOver)
				sound.play();
			gameState = GameState.GameOver;
			planeViocityVector2.x = 0;
		}
	}

	/* 记录音频设备输入* */
	private void getAudioVoice() {
		// TODO Auto-generated method stub
		shortPCM = new short[450];
		recorder.read(shortPCM, 0, shortPCM.length);
	}

	/** 岩石障碍物 */
	static class Rock {
		Vector2 positionVector2 = new Vector2();
		TextureRegion imageRegion;
		boolean counted;

		public Rock(float x, float y, TextureRegion image) {
			this.positionVector2.x = x;
			this.positionVector2.y = y;
			this.imageRegion = image;
		}
	}

	static enum GameState {
		Start, Running, GameOver
	}
}
