package tabusearch0;

import java.awt.Point;
import java.util.Random;

/*
 * 在邻域动作中挑选最佳的禁忌或者非禁忌移动。
 * 当最佳禁忌移动满足“特赦准则”时选择最佳禁忌移动，否则选择非禁忌移动。
 * 同时将本次移动记录在禁忌表中
 * 算法终止条件是求出解，或者达到设定的迭代次数。
 * 
 * 特赦准则：最佳禁忌移动的目标值小于历史最佳冲突best_conflict且小于当前非禁忌移动的值,
 * 			则可接受禁忌移动的值，否则接受非禁忌移动的值。
 */
public class TabuSearch {
	private int[][] matrix;//包含边信息的邻接矩阵
	
	private int[] lor;//记载邻接矩阵每行长度的数组;
	
	private int nodes;//节点个数
	
	private long moves;//当前迭代步数
	
	private long maxmoves;//最大迭代次数，可作为为终止条件之一
	
	private int colors;//目标解颜色数，有可行解时终止，或-1再计算新目标
	
	private int[] solution;//解，长度为节点数
	
	private int init_conflict;//初始冲突数
	
	private int current_conflict;//当前冲突数
	
	private int best_conflict;//历史最优解的冲突次数
	
	/* adjacentColorTable，邻接颜色表
	 * 大小为节点数*目标解颜色数
	 * 元素值为在节点i的邻居中染色为j的节点的个数，即j色的冲突数
	 * */
	private int[][] adjacentColorTable;
	
	/* tabuTable，禁忌表
	 * 大小为节点数*目标解颜色数
	 * 元素值为解禁操作（将点i染上颜色j）的迭代次数
	 * 采取惰性更新
	 * */
	private long[][] tabuTable;
	
	/* tabutenture，禁忌表长
	 * 动态表长，与冲突数有关，并有额外随机增量
	 * 值=moves + f + random(1-10);
	 * */
	private int[][] tabutenture;
	
	/*********用于findmove() makemove()的变量**********/
	private int move_i,move_j;//表示当前最优操作，将i点染j色
	private int best_delta;//表示当前最优操作带来的冲突数改变量
	private Random random = new Random();
	private boolean istabu;//是否执行了禁忌操作
	
	private long inittime;//初始化耗时
	private long tabumoves;//执行禁忌操作的次数
	private int noimprovetimes = 0;//连续无更优解的次数
	private int bestnotimprove = 0;//最优解未更新，且当前解连续等于最优解的累积次数
	private int count = 0;
	int delta;//当前操作带来的冲突变化
	int times;//遇到同等优秀解的次数，第一次n次以概率1/n接受,每次更新best_delta时重置为1  	
	//领域搜索，尝试将节点i改为j色
	int currentcolor;
	/*
	 * 输入参数：边邻接矩阵，节点数
	 */
    public TabuSearch(int[][] mat ,int[] llor,int nn){
    	matrix = mat;
    	nodes = nn;
    	lor = llor;
   	}
    
    /*
	 * 调用此函数开始执行算法
	 * 输入参数：目标解颜色数，最大迭代次数,最大迭代次数为0则为无次数限制
	 */
    public void Start(int cc,long mm){
    	long start = System.currentTimeMillis();//开始时间
    	colors = cc;
    	maxmoves = mm;
    	tabumoves = 0;//执行禁忌操作的次数，无关算法，仅统计
    	init();
    	if(mm <= 0){//无迭代次数限制，终止条件为冲突数为0
    		while(best_conflict != 0){
    			findmove();
    			if(best_delta == Integer.MAX_VALUE){
    				System.out.println("所有领域操作被禁忌，无法求解");
    				break;
    			}
    			makemove();
    			if(moves%200000 == 0)//每20万次迭代输出一次状态
    				System.out.println(moves+" "+best_conflict+","+current_conflict+","+init_conflict);
    			if(istabu)tabumoves++;
    			moves++;
    		}
    	}
    	else{//终止条件为冲突数为0 或 迭代次数到达限制
    		while(best_conflict != 0 && moves <= mm){
    			findmove();
    			if(best_delta == Integer.MAX_VALUE){
    				System.out.println("所有领域操作被禁忌，无法求解");
    				break;
    			}
    			makemove();
    			if(moves%100000 == 0)
        			System.out.println(moves+" "+best_conflict+","+current_conflict+","+init_conflict);
    			if(istabu)tabumoves++;
    			moves++;
    		}
    	}
    	long end = System.currentTimeMillis();//结束时间
    	//输出结果
    	System.out.println("结果为");
    	for(int i = 0 ; i < nodes ; i++){
    		System.out.print("("+i+","+solution[i]+") ");
    	}
    	System.out.println();
    	System.out.println("初始化耗时："+inittime+"ms");
    	System.out.println("初始冲突数："+init_conflict);
    	System.out.println("总耗时："+(end - start)+"ms");
    	System.out.println("迭代次数："+(moves-1));
    	double sectime = (end - start)/1000;
    	double timespers = moves/sectime;
    	System.out.println("平均每秒迭代次数："+timespers);
    	System.out.println("执行禁忌操作次数："+tabumoves);
    	testsolution();
    	
    }
    
