package cheese.squeeze.gameLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import cheese.squeeze.game.Level;
import cheese.squeeze.gameObjects.Cheese;
import cheese.squeeze.gameObjects.HorizontalLine;
import cheese.squeeze.gameObjects.Line;
import cheese.squeeze.gameObjects.Mouse;
import cheese.squeeze.gameObjects.Trap;
import cheese.squeeze.gameObjects.VerticalLine;
import cheese.squeeze.helpers.AssetLoader;
import cheese.squeeze.tweenAccessors.SoundAccessor;

public class GameBoard {

	
	private List<VerticalLine> vlines;
	private TreeMap<Float,HorizontalLine> hlines;
	private List<Cheese> goals;
	private List<Trap> traps;
	private OrthographicCamera cam;
	private HorizontalLine gesturedLine;
	private Vector2 beginPosition;
	public final int beginRadius = 5;
	private float width;
	private float height;
	private List<Vector2> startPositions;
	private List<Mouse> mice;
	private List<Mouse> miceBackup;
	private static int MAX_HLINES = 15;
	private float step;
	private float start;
	private float end;
	private Level level;
	private float multip;
	
	

	public GameBoard(float width, float height,Level l) {
		this.level = l;
		this.width = width;
		this.height = height;
		this.multip = 1;
		cam = new OrthographicCamera();
		cam.setToOrtho(true,width, height);
		
		//all the lines
		makeVerticalLinesRandom();
		gesturedLine = new HorizontalLine();
		hlines = makeHlineMap();
		
		//mouse stuff
		makeMice();
		
		//trap stuff goal stuff
		//makeTrapsGoals(amountTraps,amountGoals);

		

	}
	
	private TreeMap<Float,HorizontalLine>makeHlineMap() {
		TreeMap<Float,HorizontalLine> map = new TreeMap<Float,HorizontalLine>();
		int amntSteps = (int) ((end-start)/step);
		for(int i = 1; i < amntSteps;i++){
			map.put(start + (i*step), null);
		}
		return map;
	}
	

	private void makeMice() {
		mice = new ArrayList<Mouse>();
		Mouse mouse = new Mouse(level.getSpeed()*multip,vlines.get(0));
		mice.add(mouse);	
	}

	private void makeVerticalLinesRandom() {
		this.goals = new ArrayList<Cheese>();
		this.traps = new ArrayList<Trap>();
		vlines = new ArrayList<VerticalLine>();
		float totw = (width-20)/(level.getAmountVlines()-1);
		boolean set = false;
		for(int i=0;i<level.getAmountVlines();i++) {
			VerticalLine vl = new VerticalLine(15,height-20,10+(totw*i));
			
			double rand = Math.random();
			
			if(goals.size() < level.getAmountGoals() && rand <= 0.5){
				//TODO cheese will be 4 always 1!!
				Cheese c = new Cheese(vl,4);
				vl.setGoal(c);
				goals.add(c);
				set = true;
			}
			else if(traps.size() < level.getAmountTraps()){
				Trap t = new Trap(vl);
				vl.setGoal(t);
				traps.add(t);
				set = true;
			}
			else if(!set && goals.size() < level.getAmountGoals()){
				//TODO cheese will be 4 always 1!!
				Cheese c = new Cheese(vl,4);
				vl.setGoal(c);
				goals.add(c);
				set = true;
			}
			vlines.add(vl);
			set = false;
		}
		this.step = (vlines.get(0).getY2() - vlines.get(0).getY1())/(this.MAX_HLINES+1);
		this.start = vlines.get(0).getY1();
		this.end = vlines.get(0).getY2();
		startPositions();
	}
	
	public List<VerticalLine> getVLines() {
		return vlines;
	}
	
	public void addHLine(HorizontalLine line) {
		if(!isOcupiedPosition(line.getY1())) {
			SoundAccessor.play(AssetLoader.chalk);
			hlines.put(line.getY1(), line);
			for (VerticalLine l : vlines) {
				if (l.getX1() == line.getX1()) {
					l.setNeighbour(line);
					line.setNeighbour(l);
				}
				if (l.getX1() == line.getX2()) {
					l.setNeighbour(line);
					line.setNeighbour(l);
				}
			}
			for(Mouse m : mice) {
				if(!m.isOnHorizontalLine()) {
					m.updatePath();
				}
				
			}
		}
	}

