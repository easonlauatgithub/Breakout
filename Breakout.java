/*
 * File: Breakout.java
 * -------------------
 * Name:
 * Section Leader:
 * 
 * This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 100;

/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

/** Separation between bricks */
	private static final int BRICK_SEP = 4;

/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

/** Number of turns */
	private static final int NTURNS = 3;

/* Method: run() */
/** Runs the Breakout program. */
	public void run() {
		/* You fill this in, along with any subsidiary methods */
		//Setup
		setUpBricks();
		setUpPaddle();
		setUpBall();
		setUpScoreLabel();
		gameSpeed = 10;
		isPlaying = true;
		//Start to play
		while(isPlaying){
			pause(gameSpeed);
			ballKeepMoving();
			ballCollideWithBorder();
			ballCollideWithObject();
			updateScore();
			isPlaying = RemainTurns > 0 && RemainBricks > 0;
		}
		//Win or lose
		endGame();
	}
	
	private Boolean isPlaying;
	
	private int gameSpeed;
	
	private double xCollision, yCollision;
	
	private int counterPadelHit = 0;
	
	private int score = 0;
	
	private GLabel labelScore;
	
	private AudioClip bounceClip = MediaTools.loadAudioClip("bounce.au");
	
	private int RemainTurns = NTURNS;
	
	private int RemainBricks;
	
	private GObject collider;
	
	private GOval ball;
	
	private double vx, vy;
	
	private GRect paddle;

	RandomGenerator rgen = RandomGenerator.getInstance();

	private void endGame(){
		remove(ball);
		GLabel glabel;
		if(RemainTurns<=0){ //lose
			glabel = new GLabel("Game Over"); 
		}else if(RemainBricks<=0){ //win
			glabel = new GLabel("You won");
		}else{
			glabel = new GLabel("What happen??");
		}
		glabel.setLocation(WIDTH/2 - glabel.getWidth()/2, HEIGHT/2 - glabel.getHeight()/2);
		add(glabel);
	}
	
	//About Score	
	private void setUpScoreLabel(){
		labelScore = new GLabel("Score:"+score);
		labelScore.setLocation(WIDTH/2 - labelScore.getWidth()/2, HEIGHT - 0*labelScore.getHeight());
		add(labelScore);		
	}
	
	private void updateScore(){
		if(collider != null){
			Color color = collider.getColor();
			int colorCode = color.hashCode();
			switch(colorCode){
				case -65536://RED
					score += 25;
					break;
				case -14336://ORANGE
					score += 20;
					break;
				case -256://YELLOW
					score += 15;
					break;
				case -16711681://green
					score += 10;
					break;
				case -16711936://cyan
					score += 5;
					break;
				default://hit paddle
					score += 0;
			}
			labelScore.setLabel("Score:"+score);			
		}
	}
	
	//About Ball Collision
	private void ballCollisionSound(){
		bounceClip.play(); 
	}
	
	private void ballCollideWithObject(){
		collider = getCollidingObject();
		if(collider != null){
			ballCollisionSound();
			if(collider.equals(paddle)){
				ballCollideWithPaddle();
			}else if(collider.equals(labelScore)){
				//not doing anything
			}else{ //collide with bricks
				ballCollideWithBrick();
			}
		}
	}

	private void ballCollideWithPaddle() {
		vy = -vy;
		//Prevent ball from gluing to paddle
		//i.e. Move the ball up by the length of ball diameter and paddle height
		ball.setLocation(ball.getX(), ball.getY()-PADDLE_HEIGHT-2*BALL_RADIUS);
		//double vx if hit by the sides of paddle
		//i.e. collision point y coordinate is between upper and lower bounds of paddle
		double yPaddleTopAdjustment = 3;
		double yPaddleTop = HEIGHT - PADDLE_Y_OFFSET + yPaddleTopAdjustment;
		double yPaddleBottom = HEIGHT - PADDLE_Y_OFFSET + PADDLE_HEIGHT;
		if (yCollision>yPaddleTop && yCollision<yPaddleBottom){
			vx = -vx;
		}
		//double vx after seventh hit
		counterPadelHit++;
		if(counterPadelHit==7){
			vx = vx * 2;
		}
	}

	private void ballCollideWithBrick() {
		remove(collider);
		vy = -vy;
		RemainBricks--;		
	}
	
	/** Return null or collided object, i.e. brick or paddle */
	
	private GObject getCollidingObject(){
		GObject obj = null;
		double x = 0, y = 0;
		if(obj == null){ 
			x = ball.getX();
			y = ball.getY();
			obj = getElementAt(x, y); 
		}
		if(obj == null){ 
			x = ball.getX()+2*BALL_RADIUS;
			y = ball.getY();
			obj = getElementAt(x, y); 
		}
		if(obj == null){ 
			x = ball.getX();
			y = ball.getY()+2*BALL_RADIUS;
			obj = getElementAt(x, y); 
		}
		if(obj == null){ 
			x = ball.getX()+2*BALL_RADIUS;
			y = ball.getY()+2*BALL_RADIUS;
			obj = getElementAt(x, y); 
		}
		getCollisionPoint(x,y);
		return obj;
	}
	
	private void getCollisionPoint(double x, double y){
		xCollision = x; 
		yCollision = y;
	}
	
	
	private void ballCollideWithBorder(){
		double x = ball.getX();
		double y = ball.getY();
		if(x<0 || x>WIDTH-2*BALL_RADIUS){ //hit right, left border, bounce back
			ballCollisionSound();
			vx = -vx;
		}
		if(y<0){ //hit top border, bounce back
			ballCollisionSound();
			vy = -vy;
		}
		if(y>HEIGHT-2*BALL_RADIUS){ //hit down border, reduce one turn
			ballCollideWithBottom();
		}
	}
	
	private void ballCollideWithBottom(){
		//vy = -vy;			
		RemainTurns--;
		remove(ball);
		setUpBall();
	}
	
	//About Ball
	private void setUpBall(){
		double xPos = WIDTH /2 - BALL_RADIUS;
		double yPos = HEIGHT /2 - BALL_RADIUS;
		//double yPos = 0;
		ball = new GOval(xPos, yPos, BALL_RADIUS*2, BALL_RADIUS*2);
		ball.setFilled(true);
		add(ball);
		
		vy = 3;
		//y values in Java increase as you move down the screen)
		vx = rgen.nextDouble(1.0, 3.0);
		if (rgen.nextBoolean(0.5)) vx = -vx;
		//nextDouble(-3.0, +3.0) which might generate -1~1, a ball going more or less straight down
	}
	
	private void ballKeepMoving(){
		ball.move(vx, vy);
	}
	
	//About Paddle
	private void setUpPaddle() {
		double yPos = HEIGHT - PADDLE_Y_OFFSET;
		double xPos = WIDTH/2-PADDLE_WIDTH/2;
		paddle = new GRect(xPos, yPos, PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setFilled(true);
		paddle.setColor(Color.BLACK);
		add(paddle);
		addMouseListeners();
	}
	
	public void mouseDragged(MouseEvent e){
		double yPos = HEIGHT - PADDLE_Y_OFFSET;
		double xPos = e.getX();
		if(xPos<0){ //left
			paddle.setLocation(0, yPos);
		}else if(xPos>WIDTH-PADDLE_WIDTH){ //right
			paddle.setLocation(WIDTH-PADDLE_WIDTH, yPos);
		}else{
			paddle.setLocation(e.getX(), yPos);			
		}
	}
	
	//About Bricks
	private void setUpBricks() {
		RemainBricks = NBRICK_ROWS * NBRICKS_PER_ROW;
		for(int c=0; c<NBRICKS_PER_ROW; c++){ //column
			double yPos = BRICK_Y_OFFSET + (BRICK_HEIGHT + BRICK_SEP)*c ;
			Color color = assignColorByColumn(c);
			for(int r=0; r<NBRICK_ROWS; r++){ //row
				double xPos = BRICK_SEP + (BRICK_WIDTH + BRICK_SEP)*r;
				GRect rect = new GRect(xPos, yPos, BRICK_WIDTH, BRICK_HEIGHT);
				rect.setFilled(true);
				rect.setColor(color);
				add(rect);
			}
		}
	}
		
	public Color assignColorByColumn(int column){
		Color color = Color.BLACK;
		switch(column/2){
			case 0: color = Color.RED; break;
			case 1: color = Color.ORANGE; break;
			case 2: color = Color.YELLOW; break;
			case 3: color = Color.GREEN; break;
			case 4: color = Color.CYAN; break;
			default: color = Color.BLUE; break;
		}
		return color;
	}

}
