package map;

public class Point implements Comparable<Point> {
	private int x;
	private int y;
	private int g_score; // 到出发点的距离
	private int f_score; // 到出发点的距离 + 到目标的距离
	private Point father; // 父节点

	private final static int INF = 1000;

	public Point(int x, int y) {
		super();
		this.x = x;
		this.y = y;

		g_score = INF; // 2距离初始化为无穷
		f_score = INF;
		father = null; // 父节点初始化为null
	}

	// 设置g和f的值
	public void setG_scoreAndF_score(int g_score, int distance) {
		this.g_score = g_score;
		this.f_score = g_score + distance;
	}

	public int getG_score() {
		return g_score;
	}

	public int getF_score() {
		return f_score;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setFather(Point father) {
		this.father = father;
	}

	public Point getFather() {
		return father;
	}

	public void setG_score(int g_score) {
		this.g_score = g_score;
	}

	public void setF_score(int f_score) {
		this.f_score = f_score;
	}
	
	public void infPoint() {
		setG_score(INF);
		setF_score(INF);
	}

	@Override
	public boolean equals(Object obj) {
		return ((Point) obj).x == x && ((Point) obj).y == y; // 只有坐标相同时，才相等
	}

	@Override
	public int compareTo(Point o) {
		if (f_score > o.f_score)
			return 1;
		else if (f_score == o.f_score)
			return 0;
		else
			return -1;
	}
	
	@Override
	public String toString() {
		return "点(" + y + ", " + x + ")"; 
	}

}
