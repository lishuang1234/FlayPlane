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
	private static final float PLANE_JUMP = 350;// �ɻ������߶�
	private static final float GRAVITY = -20;// �ɻ�����׹���ٶ�
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
	Vector2 planePositionVector2 = new Vector2();// �ɻ�λ��
	Vector2 planeViocityVector2 = new Vector2();// �ɻ��ٶ�
	float planeStatetime = 0;
	Vector2 planeGravityVector2 = new Vector2();// �ɻ��½�
	Array<Rock> rocks = new Array<MainGame.Rock>();
	GameState gameState = GameState.Start;// ��Ϸ��ʼ״̬
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
		planeCamera.setToOrtho(false, 800, 480);// ��Ϸ�ɻ��ƶ����
		stateCamera = new OrthographicCamera();
		stateCamera.setToOrtho(false, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());// ��Ϸ״̬��ʾ���
		stateCamera.update();

		font = new BitmapFont(Gdx.files.internal("flayplane/arial.fnt"));
		backgroundTexture = new Texture(
				Gdx.files.internal("flayplane/background.png"));
		rockGroudRegion = new TextureRegion(new Texture(
				Gdx.files.internal("flayplane/ground.png")));// �ײ���ʯ�ϰ���
		cellingRegion = new TextureRegion(rockGroudRegion);// ������ʯ�ϰ���
		cellingRegion.flip(true, true);

		rockUpRegion = new TextureRegion(new Texture(
				Gdx.files.internal("flayplane/rock.png")));
		rockDownRegion = new TextureRegion(rockUpRegion);
		rockDownRegion.flip(false, true);// ��ʯ�ϰ���ͼƬ

		Texture frame1 = new Texture(Gdx.files.internal("flayplane/plane1.png"));
		frame1.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		Texture frame2 = new Texture(Gdx.files.internal("flayplane/plane2.png"));
		Texture frame3 = new Texture(Gdx.files.internal("flayplane/plane3.png"));// ���ַɻ�״̬ͼ
		planeAnimation = new Animation(0.05f, new TextureRegion(frame1),
				new TextureRegion(frame2), new TextureRegion(frame3),
				new TextureRegion(frame2));// �ɻ�����
		planeAnimation.setPlayMode(PlayMode.LOOP);

		readyRegion = new TextureRegion(new Texture(
				Gdx.files.internal("flayplane/ready.png")));
		gameOveRegion = new TextureRegion(new Texture(
				Gdx.files.internal("flayplane/gameover.png")));// ��Ϸ״̬��ͼ

		music = Gdx.audio.newMusic(Gdx.files.internal("flayplane/music.mp3"));
		music.setLooping(true);
		music.play();// ��Ϸ��������

		sound = Gdx.audio.newSound(Gdx.files.internal("flayplane/explode.wav"));// ��ײЧ����Ч

		recorder = Gdx.audio.newAudioRecorder(22050, true);// ��˷���Ƶ��ȡ

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
					: rockUpRegion));// ��ʼ����ʯ�ϰ���
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

	/** ���ƽ��� */
	private void drawWorld() {
		// TODO Auto-generated method stub
		planeCamera.update();
		spriteBatch.setProjectionMatrix(planeCamera.combined);// ����ͶӰ����
		spriteBatch.begin();
		spriteBatch.draw(backgroundTexture, planeCamera.position.x
				- rockGroudRegion.getRegionWidth() / 2, 0);// ������Ϸ���ⱳ��
		for (Rock rock : rocks) {
			spriteBatch.draw(rock.imageRegion, rock.positionVector2.x,
					rock.positionVector2.y);
		}
		spriteBatch.draw(rockGroudRegion, groundOffset, 0);// ����ʯ�ϰ���
		spriteBatch.draw(rockGroudRegion,
				groundOffset + rockGroudRegion.getRegionWidth(), 0);

		spriteBatch.draw(cellingRegion, groundOffset,
				480 - cellingRegion.getRegionHeight());// �ϲ���ʯ�ϰ���
		spriteBatch.draw(cellingRegion, groundOffset+cellingRegion.getRegionWidth(),
				480 - cellingRegion.getRegionHeight());

		spriteBatch.draw(planeAnimation.getKeyFrame(planeStatetime),
				planePositionVector2.x, planePositionVector2.y);// ���Ʒɻ�
		spriteBatch.end();

		spriteBatch.setProjectionMatrix(stateCamera.combined);// ����״̬ͼƬ
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

	/** ���·ɻ�״̬ */
	private void updateWorld() {
		// TODO Auto-generated method stub
		float deltaTime = Gdx.graphics.getDeltaTime();
		planeStatetime += deltaTime;
		 if (Gdx.input.isTouched(0)) {// ͨ�����ƿ�����Ϸ״̬
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
			planeViocityVector2.add(planeGravityVector2);// �����������
		}
		planePositionVector2.mulAdd(planeViocityVector2, deltaTime);
		planeCamera.position.x = planePositionVector2.x + 350;// �ƶ��ӽ������ɻ�һ���ƶ�

		if (planeCamera.position.x - groundOffset - 400 > rockGroudRegion
				.getRegionWidth()) {
			groundOffset += rockGroudRegion.getRegionWidth();// �����²��ֵ���ʯ�ϰ��������չ
		}
		planeRectangle.set(planePositionVector2.x + 20, planePositionVector2.y,
				planeAnimation.getKeyFrames()[0].getRegionWidth() - 20,
				planeAnimation.getKeyFrames()[0].getRegionHeight());// ���ɻ���������

		for (Rock rock : rocks) {
			if (planeCamera.position.x - rock.positionVector2.x - 400 > rock.imageRegion
					.getRegionWidth()) {// ��ʯ�ϰ������Ļ����ʧ
				boolean isDown = MathUtils.randomBoolean();
				rock.positionVector2.x += 5 * 500;// ����ʧ����ʯ�Ƶ������
				rock.positionVector2.y = isDown ? 480 - rock.imageRegion
						.getRegionHeight() : 0;
				rock.imageRegion = isDown ? rockDownRegion : rockUpRegion;
				rock.counted = false;
			}

			rockRectangle
					.set(rock.positionVector2.x
							+ (rock.imageRegion.getRegionWidth() - 30) / 2 + 20,
							rock.positionVector2.y, 20,
							rock.imageRegion.getRegionHeight() - 10);// ������ʯ�ϰ������
			if (planeRectangle.overlaps(rockRectangle)) {// �ɻ�����ʯ��ײ
				if (gameState != GameState.GameOver)
					sound.play();
				gameState = GameState.GameOver;
				planeViocityVector2.x = 0;// �ɻ��ٶ�Ϊ0
			}
			if (rock.positionVector2.x < planePositionVector2.x
					&& !rock.counted) {// �ɻ���Խ��ʯ�ϰ��Ʒ�
				score++;
				rock.counted = true;

			}
		}

		if (planePositionVector2.y < rockGroudRegion.getRegionHeight() - 20
				|| planePositionVector2.y
						+ planeAnimation.getKeyFrames()[0].getRegionHeight() > 480 - rockGroudRegion
						.getRegionHeight() + 20) {// �ɻ������ײ����߶����ϰ���
			if (gameState != GameState.GameOver)
				sound.play();
			gameState = GameState.GameOver;
			planeViocityVector2.x = 0;
		}
	}

	/* ��¼��Ƶ�豸����* */
	private void getAudioVoice() {
		// TODO Auto-generated method stub
		shortPCM = new short[450];
		recorder.read(shortPCM, 0, shortPCM.length);
	}

	/** ��ʯ�ϰ��� */
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
