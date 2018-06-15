package demo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import map.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import cmd.Action;
import cmd.RoundAction;

public class Client {
	private int team_id = 0;
	private String team_name = "";
//	private Team self = null;
//	private Team enemy = null;
	private int roundId = 0;

	private List<Player> players = new ArrayList<Player>();

	private List<Point> enemyPlayers = new ArrayList<>(); // 存储所有的敌人，用于寻找最近的目标

	private List<Point> coins = new ArrayList<>(); // 存储金币

	private List<Point> stars = new ArrayList<>(); // 存储道具

	private String[][] myMap; // 地图

	private int width; // 地图的宽高
	private int height;
	private final int INF = 1000; // 定义无穷，用于初始化最小值

	private final int MIN_BULLET_DIS = 4; // 默认子弹最大搜索距离
	private final int MIN_ENEMY_DIS = 3; // 默认坦克搜索最大距离

	private final int SERACH_RANGE = 4; // 默认搜索距离

	private Point[][] points; // 存储用于aStar的Point二维表，用于Astar寻路

	final int[][] DIR = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } }; // 上下左右，第一个是y，第二个是x

	final String[] dirs = { "up", "down", "left", "right" };

	public Client(int team_id, String team_name) {
		this.team_id = team_id;
		this.team_name = team_name;
	}

	public void legStart(JSONObject data) {
		System.out.println("leg start");

		JSONObject map = data.getJSONObject("map");

		width = map.getInt("width"); // 存储地图的高宽
		height = map.getInt("height");

		points = initePoints(height, width); // 初始化points，将每个位置上都放一个Point

		myMap = new String[width][height];

		System.out.printf("map width:%d, map height %d\n", width, height);

		JSONArray teams = data.getJSONArray("teams");

		for (int i = 0; i < 2; i++) {
			JSONObject team = teams.getJSONObject(i);
			int team_id = team.getInt("id");
			if (this.team_id == team_id) {
				System.out.println("self team");
			}
			else {
				System.out.println("enemy team");
			}
		}
	}

	public void legEnd(JSONObject data) {
		System.out.println("leg end");
		JSONArray results = data.getJSONArray("teams");
		for (int i = 0; i < results.size(); i++) {
			Result result = new Result(results.getJSONObject(i));
		}
	}

	public void round(JSONObject data) {

		for (int i = 0; i < myMap.length; i++)// 初始化地图2维数组
			for (int j = 0; j < myMap[i].length; j++)
				myMap[i][j] = "+";

		this.roundId = data.getInt("round_id");
		System.out.printf("round %d\n", this.roundId);

		JSONArray brickWalls = data.getJSONArray("brick_walls");
		for (int i = 0; i < brickWalls.size(); i++) {
			JSONObject object = brickWalls.getJSONObject(i);
			myMap[object.getInt("y")][object.getInt("x")] = "$";
		}

		JSONArray ironWalls = data.getJSONArray("iron_walls");
		for (int i = 0; i < ironWalls.size(); i++) {
			JSONObject object = ironWalls.getJSONObject(i);
			myMap[object.getInt("y")][object.getInt("x")] = "#";
		}

		JSONArray rivers = data.getJSONArray("river");
		for (int i = 0; i < rivers.size(); i++) {
			JSONObject object = rivers.getJSONObject(i);
			myMap[object.getInt("y")][object.getInt("x")] = "@";
		}

		this.coins.clear(); // 清空之前的数据
		JSONArray coins = data.getJSONArray("coins");
		for (int i = 0; i < coins.size(); i++) {
			JSONObject object = coins.getJSONObject(i);
			this.coins.add(new Point(object.getInt("x"), object.getInt("y")));// 将金币和道具看做一样的一起存起来
			myMap[object.getInt("y")][object.getInt("x")] = "c";
		}

		this.stars.clear();// 清空之前的数据
		JSONArray stars = data.getJSONArray("stars");
		for (int i = 0; i < stars.size(); i++) {
			JSONObject object = stars.getJSONObject(i);
			this.stars.add(new Point(object.getInt("x"), object.getInt("y")));// 将金币和道具看做一样的一起存起来
			myMap[object.getInt("y")][object.getInt("x")] = "*";
		}

		JSONArray bullets = data.getJSONArray("bullets");
		for (int i = 0; i < bullets.size(); i++) {
			JSONObject object = bullets.getJSONObject(i);

			myMap[object.getInt("y")][object.getInt("x")] = "b_" + object.getInt("type") + "_"
					+ object.getString("direction");

			// 一个子弹的完整的字符串表示为b_1(0)_up_s(e)

			if (object.getInt("team") == team_id) // 给每个子弹也加上队伍的标识
				myMap[object.getInt("y")][object.getInt("x")] += "_s";
			else
				myMap[object.getInt("y")][object.getInt("x")] += "_e";
		}

		this.players.clear();
		enemyPlayers.clear();
		JSONArray players = data.getJSONArray("players");
		for (int i = 0; i < players.size(); i++) {
			JSONObject object = players.getJSONObject(i);
			Player player = new Player(object);

			// 一个坦克的完整表示为s(e)_1(0)

			if (player.getTeam() == this.team_id) {
				this.players.add(player);
				myMap[object.getInt("y")][object.getInt("x")] = "s_" + object.getInt("super_bullet");
			}
			else {
				enemyPlayers.add(player);
				myMap[object.getInt("y")][object.getInt("x")] = "e_" + object.getInt("super_bullet");
			}
		}
	}

	public RoundAction act() {

		printMap();// 打印地图

		List<Action> actions = new ArrayList<Action>();

		for (Player player : players) {

			player.setGoal(findNearestGoal(player)); // 给每个player设置目标

			String move = moveDir(player);
			String fdAndsb = fireDirAndSb(player);

			String fire;
			int superBullet;

			if (fdAndsb == null) {
				if (move == null)
					fire = randomDir();
				else
					fire = move;
				superBullet = 0;
			}
			else {
				fire = fdAndsb.substring(0, fdAndsb.length() - 1);
				superBullet = Integer.parseInt(fdAndsb.charAt(fdAndsb.length() - 1) + "");
			}

			System.out.println(player + "的目标是" + player.getGoal() + "，它移动的方向是" + move + "，它开火的方向是" + fire);

			actions.add(new Action(player.getTeam(), player.getId(), superBullet, move, fire));
		}

		RoundAction roundAction = new RoundAction(this.roundId, actions);

		return roundAction;
	}

	// 移动方向，必须要返回一个确定方向
	private String moveDir(Player player) {
		int x_p = player.getX(); // 获得坦克的x，y
		int y_p = player.getY();

		String ret = null;

		Point next = aStarDir(player); // 获得下一个移动到的点

		int size = stars.size() + enemyPlayers.size() + coins.size();

//		// 如果当前目标不可达，就从列表内删除该目标，同时更新目标
//		for (int i = 0; i < size && ret.equals("no"); i++) {
//			System.out.print("删除" + player.getGoal());
//
//			if (myMap[player.getGoal().getY()][player.getGoal().getY()].equals("*")) // 发现不可到达的点，判断目标点的来源，删除，避免后面的坦克搜索他
//				delGoal(player.getGoal(), stars);
//			else if (myMap[player.getGoal().getY()][player.getGoal().getY()].equals("c"))
//				delGoal(player.getGoal(), coins);
//			else
//				delGoal(player.getGoal(), enemyPlayers);
//
//			player.setGoal(findNearestGoal(player)); // 更新目标
//
//			ret = aStarDir(player);
//		}

		for (int i = 0; i < size && next == null; i++) { // 一直循环到获得一个可达的目标或者所有的点都遍历完了
			System.out.print("删除" + player.getGoal());

			if (myMap[player.getGoal().getY()][player.getGoal().getY()].equals("*")) // 发现不可到达的点，判断目标点的来源，删除，避免后面的坦克搜索他
				delGoal(player.getGoal(), stars);
			else if (myMap[player.getGoal().getY()][player.getGoal().getY()].equals("c"))
				delGoal(player.getGoal(), coins);
			else
				delGoal(player.getGoal(), enemyPlayers);

			player.setGoal(findNearestGoal(player)); // 更新目标

			next = aStarDir(player); // 重新Astar寻路
		}

		ret = getNextDir(player, next);

		if (ret == null) { // 如果所有的目标都不可达，遍历上下左右找到一个可行的方向
			String[] dirs = { "up", "down", "left", "right" };
			for (int i = 0; i < DIR.length; i++) {
				int y = y_p + DIR[i][0]; // 获得上下左右方向的x，y
				int x = x_p + DIR[i][1];
				if (x < 0 || x >= width || y < 0 || y >= height) // 越界跳过
					continue;
				else {
					ret = dirs[i];
					break;
				}
			}
		}
		
//		// 搜索子弹
//		for (int i = x_p - 1; i >= x_p - MIN_BULLET_DIS && ret != null && ret.equals("up") && i >= 0; i--) {
//			if (isObstacle(myMap[y_p - 1][i]))
//				break;
//			if (myMap[y_p - 1][i].equals("b_right_e"))
//				ret = null;
//		}
//		for (int i = x_p + 1; i <= x_p + MIN_BULLET_DIS && ret != null && ret.equals("up") && i < width; i++) {
//			if (isObstacle(myMap[y_p - 1][i]))
//				break;
//			if (myMap[y_p - 1][i].equals("b_left_e"))
//				ret = null;
//		}
//
//		for (int i = x_p - 1; i >= x_p - MIN_BULLET_DIS && ret != null && ret.equals("down") && i >= 0; i--) {
//			if (isObstacle(myMap[y_p + 1][i]))
//				break;
//			if (myMap[y_p + 1][i].equals("b_right_e"))
//				ret = null;
//		}
//		for (int i = x_p + 1; i <= x_p + MIN_BULLET_DIS && ret != null && ret.equals("down") && i < width; i++) {
//			if (isObstacle(myMap[y_p + 1][i]))
//				break;
//			if (myMap[y_p + 1][i].equals("b_left_e"))
//				ret = null;
//		}
//
//		for (int i = y_p - 1; i >= y_p - MIN_BULLET_DIS && ret != null && ret.equals("left") && i >= 0; i--) {
//			if (isObstacle(myMap[i][x_p - 1]))
//				break;
//			if (myMap[i][x_p - 1].equals("b_down_e"))
//				ret = null;
//		}
//		for (int i = y_p + 1; i <= y_p + MIN_BULLET_DIS && ret != null && ret.equals("left") && i < height; i++) {
//			if (isObstacle(myMap[i][x_p - 1]))
//				break;
//			if (myMap[i][x_p - 1].equals("b_up_e"))
//				ret = null;
//		}
//
//		for (int i = y_p - 1; i >= y_p - MIN_BULLET_DIS && ret != null && ret.equals("right") && i >= 0; i--) {
//			if (isObstacle(myMap[i][x_p + 1]))
//				break;
//			if (myMap[i][x_p + 1].equals("b_down_e"))
//				ret = null;
//		}
//		for (int i = y_p + 1; i <= y_p + MIN_BULLET_DIS && ret != null && ret.equals("right") && i < height; i++) {
//			if (isObstacle(myMap[i][x_p + 1]))
//				break;
//			if (myMap[i][x_p + 1].equals("b_up_e"))
//				ret = null;
//		}
//
//		if (coin_count != 0) {
//			// 搜索坦克
//			for (int i = x_p - 1; i >= x_p - MIN_ENEMY_DIS - 1 && ret != null && ret.equals("up") && i >= 0; i--) {
//				if (isObstacle(myMap[y_p - 1][i]))
//					break;
//				if (myMap[y_p - 1][i].equals("e"))
//					ret = null;
//			}
//			for (int i = x_p + 1; i <= x_p + MIN_ENEMY_DIS + 1 && ret != null && ret.equals("up") && i < width; i++) {
//				if (isObstacle(myMap[y_p - 1][i]))
//					break;
//				if (myMap[y_p - 1][i].equals("e"))
//					ret = null;
//			}
//
//			for (int i = x_p - 1; i >= x_p - MIN_ENEMY_DIS - 1 && ret != null && ret.equals("down") && i >= 0; i--) {
//				if (isObstacle(myMap[y_p + 1][i]))
//					break;
//				if (myMap[y_p + 1][i].equals("e"))
//					ret = null;
//			}
//			for (int i = x_p + 1; i <= x_p + MIN_ENEMY_DIS + 1 && ret != null && ret.equals("down") && i < width; i++) {
//				if (isObstacle(myMap[y_p + 1][i]))
//					break;
//				if (myMap[y_p + 1][i].equals("e"))
//					ret = null;
//			}
//
//			for (int i = y_p - 1; i >= y_p - MIN_ENEMY_DIS - 1 && ret != null && ret.equals("left") && i >= 0; i--) {
//				if (isObstacle(myMap[i][x_p - 1]))
//					break;
//				if (myMap[i][x_p - 1].equals("e"))
//					ret = null;
//			}
//			for (int i = y_p + 1; i <= y_p + MIN_ENEMY_DIS + 1 && ret != null && ret.equals("left")
//					&& i < height; i++) {
//				if (isObstacle(myMap[i][x_p - 1]))
//					break;
//				if (myMap[i][x_p - 1].equals("e"))
//					ret = null;
//			}
//
//			for (int i = y_p - 1; i >= y_p - MIN_ENEMY_DIS - 1 && ret != null && ret.equals("right") && i >= 0; i--) {
//				if (isObstacle(myMap[i][x_p + 1]))
//					break;
//				if (myMap[i][x_p + 1].equals("e"))
//					ret = null;
//			}
//			for (int i = y_p + 1; i <= y_p + MIN_ENEMY_DIS + 1 && ret != null && ret.equals("right")
//					&& i < height; i++) {
//				if (isObstacle(myMap[i][x_p + 1]))
//					break;
//				if (myMap[i][x_p + 1].equals("e"))
//					ret = null;
//			}
//		}

		return ret;
	}

	// 开火方向，是否发射超级子弹
	private String fireDirAndSb(Player player) {
		String ret = null;
		int superBullet = 0;
		int x = player.getX();
		int y = player.getY();
		boolean isBullet = false;

		// 先搜索紧挨的坦克，再子弹，最后坦克

		// 搜索上下左右紧邻的坦克，距离为1
		for (int i = 0; i < DIR.length && ret == null; i++) {
			int y_t = y + DIR[i][0];
			int x_t = x + DIR[i][1];

			if (y_t < 0 || y_t >= height || x_t < 0 || x_t >= width) // 越界，跳过
				continue;

			if (myMap[y_t][x_t].contains("e")) // 如果是敌人，向其开火
				ret = dirs[i];
		}

		// 以player为中心，分别向四个方向从里向外搜索子弹，如果发现障碍就跳过这个方向的搜索，否则发现子弹就向其开火
		for (int i = y - 1; i >= y - MIN_BULLET_DIS && ret == null && i >= 0; i--) {
			if (isObstacle1(myMap[i][x]))// 如果是障碍，就不管
				break;
			if (myMap[i][x].equals("b_0_down_e")) {
				ret = "up";
				isBullet = true;
			}
		}
		for (int i = y + 1; i <= y + MIN_BULLET_DIS && ret == null && i < height; i++) {
			if (isObstacle1(myMap[i][x]))
				break;
			if (myMap[i][x].equals("b_0_up_e")) {
				ret = "down";
				isBullet = true;
			}
		}
		for (int i = x - 1; i >= x - MIN_BULLET_DIS && ret == null && i >= 0; i--) {
			if (isObstacle1(myMap[y][i]))
				break;
			if (myMap[y][i].equals("b_0_right_e")) {
				ret = "left";
				isBullet = true;
			}
		}
		for (int i = x + 1; i <= x + MIN_BULLET_DIS && ret == null && i < width; i++) {
			if (isObstacle1(myMap[y][i]))
				break;
			if (myMap[y][i].equals("b_0_left_e")) {
				ret = "right";
				isBullet = true;
			}
		}

		// 以player为中心，分别向四个方向从里向外搜索敌方坦克，如果发现障碍就跳过这个方向的搜索，否则发现敌方坦克就向其开火
		// 如果有超级子弹，就缩小搜索距离
		for (int i = y - 1; i >= y - MIN_ENEMY_DIS + player.getSuperBullet() && ret == null && i >= 0; i--) {
			if (isObstacle1(myMap[i][x]))// 如果是障碍，就不管
				break;
			if (myMap[i][x].contains("e")) {
				ret = "up";
			}
		}
		for (int i = y + 1; i <= y + MIN_ENEMY_DIS - player.getSuperBullet() && ret == null && i < height; i++) {
			if (isObstacle1(myMap[i][x]))
				break;
			if (myMap[i][x].contains("e")) {
				ret = "down";
			}
		}
		for (int i = x - 1; i >= x - MIN_ENEMY_DIS + player.getSuperBullet() && ret == null && i >= 0; i--) {
			if (isObstacle1(myMap[y][i]))
				break;
			if (myMap[y][i].contains("e")) {
				ret = "left";
			}
		}
		for (int i = x + 1; i <= x + MIN_ENEMY_DIS - player.getSuperBullet() && ret == null && i < width; i++) {
			if (isObstacle1(myMap[y][i]))
				break;
			if (myMap[y][i].contains("e")) {
				ret = "right";
			}
		}

		// 如果确定向子弹和坦克发射子弹
		if (ret != null) {
			if (isBullet) { // 如果开火目标是子弹，则默认不发射超级子弹
				superBullet = 0;
			}
			else {
				superBullet = player.getSuperBullet();// 如果向坦克开火，有超级子弹就发射超级子弹
			}
			ret += superBullet;
			return ret;
		}

		return ret; // 这里返回的有可能是null

	}

	// 判断地图某元素是否是障碍(砖墙，铁墙，我方坦克)
	private boolean isObstacle(String s) {
		return s.equals("$") || s.equals("#") || s.contains("s");
	}

	// 断地图某元素是否是障碍(砖墙，铁墙)
	private boolean isObstacle1(String s) {
		return s.equals("$") || s.equals("#");
	}

	// 返回距离player最近的目标，遍历3个列表，优先级是：星星、坦克、金币
	private Point findNearestGoal(Player player) {
		int index = getIndexOfNearestGoal(player, stars);
		if (index != -1) // 如果指定范围内有目标，就返回对应的目标
			return stars.get(index);

		index = getIndexOfNearestGoal(player, enemyPlayers);
		if (index != -1)
			return enemyPlayers.get(index);

		index = getIndexOfNearestGoal(player, coins);
		if (index != -1)
			return coins.get(index);

		return null; // 一个类型的目标都没有，返回null

	}

	// 返回我方坦克和目标之间的距离
	private int distance(Point p1, Point p2) {
		return distance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}

	// 计算2点之间的距离（街区距离）
	public static int distance(int x1, int y1, int x2, int y2) {
		return Math.abs(x1 - x2) + Math.abs(y1 - y2);
	}

	private String randomDir() {
		String[] dir = { "up", "down", "left", "right" };
		return dir[new Random().nextInt(dir.length)];
	}

	// 打印地图
	private void printMap() {
		System.out.print("   ");
		for (int i = 0; i < myMap.length; i++)
			System.out.printf("%-3d", i);
		System.out.println();
		for (int i = 0; i < myMap.length; i++) {
			System.out.printf("%-3d", i);
			for (int j = 0; j < myMap[i].length; j++)
				System.out.print(myMap[i][j].charAt(0) + "  ");
			System.out.println();
		}
	}

	// 目标是否存在
	private boolean goalExist(Point goal, List<Point> list) {
		if (goal == null)
			return false;
		for (Point e : list) {
			if (e.equals(goal)) // 比较是否相等
				return true;
		}
		return false;
	}

