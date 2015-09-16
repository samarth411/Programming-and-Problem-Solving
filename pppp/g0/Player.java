package pppp.g0;

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

	// generate point after negating or swapping coordinates
	private static Point point(double x, double y,
	                           boolean neg_y, boolean swap_xy)
	{
		if (neg_y) y = -y;
		return swap_xy ? new Point(y, x) : new Point(x, y);
	}

	private static double distance(Point a, Point b)
	{
		double x = a.x-b.x;
		double y = a.y-b.y;
		return Math.sqrt(x * x + y * y);
	}

	private Point[] nearest_neighbor(Point[][] pipers)
	{
		//keeps track of which pipers still need a nearest neighbor assignment
		Point neighbors = new Point[pipers[id].length];
		HashSet<Integer> pipers_remaining = new HashSet<Integer>();
		for(int i=0; i<pipers[id].length; ++i)
		{
			pipers_remaining.add(i);
		}

		for(int i=0; i<pipers[id].length; ++i)
		{
			if(!pipers_remaining.contains(i))
			{
				continue;
			}
			ArrayList<Integer> companions = new ArrayList<Integer>();

			double min_dist = Double.MAX_VALUE;
			int neighbor = -1;
			
			for(int j=0; j<pipers[id].length; j++)
			{
				if(!pipers_remaining.contains(i))
				{
					continue;
				}
				if (Math.abs(pipers[id][i].x - pipers[id][j].x) < 0.000001 &&
			    Math.abs(pipers[id][i].y - pipers[id][j].y) < 0.000001)
				{
					companions.add(j);
					pipers_remaining.remove(i);
					continue;
				}
				double dist = distance(pipers[id][i], pipers[id][j]);
				if(dist<min_dist)
				{
					min_dist = dist;
					neighbor = j;
				}

			}
			//if odd number of pipers, one left without a piar, just sent it to closest other piper
			if(neighbor == -1)
			{
				for(int j=0; j<pipers[id].length; j++)
				{
					double dist = distance(pipers[id][i], pipers[id][j]);
					if(dist<min_dist)
					{
						min_dist = dist;
						neighbor = j;
					}

				}
			}
			neighbors[i] = pipers[id][neighbor];
			neighbors[neighbor] = pipers[id][i];
			for(Integer k : companions)
			{
				neighbors[k] = pipers[id][neighbor];
			}
			pipers_remaining.remove(i);
			pipers_remaining.remove(neighbor);
		}
		return neighbors;
	}

	//return true if all pipers within a certain radius of eachother
	//shoudl check before checking for nearest neighbors
	private boolean pipers_together(double radius, Point[][] pipers)
	{
		for (int i=0; i<pipers[id].length; ++i)
		{
			for(int j=i; j<pipers[id].length; ++j)
			{
				if(distance(pipers[i][i], pipers[id][j])>radius)
				{
					return false;
				}
			}
		}
		return true;
	}

	// specify location that the player will alternate between
	public void init(int id, int side, long turns,
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
		boolean pipers_clustered = pipers_together(.1,pipers);
		Point[] next;
		if(!pipers_clustered)
		 {
		 	next = nearest_neighbor(pipers);
		 }
		 else
		 {
		 	next = null;
		 }
		for (int p = 0 ; p != pipers[id].length ; ++p) {
			Point src = pipers[id][p];
			Point dst = pos[p][pos_index[p]];
			if(pos_index[p]>1 && !pipers_clustered)
			{
				dst = next[p];
			}
			// if null then get random position
			if (dst == null) dst = random_pos[p];
			// if position is reached
			if (Math.abs(src.x - dst.x) < 0.000001 &&
			    Math.abs(src.y - dst.y) < 0.000001) {
				// discard random position
				if (dst == random_pos[p]) random_pos[p] = null;
				// get next position
				if (++pos_index[p] == pos[p].length) pos_index[p] = 0;
				dst = pos[p][pos_index[p]];
				// generate a new position if random
				if (dst == null) {
					double x = (gen.nextDouble() - 0.5) * side * 0.9;
					double y = (gen.nextDouble() - 0.5) * side * 0.9;
					random_pos[p] = dst = new Point(x, y);
				}
			}
			// get move towards position
			moves[p] = move(src, dst, pos_index[p] > 1);
		}
	}
}
