package pppp.g1;

import pppp.sim.Point;
import pppp.sim.Move;

import java.util.*;

public class Player implements pppp.sim.Player {

	// see details below
	private int id = -1;
	private int side = 0;
	private int[] pos_index = null;
	private Point[][] pos = null;
	private Point[] random_pos = null;
	private Random gen = new Random();

	// create move towards specified destination
	private static Move move(Point src, Point dst, boolean play)
	{
		double dx = dst.x - src.x;
		double dy = dst.y - src.y;
		double length = Math.sqrt(dx * dx + dy * dy);
		double limit = play ? 0.1 : 0.5;
		if (length > limit) {
			dx = (dx * limit) / length;
			dy = (dy * limit) / length;
		}
		return new Move(dx, dy, play);
	}
	
	private static Move moveSlowly(Point src, Point dst, boolean play)
	{
		double dx = dst.x - src.x;
		double dy = dst.y - src.y;
		double length = Math.sqrt(dx * dx + dy * dy);
		double limit = play ? 0.1 : 0.5;
		if (length > limit) {
			dx = (dx * limit) / length;
			dy = (dy * limit) / length;
		}
		return new Move(dx, dy, play);
	}
	
	
	private static double getDistance(Point a, Point b)
	{
		double x = a.x-b.x;
		double y = a.y-b.y;
		return Math.sqrt(x * x + y * y);
	}

	// generate point after negating or swapping coordinates
	private static Point point(double x, double y,
	                           boolean neg_y, boolean swap_xy)
	{
		if (neg_y) y = -y;
		return swap_xy ? new Point(y, x) : new Point(x, y);
	}

	// specify location that the player will alternate between
	// Init for semi circle sweeping .. 
	public void init(int id, int side, long turns,
	                 Point[][] pipers, Point[] rats)
	{
		this.id = id;
		this.side = side;
		int n_pipers = pipers[id].length;
		pos = new Point [n_pipers][6];
		random_pos = new Point [n_pipers];
		pos_index = new int [n_pipers];
		for (int p = 0 ; p != n_pipers ; ++p) {
			// spread out at the door level
			double door = 0.0;
			//if (n_pipers != 1) door = p * 1.8 / (n_pipers - 1) - 0.9;
			
			if (n_pipers != 1) door = -0.95;
			
			// pick coordinate based on where the player is
			boolean neg_y = id == 2 || id == 3;
			boolean swap  = id == 1 || id == 3;
			// first and third position is at the door
			pos[p][0] = pos[p][3] = point(door, side * 0.5, neg_y, swap);
			
			// Set the second position 
			double xCoordinate = (p * 0.4 / (n_pipers - 1) - 0.2) * side;
			double yCoordinate = -0.1*side;
			
			pos[p][1] = point(xCoordinate, yCoordinate, neg_y, swap);
			
			pos[p][2] = point(0, 0.40*side, neg_y, swap);
			
			// second position is chosen randomly in the rat moving area
//			pos[p][1] = null;
			// fourth and fifth positions are outside the rat moving area
			pos[p][4] = point(door * -6, side * 0.5 + 3, neg_y, swap);
			pos[p][5] = point(door * +6, side * 0.5 + 3, neg_y, swap);
			
			
			// start with first position
			pos_index[p] = 0;
		}
	}
	
	
    