//	// 返回距离我方所有坦克最近的敌人
//	private Point ourNearstEnemy() {
//		int min = INF;
//		int index = 0;
//		for (int i = 0; i < enemyPlayers.size(); i++) {
//			int sumDis = 0;
//			for (Player player : players)
//				sumDis += distance(player, enemyPlayers.get(i));
//			if (sumDis < min) {
//				min = sumDis;
//				index = i;
//			}
//
//		}
//		return enemyPlayers.get(index);
//	}

	// 初始化points
	private Point[][] initePoints(int height, int width) {
		Point[][] points = new Point[height][width];
		for (int y = 0; y < points.length; y++)
			for (int x = 0; x < points[y].length; x++)
				points[y][x] = new Point(x, y);
		return points;
	}

	// 将points中的每个元素的f和g设为INF
	private void infPoints() {
		for (int i = 0; i < points.length; i++)
			for (int j = 0; j < points[i].length; j++) {
				points[i][j].infPoint();
			}
	}

	// Astar寻找路径
	private Point aStarDir(Player player) {

		infPoints();

		ArrayList<Point> openSet = new ArrayList<>();
		Set<Point> closeSet = new HashSet<>();

		Point start = points[player.getY()][player.getX()];
		Point goal = points[player.getGoal().getY()][player.getGoal().getX()];

		openSet.add(start);

		while (!openSet.isEmpty()) {
			Point current = openSet.remove(indexOfMinF(openSet));
			closeSet.add(current);

			int y = current.getY();
			int x = current.getX();

			for (int i = 0; i < DIR.length; i++) {
				int y_t = y + DIR[i][0]; // 获得上下左右的点的坐标
				int x_t = x + DIR[i][1];

				if (y_t < 0 || y_t >= height || x_t < 0 || x_t >= width) // 判断是否越界
					continue;

				Point temp = points[y_t][x_t];

				if (closeSet.contains(temp)) // 如果该点已经存在于关闭列表内，跳过
					continue;
				else if (myMap[y_t][x_t].equals("#") || myMap[y_t][x_t].equals("@") || myMap[y_t][x_t].equals("s")
						|| myMap[y_t][x_t].equals("e")) // 如果该点不可越过，跳过
					continue;
				else if (openSet.contains(temp)) { // 如果该点已在开启列表内，看是否更新值
					if (current.getG_score() <= temp.getG_score()) {
						temp.setG_scoreAndF_score(current.getG_score() + 1, distance(temp, goal));
						temp.setFather(current);
					}
				}
				else { // 如果没在开启列表内，加入开启列表
					temp.setG_scoreAndF_score(current.getG_score() + 1, distance(temp, goal));
					temp.setFather(current);
					openSet.add(temp);
				}
			}

			if (openSet.contains(goal))
				break;
		}

		if (openSet.isEmpty()) { // 该player的目标不可达
			System.out.println(goal + "不可达");
//			return "no";
			return null; // 如果不可达，返回空
		}

//		return getNextDir(start, getTheNextPoint(start, goal));

		return getTheNextPoint(start, goal); // 返回将要移动到的位置的Point
	}

	// 获得开启列表内具有最小f的点的下标
	private int indexOfMinF(ArrayList<Point> openSet) {
		int index = 0;
		int min = INF;
		for (int i = 0; i < openSet.size(); i++)
			if (openSet.get(i).getF_score() < min) {
				index = i;
				min = openSet.get(i).getF_score();
			}
		return index;
	}

	// 获得下一个移动的方向的点
	private Point getTheNextPoint(Point start, Point goal) {
		Point current = goal;
		while (!current.getFather().equals(start))
			current = current.getFather(); // 向上回溯
		return current;
	}

	// 获得下一个移动方向
	private String getNextDir(Point start, Point next) {
		if (next == null) // 如果next为null，返回null
			return null;
		else if (start.getY() == next.getY()) {
			if (next.getX() > start.getX())
				return "right";
			else
				return "left";
		}
		else {
			if (next.getY() < start.getY())
				return "up";
			else
				return "down";
		}
	}

	// 删除指定的目标
	private void delGoal(Point goal, List<Point> list) {
		for (int i = 0; i < list.size(); i++) {
			if (goal.equals(list.get(i))) {
				list.remove(i);
				break;
			}
		}
	}

	// 判断点point是否在player的范围之内
	private boolean isInTheSpace(Player player, Point point) {
		return (Math.abs(player.getX() - point.getX()) <= SERACH_RANGE)
				&& (Math.abs(player.getY() - point.getY()) <= SERACH_RANGE);
	}

	// 获得距离player最近的目标的下标，没有的话返回-1
	private int getIndexOfNearestGoal(Player player, List<Point> list) {
		int min = INF;
		int index = -1;

		for (int i = 0; i < list.size(); i++) {
			if (!isInTheSpace(player, list.get(i))) // 如果不在指定范围内，跳过
				continue;
			int dis = distance(player, list.get(i));
			if (dis < min) {
				min = dis;
				index = i;
			}
		}
		return index;
	}

	// 计算当前点的危险系数，遍历4个方向
	private int currentDangerDegree(Point point) {
		return degreeOfFourDirs(point, false);
	}

	// 计算将要移动后的点的危险系数
	private int nextDangerDegree(Point point) {
		int degree = 0;
		int y = point.getY();
		int x = point.getX();

		// 遍历上下左右，计算危险度
		degree += degreeOfFourDirs(point, false);

		for (int i = 0; i < DIR.length; i++) {
			int y_t = y + DIR[i][0];
			int x_t = x + DIR[i][1];

			if (y_t < 0 || y_t >= height || x_t < 0 || x_t >= width) // 判断是否越界
				continue;

			String temp = myMap[y_t][x_t];

			switch (i) {
				case 0:
					degree += computeDegree(temp, "down", true);
				case 1:
					degree += computeDegree(temp, "up", true);
				case 2:
					degree += computeDegree(temp, "right", true);
				case 3:
					degree += computeDegree(temp, "left", true);
			}
		}

		return degree;
	}

	// 输入一个字符串和方向，返回危险度
	private int computeDegree(String temp, String bulletDir, boolean addBasic) {
		int degree = 0;
		if (temp.contains("e")) {
			if (temp.contains("1"))
				degree += 2;
			else
				degree++;
		}
		if (temp.contains("s")) // option为1，就计算队友
			degree--;
		if (temp.equals("b_1_" + bulletDir + "_e")) // 一个子弹的完整的字符串表示为b_1(0)_up_s(e)
			degree++;

		if (temp.equals("b_0_" + bulletDir + "_e") && addBasic)
			degree++;

		return degree;
	}

	// 返回4个方向的危险度的和（延伸方向）
	private int degreeOfFourDirs(Point point, boolean addBasic) {
		int degree = 0;
		int y = point.getY();
		int x = point.getX();
		// 上
		for (int i = y - 1; i > y - SERACH_RANGE && i >= 0; i--) {
			String temp = myMap[i][x];
			if (temp.equals("#")) // 如果遇到铁墙，跳过这个方向
				break;
			degree += computeDegree(temp, "down", addBasic);
		}

		// 下
		for (int i = y + 1; i < y + SERACH_RANGE && i < height; i++) {
			String temp = myMap[i][x];
			degree += computeDegree(temp, "up", addBasic);
		}

		// 左
		for (int i = x - 1; i > x - SERACH_RANGE && i >= 0; i--) {
			String temp = myMap[y][i];
			if (temp.equals("#")) // 如果遇到铁墙，跳过这个方向
				break;
			degree += computeDegree(temp, "right", addBasic);
		}

		// 右
		for (int i = x + 1; i < x + SERACH_RANGE && i < width; i++) {
			String temp = myMap[y][i];
			if (temp.equals("#")) // 如果遇到铁墙，跳过这个方向
				break;
			degree += computeDegree(temp, "left", addBasic);
		}

		return degree;
	}
}