/**
* @name AuraluxTest
* @package rs.kockasystems.auraluxtest
* @date 29/09/15
* @author KockaAdmiralac
* @author Akimil
*/


package rs.kockasystems.auraluxtest;

/** 
* Library imports
*/

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;



/**
* The main class of the application.
* @author KockaAdmiralac
*/

public class AuraluxTest extends ApplicationAdapter {
	
	// Class variables
	private final Input input = new Input();					// Input processor
	private final Random rnd = new Random();					// Random generator
	private Vector2 touched = null, dragged = new Vector2();	// Circle coordinates
	private ShapeRenderer renderer;								// Renderer for circle
	private Circle selectedCircle;								// The selected circle
	private boolean selecting, forcedRadius;					// Is circle currently being selected, and is radius too small?
	private SpriteBatch batch;									// Batch to draw things with
	private Texture soldierTexture, barracksTexture, 			
					enemySoldierTexture, enemyBarracksTexture;	// Textures for soldiers (balls) and barracks (suns)
	private final int MAX_BARRACKS = 5;							// The max number of barracks
	private ArrayList<Soldier> soldiers;	 					// The list of soldiers
	private ArrayList<Barrack> barracks;						// The list of barracks
	private BitmapFont font;									// The font for writing
	
	// Constants
	private final byte SUN_STEP 		   = 60;				// The number of frames for sun update (max = 255, 60 = 1 second)
	private final float COLLISION_DISTANCE = 1;					// If two soldiers are closer than this, they collide and disapear
	private final float SUN_ATTACK 		   = 30;				// When soldier is near to an enemy sun, he reduces its health
	private final int SUN_MAGNET		   = 30;				// When soldier has went near the sun, he auto repairs/attacks the sun
	private final int SOLDIER_SPEED 	   = 5;					// The speed of soldiers, ie. the number of pixels that soldier passes in s second
	private final int MAX_HEALTH 		   = 100;				// The health of the normal (unattacked) sun
	private final int ATTACK_DAMAGE		   = 1;					// The damage made to an enemy sun.
	private final int HEAL_REGENERATION    = 1;					// The health regeneration when sun is healed.
	
	
	
	/** 
	* The default method that is called when window is opened
	* @author KockaAdmiralac
	*/
	
	@Override
	public void create () {
		
		// Variable creating
		batch 					= new SpriteBatch();
		renderer 				= new ShapeRenderer();
		selectedCircle			= new Circle();
		soldierTexture			= new Texture("FriendSoldier.png");
		barracksTexture 		= new Texture("FriendBarracks.png");
		enemySoldierTexture 	= new Texture("EnemySoldier.png");
		enemyBarracksTexture 	= new Texture("EnemyBarracks.png");
		soldiers 				= new ArrayList<Soldier>();
		barracks				= new ArrayList<Barrack>();
		font					= new BitmapFont();
		
		// Initializing barracks
		for(int i=0; i<MAX_BARRACKS; i++){
			barracks.add(new Barrack(rnd.nextFloat() * 640, rnd.nextFloat() * 480, (byte)0));
			barracks.add(new Barrack(rnd.nextFloat() * 640, rnd.nextFloat() * 480, (byte)1));
		}		
		
		// Setting the Input processor
		Gdx.input.setInputProcessor(input);
		
	}
	
	
	
	
	/**
	 * The default method that is called when window is closed.
	 * @author KockaAdmiralac
	 */
	@Override
	public void dispose() {
		// Massive disposal
		soldierTexture	.dispose();
		barracksTexture	.dispose();
	}
	
	
	
	/**
	 * The default method called on every 60th part of a second.
	 * @author KockaAdmiralac
	 */
	@Override
	public void render () {
		
		// Clearing
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// Begin drawing
		batch.begin();
			
			// Draw barracks' sprites
			for(int i=0; i<barracks.size(); i++) {
				barracks.get(i).draw(batch);
				barracks.get(i).update();
			}
			
			// Draw soldier sprites
			for(int i=0; i<soldiers.size(); i++) {
				soldiers.get(i).draw(batch);
				soldiers.get(i).update();
			}
			
			
		batch.end();
		
		// Rendering the select circle
		if(touched != null) {
			renderer.begin(ShapeType.Line);
			selectedCircle.x = (touched.x + dragged.x) / 2;
			selectedCircle.y = (touched.y + dragged.y) / 2;
			selectedCircle.radius = forcedRadius ? 100f : pitagora(Math.abs(touched.x - dragged.x), Math.abs(touched.y - dragged.y)) / 2;
			renderer.circle(selectedCircle.x, Gdx.graphics.getHeight() - selectedCircle.y, selectedCircle.radius);
			renderer.end();
		}
		
	}
	
	
	
	/**
	 * A class for handling the input for the whole game.
	 * @author KockaAdmiralac
	 */
	private class Input implements InputProcessor
	{
		