    /*
     * 做出动作
     */
    int m_i,m_j;//为了不在findmove，makemove中创建局部变量，提升效率
    private void makemove(){                                  
    		//更新邻接颜色表,按照邻接矩阵受影响者为move_i的所有邻居
        	for(m_i = 0 ; m_i < lor[move_i] ; m_i++){
        		m_j = matrix[move_i][m_i];//j表示move_i的i号邻居
        		adjacentColorTable[m_j][solution[move_i]]--;//旧色冲突数-1
        		adjacentColorTable[m_j][move_j]++;//新色冲突数+1
        	}
        	//更新当前冲突
        	current_conflict += best_delta;
        	//更新最优解
        	if(current_conflict < best_conflict){
        		best_conflict = current_conflict;
        	}
        	//更新解
        	solution[move_i] = move_j;
        	
        	if(best_delta >= 0)noimprovetimes++;
        	else noimprovetimes = 0;
        	//更新禁忌表
        	tabuTable[move_i][move_j] = moves + current_conflict*random.nextInt(10)+1;
        								//+ noimprovetimes/20;
//        	tabuTable[move_i][move_j] = moves + current_conflict + random.nextInt(10)+1;
    }
    
    /*
     * 查找可行的领域动作
     * 执行后改变全局变量（返回）best_delta , move_i , move_j
     */
    int f_i,f_j;//为了不在findmove，makemove中创建局部变量，提升效率
    private void findmove(){
    	best_delta = Integer.MAX_VALUE;//历史最优动作带来的冲突数变化，初始为极大值
    	delta = 0;//当前操作带来的冲突变化
    	times = 1;//遇到同等优秀解的次数，第一次n次以概率1/n接受,每次更新best_delta时重置为1  	
    	//领域搜索，尝试将节点i改为j色
    	for(f_i = 0 ; f_i < nodes ; f_i ++){
    		currentcolor = solution[f_i];
    		if(adjacentColorTable[f_i][currentcolor]>0){
    			for(f_j = 0 ; f_j < colors ; f_j++){
        			if(f_j != currentcolor){//若将要改变的颜色不是本来的颜色
        				//计算delta值=新色邻居数-旧色邻居数
        				delta = adjacentColorTable[f_i][f_j] - adjacentColorTable[f_i][currentcolor];   				
        				if(delta < best_delta){//若该操作在领域中为历史最优
        					//若该操作被禁忌
        					if(tabuTable[f_i][f_j] > moves){
        						//但该禁忌操作能改善全局最优解，该解可候选
        						if(current_conflict + delta < best_conflict){    							
        							move_i = f_i;
        							move_j = f_j;        							
                					times = 1;
                					best_delta = delta;
                					istabu = true;
                					//一旦接受一个禁忌解，best_delta水平提高，之后所有接受的解必都能改善全局解
        						}
        					}
        					else{//若未被禁忌
        						move_i = f_i;
        						move_j = f_j;
            					best_delta = delta;		
            					times = 1;
            					istabu = false;
        					}
        				}
        				//若该操作与最优解同等优，是第n次遇到则以1/n的概率接受这个解，使每个遇到的解都是1/n的机会被接受
        				//由于delta未变，更新解时不更新delta，times也不重置
        				else if(delta == best_delta && random.nextInt(times++) == 0){    					
        					if(tabuTable[f_i][f_j] > moves){//若该操作被禁忌
        						//但该禁忌操作能改善全局最优解，该解可候选，保存禁忌解
        						if(current_conflict + delta < best_conflict){    							
        							move_i = f_i;
        							move_j = f_j;  
        							istabu = true;
        						}
        					}
        					else{//若未被禁忌,保存该非禁忌解
        						move_i = f_i;
        						move_j = f_j;	
        						istabu = false;
        					}
        				}
        			}
        		}
    		}
    	}
    }
    