	// specify location that the player will alternate between
		public void init2(int id, int side, long turns,
		                 Point[][] pipers, Point[] rats)
		{
			this.id = id;
			this.side = side;
			int n_pipers = pipers[id].length;
			pos = new Point [n_pipers][5];
			random_pos = new Point [n_pipers];
			pos_index = new int [n_pipers];
			for (int p = 0 ; p != n_pipers ; ++p) {
				// spread out at the door level
				double door = 0.0;
				if (n_pipers != 1) door = p * 1.8 / (n_pipers - 1) - 0.9;
				// pick coordinate based on where the player is
				boolean neg_y = id == 2 || id == 3;
				boolean swap  = id == 1 || id == 3;
				// first and third position is at the door
				pos[p][0] = pos[p][2] = point(door, side * 0.5, neg_y, swap);
				// second position is chosen randomly in the rat moving area
				pos[p][1] = null;
				// fourth and fifth positions are outside the rat moving area
				pos[p][3] = point(door * -6, side * 0.5 + 3, neg_y, swap);
				pos[p][4] = point(door * +6, side * 0.5 + 3, neg_y, swap);
				// start with first position
				pos_index[p] = 0;
			}
		}
	
	
		
		
	// return next locations on last argument
	public void play(Point[][] pipers, boolean[][] pipers_played,
	                 Point[] rats, Move[] moves)
	{
		for (int p = 0 ; p != pipers[id].length ; ++p) {
			Point src = pipers[id][p];
			Point dst = pos[p][pos_index[p]];
			
			// if null then get random position
			//if (dst == null) dst = random_pos[p];

			// if position is reached
			if (Math.abs(src.x - dst.x) < 0.000001 &&
			    Math.abs(src.y - dst.y) < 0.000001) {
				// discard random position
				//if (dst == random_pos[p]) random_pos[p] = null;
				// get next position
				if (++pos_index[p] == pos[p].length) pos_index[p] = 0;
				dst = pos[p][pos_index[p]];
				// generate a new position if random
				/*if (dst == null) {
					double x = (gen.nextDouble() - 0.5) * side * 0.9;
					double y = (gen.nextDouble() - 0.5) * side * 0.9;
					random_pos[p] = dst = new Point(x, y);
				} */
			}
			// get move towards position
			if (pos_index[p] == 3 || pos_index[p] == 2)
			{
				moves[p] = moveSlowly(src, dst, pos_index[p] > 1);
			}
			else
			{
				moves[p] = move(src, dst, pos_index[p] > 1);
			}
			
		}
	}

	// This method is follows greedy approach : 
	// The pipers go to the nearest rat , start playing the pipe and come back 
	public void play2(Point[][] pipers, boolean[][] pipers_played,
	                 Point[] rats, Move[] moves)
	{	
		
		boolean teamPlaying = false;
		for (int p = 0 ; p != pipers[id].length ; ++p) {
			boolean playPiper = false;
			boolean piperStationary = false;
			double minDistance = Double.MAX_VALUE;
			Point src = pipers[id][p];	
			double distance = 0.0;
			//Point dst = null;
			Point dst = pos[p][pos_index[p]];
			// if null then get random position
			//if (dst == null) dst = random_pos[p];
			
			//Calculate if piper should be played or not
			double minX = 0.0;
			double minY = 0.0;
			Point minRat = null;
			for (int i =0; i<rats.length; i++)
			{
				distance = getDistance(src, rats[i]);
				
				if (distance < minDistance)
				{
					minRat = rats[i];
					minX = minRat.x;
					minY = minRat.y;
					minDistance = distance;
				}
			}
			if (minDistance < 3)
			{
				playPiper = true;
				teamPlaying = true;
			}
			
			//If distance between piper and gate is less than 2m, stop the piper
			distance = getDistance(src, pos[p][0]);
			if (distance < 5)
			{
				for (int temp = 0 ; temp < p ; ++temp)
				{
					Point mySrc = pipers[id][temp];
					//If distance between piper and gate is less than 2m, stop the piper
					distance = getDistance(mySrc, pos[p][0]); 
					if (distance < 6  && teamPlaying == true)
					{
						//System.out.println("Reached here at least ");
						//if (pipers_played[id][temp] == true)
						//{
							//System.out.println("Reached here man ");
							playPiper = false;
							piperStationary = true;
							break;
						//}
					}
				}
			} 
			if (pos_index[p] == 1)
			{	
				// If the minimum distance is less than 10, it means that rat is within the piper's range,
				// Now it will get automatically attracted towards the piper
				if (minDistance < 3)
				{
					//Increment pos index
					pos_index[p] = pos_index[p] + 1;
					dst = pos[p][pos_index[p]];
				}
				else
				{
					// Continue looking for the rat unless one rat is found
					// Sweeping
					// Update the destination
					dst = new Point(minX, minY);
				}
			}
			else
			{
				if (Math.abs(src.x - dst.x) < 0.000001 && Math.abs(src.y - dst.y) < 0.000001) {
					// get next position
					if (++pos_index[p] == pos[p].length) pos_index[p] = 0;
					dst = pos[p][pos_index[p]];
					
					if (pos_index[p] == 1)
					{
						dst = new Point(minX, minY);
					}
				}
				if (piperStationary == true)
				{
					dst = new Point(src.x, src.y);
				}
				
			}
			// If the position is reached,  get move towards position
			//moves[p] = move(src, dst, pos_index[p] > 1);
			if (src == null)
			{
				System.out.println("Source is null ");
			}
			if (dst == null)
			{
				System.out.println("Destination is null ");
			}
			
			moves[p] = move(src, dst, playPiper);
			//moves[p] = move(src, dst, true);
		}
	}
}
