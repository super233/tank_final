决赛代码：
	在round内生成地图信息，将星星、敌人、金币分别存储
				|
				|
				∨
	遍历每个player，设置最近的目标（不固定），按照星星、敌人、金币的优先级搜索（9 * 9）
				|		^
				|		|
				∨		|
	Astar判断方向，不可达，在对应列表内删除该目标
				|
				|
				∨
	获得移动方向后，计算当前位置和将要移动的位置的危险系数，选择较小的方向移动（移动或者静止）
				|
				|
				∨
	开火方向，遍历上下左右
			首先，敌人距离为1，向其开火
			向子弹开火
			向较近的敌人开火
			
			
			
			
未实现的部分：
	构造函数计算危险度
	开火方向的判断