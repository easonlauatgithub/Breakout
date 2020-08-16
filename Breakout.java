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
	private static final int PADDLE_Y_OFFSET = 30;

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
		setUpBricks();
		setUpPaddle();
		setUpBall();		

		while(RemainTurns > 0 && RemainBricks > 0){
			pause(speed);
			ballMoving();
			ballCollideWithBorder();
			ballCollideWithBottom();
			ballCollideWithObject();
		}
		endGame();
	}
	
	private int speed = 10;
	
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
	
	//About Ball
	private void ballCollideWithObject(){
		collider = getCollidingObject();
		if(collider != null){
			//println("collider:"+obj+" at("+x+","+y+")");
			println("collider:"+collider);

			if(collider.equals(paddle)){
				vy = -vy;
				//Prevent ball from gluing to paddle
				ball.setLocation(ball.getX(), ball.getY()-PADDLE_HEIGHT-2*BALL_RADIUS);
			}else{ //collide with bricks
				remove(collider);
				RemainBricks--;
				vy = -vy;
			}			
		}
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
		return obj;
	}
	
	private void ballCollideWithBorder(){
		double x = ball.getX();
		double y = ball.getY();
		if(x<0 || x>WIDTH-2*BALL_RADIUS){ //hit right, left border, bounce back
			vx = -vx;
		}
		if(y<0){ //hit top border, bounce back
			vy = -vy;
		}
	}
	
	private void ballCollideWithBottom(){
		double y = ball.getY();
		if(y>HEIGHT-2*BALL_RADIUS){ //hit down border, reduce one turn
			//vy = -vy;			
			RemainTurns--;
			remove(ball);
			setUpBall();
		}
	}
	
	private void ballMoving(){
		double x = ball.getX()+vx;
		double y = ball.getY()+vy;
		ball.setLocation(x, y);
	}
	
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