    /*
     * 初始化
     */
    private void init(){
    	long start = System.currentTimeMillis();//开始时间
    	//初始化迭代步数
    	moves = 0;
    	
    	//初始化解，为随机数
    	solution = new int[nodes];
    	for(int i = 0 ; i < nodes ; i++){
    		solution[i] = random.nextInt(colors);
    	}
    	
    	//初始化邻接颜色表
    	adjacentColorTable = new int[nodes][colors];
    	for(int i = 0 ; i < nodes ; i++){//全置零
    		for(int j = 0 ; j < colors ; j++){
    			adjacentColorTable[i][j] = 0;
    		}
    	}
    	for(int i = 0 ; i < nodes ; i++){
    		for(int j = 0 ; j < lor[i] ; j++){
    			adjacentColorTable[i][solution[matrix[i][j]]]++;
    			//matrix访问i的邻居，solution访问邻居颜色，在i邻接颜色表中的对应颜色+1
    		}
    	}
    	
    	//计算总冲突数并初始化最佳冲突数,邻接表中与i染色相同的邻居个数，求和
    	best_conflict = 0;
    	for(int i = 0 ; i < nodes ; i++){
    		best_conflict += adjacentColorTable[i][solution[i]];
    	}
    	best_conflict /= 2;
    	init_conflict = best_conflict;
    	current_conflict = best_conflict;

    	//初始化禁忌表
    	tabuTable = new long[nodes][colors];
    	for(int i = 0 ; i < nodes ; i++){//全置零
    		for(int j = 0 ; j < colors ; j++){
    			tabuTable[i][j] = 0;
    		}
    	}
    	    	
    	long end = System.currentTimeMillis();//结束时间
    	inittime = end - start;
    	
    	//printACT();
    	
    }
    
    /*
     * 调试输出邻接颜色表
     */
    private void printACT(){
    	for(int i = 0 ; i < nodes ; i++){//全置零
    		System.out.print("Node"+i+"   ");
    		for(int j = 0 ; j < colors ; j++){
    			System.out.print(adjacentColorTable[i][j]+" ");
    		}
    		System.out.println();
    	}
    }
    
    /*
     * 验算结果
     */
    private void testsolution(){
    	int[][] act = new int[nodes][colors];
    	for(int i = 0 ; i < nodes ; i++){//全置零
    		for(int j = 0 ; j < colors ; j++){
    			act[i][j] = 0;
    		}
    	}
    	for(int i = 0 ; i < nodes ; i++){
    		for(int j = 0 ; j < lor[i] ; j++){
    			act[i][solution[matrix[i][j]]]++;
    			//matrix访问i的邻居，solution访问邻居颜色，在i邻接颜色表中的对应颜色+1
    		}
    	}
    	
    	int cf = 0;
    	for(int i = 0 ; i < nodes ; i++){
    		cf += act[i][solution[i]];
    	}
    	cf /= 2;
    	
    	System.out.println("答案冲突个数为："+cf);
    	
    }
    
    //随机重置解
    private void diversification(){
    	for(int i = 0 ; i < nodes ; i++){
    		solution[i] = random.nextInt(colors);
    	}
    	for(int i = 0 ; i < nodes ; i++){//全置零
    		for(int j = 0 ; j < colors ; j++){
    			adjacentColorTable[i][j] = 0;
    		}
    	}
    	for(int i = 0 ; i < nodes ; i++){
    		for(int j = 0 ; j < lor[i] ; j++){
    			adjacentColorTable[i][solution[matrix[i][j]]]++;
    			//matrix访问i的邻居，solution访问邻居颜色，在i邻接颜色表中的对应颜色+1
    		}
    	}
    	best_conflict = 0;
    	for(int i = 0 ; i < nodes ; i++){
    		best_conflict += adjacentColorTable[i][solution[i]];
    	}
    	best_conflict /= 2;
    	init_conflict = best_conflict;
    	current_conflict = best_conflict;
    	
    }
	
}
/*
 * 改进:搜索领域只考虑冲突大于0的点
 */