	public TreeMap<Float,HorizontalLine> getHLinesMap() {
		return hlines;
	}
	
	public ArrayList<HorizontalLine> getHLines() {
		ArrayList<HorizontalLine> list = new ArrayList<HorizontalLine>();
		for (Entry<Float,HorizontalLine> e : hlines.entrySet()) {
			HorizontalLine l = e.getValue();
			if(l != null) {
				list.add(l);
			}
		}
		return list;
	}
	
	/**
	 * A horizontal line consists of two points, given the two points, the
	 * closest vertical lines are searched and the location of the points is modified to
	 * the position of the corresponding vertical lines.
	 * 
	 * @param l the horizontal line, that is modified after executing the method.
	 */
	public void fitHorizontalLineBetweenVertivalLines(HorizontalLine l) {
		
		//TODO backtrack algo need optimization
		//calculating the closest bars
		float x1;
		float x2;
		if(l.getX1() < l.getX2()) {
			x1 = l.getX1();
			x2 = l.getX2();
		}
		else if (l.getX1() == l.getX2()) {
			x2 = (float) (l.getX1()+0.1);
			x1 = l.getX2();
		}
		else {
			x2 = l.getX1();
			x1 = l.getX2();
		}
		float minDistLeft = width;
		float minDistRight = width;
		VerticalLine vlLeft = null;
		VerticalLine vlRight = null;
		Iterator<VerticalLine> itr = vlines.iterator();
		while(itr.hasNext()) {
			VerticalLine current = itr.next();
			if(Math.abs(current.getX1() - x1) < minDistLeft) {
				
				if(current != vlRight) {
					minDistLeft = Math.abs(current.getX1() - x1);
					vlLeft = current;
					itr = vlines.iterator();
				}
				else if (minDistRight > Math.abs(current.getX1() - x1)) {
					minDistLeft = Math.abs(current.getX1() - x1);
					vlLeft = current;
					minDistRight = width;
					vlRight = null;
					itr= vlines.iterator();
				}
				
			}
			if(Math.abs(current.getX1() - x2) < minDistRight) {
				
				if(current != vlLeft) {
					minDistRight = Math.abs(current.getX1() - x2);
					vlRight = current;
					itr = vlines.iterator();
				}
				else if ( Math.abs(current.getX1() - x2) < minDistLeft) {
					minDistRight = Math.abs(current.getX1() - x2);
					vlRight = current;
					minDistLeft = width;
					vlLeft = null;
					itr= vlines.iterator();
				}
				
			}
		}
		
		float multiple = multipleOfPosition(l.getY1());
		//l.setPoint1(new Vector2(vlLeft.getX1(),l.getY1()));
		//l.setPoint1(new Vector2(vlLeft.getX1(),l.getY1()));
		l.setPoint1(new Vector2(vlLeft.getX1(),multiple));
		l.setPoint2(new Vector2(vlRight.getX2(),multiple));
	}
	

	private float multipleOfPosition(float y1) {
		//TODO make sure the resutl is not larger then the longest y position.
		int amntSteps = (int) ((y1-start)/step);
		float result = start + (amntSteps*step);
		if(result >= end){
			return end-step;
		}
		if(result <= start) {
			return start+step;
		}
		
		return result;
	}

	public OrthographicCamera getCamera() {
		return cam;
	}

	public HorizontalLine getGesturedLine() {
		return gesturedLine;
	}

	/**
	 * The gesture line is a line that is partially transparent to indicate where the actual line is going.
	 * @post the given line is also fitted between two horizontal lines.
	 * 			|	fitHorizontalLineBetweenVertivalLines(gesturedLine)
	 * @param gesturedLine
	 */
	public void setGesturedLine(HorizontalLine gesturedLine) {
		Vector3 point1 = cam.unproject(new Vector3(gesturedLine.getX1(),gesturedLine.getY1(),0));
		Vector3 point2 = cam.unproject(new Vector3(gesturedLine.getX2(),gesturedLine.getY2(),0));
		gesturedLine.setPoint1(new Vector2(point1.x,(point1.y)));
		gesturedLine.setPoint2(new Vector2(point2.x,(point2.y)));
		fitHorizontalLineBetweenVertivalLines(gesturedLine);
		if(!isOcupiedPosition(gesturedLine.getY1())) {
			this.gesturedLine = gesturedLine;
		}
		else {
			this.gesturedLine = null;
		}
	}
	
