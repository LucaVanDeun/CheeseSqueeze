package cheese.squeeze.gameworld;

import cheese.squeeze.gameLogic.GameBoard;
import cheese.squeeze.gameObjects.Goal;
import cheese.squeeze.gameObjects.HorizontalLine;
import cheese.squeeze.gameObjects.Line;
import cheese.squeeze.gameObjects.Mouse;
import cheese.squeeze.gameObjects.Trap;
import cheese.squeeze.helpers.AssetLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class GameRenderer {

	private OrthographicCamera cam;
	private ShapeRenderer shapeRenderer;
	private SpriteBatch batcher;
	private int midPointY;
	private int height;
	private int width;
	private GameBoard board;
	private boolean updateStaticElements;
	private TextureRegion bg,goal,trap,mouse;


	public GameRenderer(GameBoard board,int midPointY,int height, int width) {
		this.height = height;
		this.width = width;
		this.board = board;
		cam = board.getCamera();
		updateStaticElements = true;

		batcher = new SpriteBatch();
		// Attach batcher to camera
		batcher.setProjectionMatrix(cam.combined);

		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(cam.combined);



		// Call helper methods to initialize instance variables
		initGameObjects();
		initAssets();
	}

	public void render() {


		// Fill the entire screen with black, to prevent potential flickering.
		//Gdx.gl.glClearColor(0, 0, 0, 1);
		//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//shapeRenderer.begin(ShapeType.Filled);
		//shapeRenderer.end();

		//TODO gives strange things if it is updated once
		//Fixed Elements
		if(updateStaticElements){
			drawBackground();

			drawVerticalLines();

			drawTraps();
			
			drawGoals();
			//updateStaticElements = false;
		}

		
		//Dynamic elements
		
		drawHorizontalLines();

		drawGestureLine();
		
		drawRedDot();
		
		drawMice();
		





		Gdx.app.log("GameRenderer", "rendered");
	}

	private void drawRedDot() {
		if(board.getBeginPosition() != null) {

			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(new Color(255.0f, 0f, 0f, 0.5f));
			shapeRenderer.circle(board.getBeginPosition().x, board.getBeginPosition().y, board.beginRadius);
			shapeRenderer.end();

			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
		
	}

	private void drawMice() {
		for(Mouse m : board.getMice()) {
			batcher.begin();
			// enable transparency
			batcher.enableBlending();
			System.out.println(m.getX());
			System.out.println(m.getY());
			batcher.draw(mouse,m.getX(),m.getY(),width/7,height/10);
			//AssetLoader.mouseS.draw(batcher);
			// End SpriteBatch
			batcher.end();
		}
	}

	private void drawGestureLine() {
		HorizontalLine line = board.getGesturedLine();
		if(line.isdrawable()) {

			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(new Color(255.0f, 255.0f, 255.0f, 0.4f));
			shapeRenderer.line(line.getPoint1(),line.getPoint2());
			shapeRenderer.end();

			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
	}

	private void drawHorizontalLines() {
		for(Line l : board.getHLines()) {
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.line(l.getPoint1(),l.getPoint2());
			shapeRenderer.end();
		}
	}

	private void drawGoals() {
		// TODO Auto-generated method stub
		for(Goal g: board.getGoals()) {
			batcher.begin();
			batcher.enableBlending();
			// 2* 10 rekening houdend met die schaal : width/10
			batcher.draw(goal,(g.getPosition().x)-(AssetLoader.goalCenter.x/(2*10)), g.getPosition().y+2,width/10,height/10);
			batcher.end();
		}
	}

	private void drawTraps() {
		for(Trap t: board.getTraps()) {
			batcher.begin();
			batcher.enableBlending();
			// 2* 10 rekening houdend met die schaal : width/10
			batcher.draw(trap,(t.getPosition().x)-(AssetLoader.goalCenter.x/(2*10)), t.getPosition().y+2,width/10,height/10);
			batcher.end();
		}
		
	}

	private void drawVerticalLines() {
		//set line w
		//TODO scale with zoom
		Gdx.gl.glLineWidth(50);

		// begin line draw
		shapeRenderer.begin(ShapeType.Line);
		//Draw Static lines.
		shapeRenderer.setColor(new Color(255.0f, 255.0f, 255.0f, 1f));
		//shapeRenderer.setColor(126f, 71f, 255.0f, (float) 1);

		for(Line l : board.getVLines()) {
			shapeRenderer.line(l.getPoint1(),l.getPoint2());
		}

		// End ShapeRenderer
		shapeRenderer.end();

	}

	private void drawBackground() {
		// Begin SpriteBatch draw bg
		batcher.begin();
		// Disable transparency
		batcher.disableBlending();     
		batcher.draw(bg, 0, midPointY, width, height);
		batcher.end();
	}

	private void initGameObjects() {

	}

	private void initAssets() {
		bg = AssetLoader.bg;
		goal = AssetLoader.goal;
		trap = AssetLoader.trap;
		mouse = AssetLoader.mouse;
	}

}