		// Creating variables
		private float mouseX = 0.0f, mouseY = 0.0f;
		
		
		// Unused methods
		@Override
		public boolean keyDown(int keycode) { return false; }
		@Override
		public boolean keyUp(int keycode) { return false; }
		@Override
		public boolean keyTyped(char character) { return false; }
		@Override
		public boolean scrolled(int amount) { return false; }
		
		
		
		/**
		 * The default method for handling the mouse click or touch event.
		 * @author KockaAdmiralac
		 */
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) { 
			if(button == Buttons.LEFT && !selecting){
				touched = new Vector2(screenX, screenY);
				dragged.x = screenX;
				dragged.y = screenY;
			}
			return true;
		}
		
		
		
		/**
		 * The default method for handling the mouse un-click or un-touch event.
		 * @author KockaAdmiralac
		 * @author Akimil
		 */
		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) { 
			if(selecting){
				for(int i=0; i<soldiers.size(); i++)if(soldiers.get(i).x <= selectedCircle.x + selectedCircle.radius && soldiers.get(i).x >= selectedCircle.x - selectedCircle.radius && Gdx.graphics.getHeight() - soldiers.get(i).y <= selectedCircle.y + selectedCircle.radius && Gdx.graphics.getHeight() - soldiers.get(i).y >= selectedCircle.y - selectedCircle.radius)if(selectedCircle.contains(soldiers.get(i).x, Gdx.graphics.getHeight() - soldiers.get(i).y) && selectedCircle.contains(soldiers.get(i).x, Gdx.graphics.getHeight() - soldiers.get(i).y)){
					soldiers.get(i).px = screenX;
					soldiers.get(i).py = Gdx.graphics.getHeight() - screenY;
					// If this is near to some sun, order to the soldier to attack/repare the sun
					for(int j=0; j<barracks.size(); j++)
					{
						if(Math.abs(soldiers.get(i).px - barracks.get(j).x) < SUN_MAGNET && Math.abs(soldiers.get(i).py - barracks.get(j).y) < SUN_MAGNET){
							soldiers.get(i).px = barracks.get(j).x;
							soldiers.get(i).py = barracks.get(j).y;
						}
					}
					
					// Computing soldier speed
					soldiers.get(i).vx = (float)SOLDIER_SPEED / pitagora(soldiers.get(i).px - soldiers.get(i).x, soldiers.get(i).py - soldiers.get(i).y) * (soldiers.get(i).px - soldiers.get(i).x);
					soldiers.get(i).vy = (float)SOLDIER_SPEED / pitagora(soldiers.get(i).px - soldiers.get(i).x, soldiers.get(i).py - soldiers.get(i).y) * (soldiers.get(i).py - soldiers.get(i).y);
				}
				touched = null;
				forcedRadius = false;
			}
			else if(selectedCircle.radius < 1f) forcedRadius = true;
			if(button == Buttons.LEFT) selecting = !selecting;
			return true;
		}
		
		
		
		/**
		 * The default method for handling the mouse drag or drag event.
		 * @author KockaAdmiralac
		 */
		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			if(touched != null) {
				dragged.x = screenX;
				dragged.y = screenY;
				if(selecting) {
					selecting = false;
					forcedRadius = false;
					touched.x = screenX;
					touched.y = screenY;
				}
			}
			return true;
		}
		
		
		
		/**
		 * The default method for handling the mouse move.
		 * @author KockaAdmiralac
		 */
		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			mouseX = screenX;
			mouseY = screenY;
			return true;
		}
		
		
		
	}
	
	
	
	/**
	 * A data class for sun soldiers.
	 * x  	- The X position of the soldier.
	 * y  	- The Y position of the soldier.
	 * px 	- The X position of the destination.
	 * py 	- The Y position of the destination.
	 * team - The team in which soldier is.
	 * @author KockaAdmiralac
	 */
	private class Soldier {
		
		// Variable definition
		public float x, y, px, py, vx, vy;
		private byte team;
		private boolean exists = true;
		public boolean temp = false;
		
		
		
		/**
		 * The class constructor.
		 * @param x
		 * @param y
		 * @param vx
		 * @param vy
		 * @author KockaAdmiralac
		 * @author Akimil
		 */
		public Soldier(float x, float y, float vx, float vy, byte team) {
			this.x = x;
			this.px = x - 10;
			this.y = y;
			this.py = y - 10;
			this.vx = vx;
			this.vy = vy;
			this.team = team;
		}
		
		
		
		/**
		 * The method called on frame update.
		 * @author Akimil
		 */
		public void update(){
			
			if(!exists)return;
			
			// Motion
			if(Math.abs(px - x) <= 10f && Math.abs(py - y) <= 10f) vx = vy = 0;
			else
			{
				this.x += vx;
				this.y += vy;
			}
			
			int i = 0;
			
			// Collision detector - not optimal at all, but simple
			for(; i<soldiers.size(); i++) 
			{
				if(soldiers.get(i).team != this.team)
				{
					if(dist(this, soldiers.get(i)) < COLLISION_DISTANCE)
					{
						// There is 50% chance that both of them die, 25% that first wins, 25% that second wins
						float tmp = rnd.nextFloat();
						if(tmp >= 0 && tmp < 0.75) die();						// First dies
						else if(tmp >= 0.25 && tmp < 1) soldiers.get(i).die();  // Second dies
					}
				}
			}
			
			// Sun attack detector
			for(i=0; i<barracks.size(); i++)
			{
				// Attack
				if(barracks.get(i).team != this.team && pitagora(this.x - barracks.get(i).x, this.y - barracks.get(i).y) < SUN_ATTACK)
				{
					barracks.get(i).attack();	 											// Attack the barrack
					if(rnd.nextFloat() > 0.5f)die();										// 50% chance of surviving
					if(barracks.get(i).health <= 0)barracks.get(i).changeTeam(this.team);	// Change team if lost health
					break;
				}
				
				// Recover
				else if(barracks.get(i).team == this.team && pitagora(this.x - barracks.get(i).x, this.y - barracks.get(i).y) < SUN_ATTACK && barracks.get(i).health != MAX_HEALTH)
				{
					if(rnd.nextFloat() > 0.5f)die();	// 50% chance of surviving
					barracks.get(i).heal();			 	// We could implement randomization here also
					break;
				}
			}
		}
		
		
		
		/**
		 * The method called when drawing the soldier.
		 * @param b
		 */
		public void draw(SpriteBatch b){
			if(exists)b.draw(team == 0 ? soldierTexture : enemySoldierTexture, x, y);
		}
		
		
		
		/**
		 * The method called when soldier dies.
		 * @todo Add death animations
		 * @author KockaAdmiralac
		 */
		public void die(){
			exists = false;
		}
		
		
		
	}
	
	
	
	/**
	 * The data class for barracks (suns).
	 * x         - The X position of the barrack
	 * y         - The Y position of the barrack
	 * level     - The level of the barrack
	 * team	     - The team of the barrack
	 * gauge     - The health gauge
	 * gaugeTeam - What team is posessing the health gauge
	 * step      - Current step, by default when it reaches 60 new soldier is made.
	 * @author KockaAdmiralac
	 */
	private class Barrack {
		
		// Variable definition
		public float x, y;
		private byte level, team, gauge, gaugeTeam, step;
		private float health = MAX_HEALTH;
		private float fadeOut = 0;
		
		
		
		/**
		 * The class constructor.
		 * @param x
		 * @param y
		 * @param team
		 * @author KockaAdmiralac
		 */
		public Barrack(float x, float y, byte team) {
			this.x = x;
			this.y = y;
			this.team = team;
		}
		
		
		
		/**
		 * The method called on frame update.
		 * @author KockaAdmiralac
		 */
		public void update(){
			step++;
			if(step == SUN_STEP){
				step = 0;
				soldiers.add(new Soldier(x + (rnd.nextFloat() * 40) - 20f, y + (rnd.nextFloat() * 40) - 20f, 1, -1, team));
			}
			if(health <= 0){
				health = MAX_HEALTH;
				team = 1;
			}
		}
		
		
		
		/**
		 * The method called when drawing the sun.
		 * @param batch
		 */
		public void draw(SpriteBatch batch){
			batch.draw(team == 0 ? barracksTexture : enemyBarracksTexture, x, y);
			if(health != MAX_HEALTH){
				font.setColor(team == 0 ? 255 : 0, team == 1 ? 255 : 0, 0, fadeOut);
				font.draw(batch, String.valueOf(health), x, y);
			}
			else if(fadeOut > 0){
				fadeOut -= 0.01f;
				font.setColor(team == 0 ? 255 : 0, team == 1 ? 255 : 0, 0, fadeOut);
				font.draw(batch, String.valueOf(health), x, y);
			}
		}
		
		
		
		/**
		 * The method called when barracks change team.
		 * @todo Add change team animation
		 * @param team
		 * @author KockaAdmiralac
		 */
		public void changeTeam(byte team){
			this.team = team;
		}
		
		
		
		/**
		 * The method called when an enemy soldier attacked your barrack.
		 * @todo Implement randomization
		 * @todo Add attack animation
		 * @author KockaAdmiralac
		 */
		public void attack(){
			health -= ATTACK_DAMAGE;
			fadeOut = 1;
		}
		
		
		
		/**
		 * The method called when your soldier healed your barrack.
		 * @todo Implement randomization
		 * @todo Add heal animation
		 * @author KockaAdmiralac
		 */
		public void heal(){
			health += HEAL_REGENERATION;
			fadeOut = 1;
		}
		
		
		
	}
	
	
	
	/**
	 * The Pytagorian theorem method.
	 * @param num1
	 * @param num2
	 * @return float
	 * @author KockaAdmiralac
	 */
	private float pitagora(float num1, float num2){ return (float)Math.sqrt((double)(num1*num1 + num2*num2)); }
	
	
	
	/**
	 * The distance formula, using pitagora method
	 * @param sola
	 * @param solb
	 * @return float
	 * @author Akimil
	*/
	private float dist(final Soldier sola, final Soldier solb){return (float)pitagora(sola.x - solb.x, sola.y - solb.y);}
	
}