	public ArrayList<Float> getYPositions() {
		return new ArrayList<Float>(hlines.keySet());
	}
	
	 public void setGesturedLineDragged(HorizontalLine gesturedLine) {
	        Vector3 point1 = cam.unproject(new Vector3(gesturedLine.getX1(),gesturedLine.getY1(),0));
	        Vector3 point2 = cam.unproject(new Vector3(gesturedLine.getX2(),gesturedLine.getY2(),0));
	        gesturedLine.setPoint1(new Vector2(gesturedLine.getX1(),(point1.y)));
	        gesturedLine.setPoint2(new Vector2(gesturedLine.getX2(),(point2.y)));
	        fitHorizontalLineBetweenVertivalLines(gesturedLine);
			if(!isOcupiedPosition(gesturedLine.getY1())) {
				this.gesturedLine = gesturedLine;
			}
			else {
				this.gesturedLine = null;
			}
	    }
	
	private boolean isOcupiedPosition(float y) {
		if(hlines.get(y) == null) {
			System.out.println("false");
			return false;
		}
		System.out.println("true");
		return true;
	}

	public Vector2 getBeginPosition() {
		return beginPosition;
	}

	/**
	 * Set the clicked begin position to draw a red circle.
	 * @param beginPosition
	 */
	public void setClickedPosition(Vector2 beginPosition) {
		/*
		if (beginPosition!=null) {
			Vector3 point1 = cam.unproject(new Vector3(beginPosition.x,beginPosition.y,0));
			this.beginPosition = new Vector2(point1.x,point1.y);
		}
		else {
			this.beginPosition = null;
		}
		*/
	}

	/**
	 * check if the coordinates are in the area of the begin position.
	 * @param screenX
	 * @param screenY
	 * @return	true if so
	 * 				otherwise false.
	 */
	public boolean isClearable(int screenX, int screenY) {
		/*
		Vector3 point1 = cam.unproject(new Vector3(screenX,screenY,0));
		if(getBeginPosition() == null) {
			return true;
		}
		if(Math.abs(point1.x - getBeginPosition().x) < beginRadius && 
				Math.abs(point1.y - getBeginPosition().y) < beginRadius) {
			setClickedPosition(null);
			return true;
		}
		setClickedPosition(null);
		*/
		return false;
	}

	/**
	 * calculate the start positions of the mouse. These are the beginnings of the vertical lines.
	 */
	private void startPositions() {
		startPositions = new ArrayList<Vector2>();
		for(VerticalLine l : vlines) {
			startPositions.add(l.getPoint1());
		}
		
	}
	
	public List<Vector2> getStartPositions() {
		return startPositions;
	}
	
	/**
	 * Update the game board, this will update all the mice positions.
	 * @param delta
	 */
	public void update(float delta) {
		int counter = 0;
		Iterator<Mouse> itr = mice.iterator();
		while(itr.hasNext()) {
			Mouse m = itr.next();
			if(!m.isEnded()) {
				m.update(delta);
			}
			else {
				itr.remove();
				counter++;
			}
		}
		for(int i = 0; i< counter;i++) {
			this.multip+= 0.3f;
			mice.add(new Mouse(level.getSpeed()*this.multip,getRandomLine()));
		}
		Gdx.app.log("GameBoard", "update");
	}
	
	private Line getRandomLine(){
		double nb = Math.random() * vlines.size();
		return vlines.get((int) nb);
	}

	public List<Mouse> getMice() {
		return mice;
	}

	public List<Trap> getTraps() {
		return traps;
	}

	public List<Cheese> getGoals() {
		return goals;
	}

	public Vector2 unProject(int screenX, int screenY) {
		Vector3 vec = cam.unproject(new Vector3(screenX,screenY,0));
		return new Vector2(vec.x,vec.y);
	}

	public void pause() {
		miceBackup = new ArrayList<Mouse>(mice);
		mice = new ArrayList<Mouse>();
		
	}
	
	public void resume() {
		mice = new ArrayList<Mouse>(miceBackup);
	}
	
	
}
