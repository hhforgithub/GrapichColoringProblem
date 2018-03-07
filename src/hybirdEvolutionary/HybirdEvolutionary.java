package hybirdEvolutionary;

import java.util.Random;
import java.util.Iterator;
import java.util.LinkedList;

public class HybirdEvolutionary {//种群数为2

	private int[][] matrix;//包含边信息的邻接矩阵
	
	private int[] lor;//记载邻接矩阵每行长度的数组;
	
	private int nodes;//节点个数
	
	private long moves_a,moves_b,moves;//当前迭代步数
	
	private int colors;//目标解颜色数，有可行解时终止，或-1再计算新目标
	
	private int[] solution;//算法最终解，长度为节点数
	
	private int[] tmpsolution;//临时解，用于禁忌搜索过程和存放子代解
	
	private int[] solution_a,solution_b;//两个父代解
	
	private LinkedList<Integer>[] ll_a;//存放解的链表
	private LinkedList<Integer>[] ll_b;
	private LinkedList<Integer>[] ll_son;
	
	private int init_conflict_a,init_conflict_b;//初始冲突数
	
	private int current_conflict_a,current_conflict_b,current_conflict;//当前冲突数
	
	private int best_conflict_a,best_conflict_b,best_conflict;//历史最优解的冲突次数

	/* adjacentColorTable，邻接颜色表
	 * 大小为节点数*目标解颜色数
	 * 元素值为在节点i的邻居中染色为j的节点的个数，即j色的冲突数
	 * */
	private int[][] adjacentColorTable,act_a,act_b;
	
	/* tabuTable，禁忌表
	 * 大小为节点数*目标解颜色数
	 * 元素值为解禁操作（将点i染上颜色j）的迭代次数
	 * 采取惰性更新
	 * */
	private long[][] tabuTable,tabuTable_a,tabuTable_b;
	
	private long total_time = 0;//算法运行总时长
	
	/*********用于findmove() makemove()的变量**********/
	private int move_i,move_j;//表示当前最优操作，将i点染j色
	private int best_delta;//表示当前最优操作带来的冲突数改变量
	private Random random = new Random();
	
	private long inittime;//初始化耗时
	int delta;//当前操作带来的冲突变化
	int times;//遇到同等优秀解的次数，第一次n次以概率1/n接受,每次更新best_delta时重置为1  	
	//领域搜索，尝试将节点i改为j色
	int currentcolor;
	int breakpoiont=0;
	/*
	 * 输入参数：边邻接矩阵，节点数
	 */
    public HybirdEvolutionary(int[][] mat ,int[] llor,int nn){
    	matrix = mat;
    	nodes = nn;
    	lor = llor;
   	}
    /*
	 * 调用此函数开始执行算法
	 * 输入参数：目标解颜色数cc，最大迭代次数mm,最大迭代次数为0则为无次数限制
	 */
    private int gen;//进化代数
    public void Start(int cc,int mm){
    	long start = System.currentTimeMillis();//开始时间
    	colors = cc;
    	init();
    	while(true){
    		tabusearch_a(mm);
    		if(best_conflict == 0){
    			System.out.println("A jump out"+best_conflict);
    			break;
    		}
    		tabusearch_b(mm);
    		if(best_conflict == 0){
    			System.out.println("B jump out"+best_conflict);
    			break;
    		}
    		
    		crossoperation();
    		succeed();
    		if(gen*mm%100000 ==0){
			System.out.println("gen "+gen+" "+best_conflict_a+","+best_conflict_b);
			}
    		gen++;
    	}
    	for(s_i = 0 ; s_i < nodes ; s_i++){
			solution[s_i] = tmpsolution[s_i];
		}
    	
    	long end = System.currentTimeMillis();//结束时间
    	total_time = end - start;
    	printsolution();
    }
    
    /*
     * 交叉运算，对solution_a和solution_b,子代存放于tmpsolution
     * 执行完毕后，两个父代链表被清空，tmpsolution被赋值子代解
     */
    private int cross_count;//父代链表的剩余元素,子代继承集合数计数（为colors时终止）
    private void crossoperation(){
    	//转换为链表
    	arraytolinked(solution_a,ll_a);
    	arraytolinked(solution_b,ll_b);
    	cross_count = 0;
    	//子代继承父代独立集，从最大独立集的开始，继承colors次，剩余未继承的元素随机赋值给子代
    	while(true){
    		findmax(ll_a);
    		if(max == 0)break;
    		//System.out.println("from a color="+maxcolor+" num="+max);
    		ll_son[maxcolor].addAll(ll_a[maxcolor]);
    		deletaElement(ll_b,ll_a[maxcolor]);
    		if(cross_count == colors)break;
    		cross_count++;
    		
    		findmax(ll_b);
    		if(max == 0)break;
    		//System.out.println("from b color="+maxcolor+" num="+max);
    		ll_son[maxcolor].addAll(ll_b[maxcolor]);
    		deletaElement(ll_a,ll_b[maxcolor]);
    		if(cross_count == colors)break;
    		cross_count++;
    	}
    	//将父代链表中的剩余元素随机赋值给子代链表，同时在两个父代中删除这些元素
    	for(i = 0 ; i < colors ;i++){
    		while(!ll_a[i].isEmpty()){
        		tmp_e=ll_a[i].removeFirst();
        		ll_son[random.nextInt(colors)].add((int)tmp_e);
        		for(j = 0 ; j < colors ; j++){
        			if(ll_b[j].remove(tmp_e))break;//若成功删除则停止循环，否则从下一条链表中删除该元素
        		}
           	}
    	}   	
    	linkedtoarray(ll_son, tmpsolution);
    	
    }
    
    /*
     * 子代解取代较差的父代解
     * 被取代的解的禁忌搜索相关数据将被重新初始化，而未被取代的解将保留上次禁忌搜索的状态
     */
    private int s_i,s_j;
    private void succeed(){
    	if(random.nextBoolean()){//取代a
    		for(s_i = 0 ; s_i < nodes ; s_i++){
    			solution_a[s_i] = tmpsolution[s_i];
    		}
    		//计算邻接颜色表
    		for(i = 0 ; i < nodes ; i++){//全置零
        		for(j = 0 ; j < colors ; j++){
        			act_a[i][j] = 0;
        		}
        	}
        	for(s_i = 0 ; s_i < nodes ; s_i++){
        		for(s_j = 0 ; s_j < lor[s_i] ; s_j++){
        			act_a[s_i][solution_a[matrix[s_i][s_j]]]++;
        		}
        	}
        	//重置禁忌表
        	for(i = 0 ; i < nodes ; i++){//全置零
        		for(j = 0 ; j < colors ; j++){
        			tabuTable_a[i][j] = 0;
        		}
        	}
        	//重置搜索步数
        	moves_a = 0;
        	
        	//计算总冲突数
        	best_conflict_a = 0;
        	for(s_i = 0 ; s_i < nodes ; s_i++){
        		best_conflict_a += act_a[s_i][solution_a[s_i]];   		
        	}
        	best_conflict_a /= 2;
        	current_conflict_a = best_conflict_a;

        	
    	}
    	else{//取代b
    		for(s_i = 0 ; s_i < nodes ; s_i++){
    			solution_b[s_i] = tmpsolution[s_i];
    		}
    		//计算邻接颜色表
    		for(i = 0 ; i < nodes ; i++){//全置零
        		for(j = 0 ; j < colors ; j++){
        			act_b[i][j] = 0;
        		}
        	}
        	for(s_i = 0 ; s_i < nodes ; s_i++){
        		for(s_j = 0 ; s_j < lor[s_i] ; s_j++){
        			act_b[s_i][solution_b[matrix[s_i][s_j]]]++;
        		}
        	}
        	//重置禁忌表
        	for(i = 0 ; i < nodes ; i++){//全置零
        		for(j = 0 ; j < colors ; j++){
        			tabuTable_b[i][j] = 0;
        		}
        	}
        	//重置搜索步数
        	moves_b = 0;
        	
        	//计算总冲突数
        	best_conflict_b = 0;
        	for(s_i = 0 ; s_i < nodes ; s_i++){  		
        		best_conflict_b += act_b[s_i][solution_b[s_i]];
        	}
        	best_conflict_b /= 2;
        	current_conflict_b = best_conflict_b;

    	}
    }
    
    /*
     * 子代选择某一父解中的子集后，从另一父代中剔除子集中的相同元素
     * c是父代的子集，ll是另一父代
     * 执行后c会被清空，ll中与c相同的元素会被移除
     */
    private int de_i;
    private Object tmp_e;
    private void deletaElement(LinkedList[] ll,LinkedList c){
    	while(!c.isEmpty()){
    		tmp_e=c.removeFirst();
    		for(de_i = 0 ; de_i < colors ; de_i++){
    			if(ll[de_i].remove(tmp_e)){
    				break;//若成功删除则停止循环，否则从下一条链表中删除该元素
    			}
    		}
       	}
    }
    
    //查找数组中最大值,输入只能为ll_a_length 或 ll_a_length ,长度为colors,返回改变maxcolor
    int fm_i,max,maxcolor;
    private void findmax(LinkedList[] ll){
    	max = -1;
    	for(fm_i = 0 ; fm_i < colors ; fm_i++){
    		if(ll[fm_i].size()>max){
    			max = ll[fm_i].size();
    			maxcolor = fm_i;
    		}
    	}
    }
    
    int i,j;
    /*
     * 对a进行禁忌搜索，将a的状态赋值给搜索用的变量
     * 搜索结束后，将搜索结果赋值给a的状态
     */
    private void tabusearch_a(int mm){
    	for(i = 0 ; i < nodes ; i++){
    		tmpsolution[i] = solution_a[i];
    	}
    	current_conflict = current_conflict_a;
    	best_conflict = best_conflict_a;
    	adjacentColorTable = act_a;
    	tabuTable = tabuTable_a;
    	moves = moves_a;
    	tabusearch(mm);
    	moves_a = moves;
    	for(i = 0 ; i < nodes ; i++){
    		solution_a[i] = tmpsolution[i];
    	}
    	current_conflict_a = current_conflict;
    	best_conflict_a = best_conflict;
    }
    
    /*
     * 对b进行禁忌搜索，将b的状态赋值给搜索用的变量
     * 搜索结束后，将搜索结果赋值给b的状态
     */
    private void tabusearch_b(int mm){
    	for(i = 0 ; i < nodes ; i++){
    		tmpsolution[i] = solution_b[i];
    	}
    	current_conflict = current_conflict_b;
    	best_conflict = best_conflict_b;
    	adjacentColorTable = act_b;
    	tabuTable = tabuTable_b;
    	moves = moves_b;
    	tabusearch(mm);
    	for(i = 0 ; i < nodes ; i++){
    		solution_b[i] = tmpsolution[i];
    	}
    	moves_b = moves;
    	current_conflict_b = current_conflict;
    	best_conflict_b = best_conflict;
    }
    
    /*
     * 针对tmpsolution搜索,使用变量名tabuTable,AdjacentColorTable,best_conflict,current_conflict,moves
     * 搜索前需要对以上变量相应的赋值(a或b的对应值)
     * 搜索结束时，结果存储在bestsolution
     */
    private void tabusearch(int mm){//输入最大迭代次数,对全局变量tmpsolution进行搜索,使用AdjacentColorTable
    	while(best_conflict != 0 && moves <= mm){
			findmove();
			if(best_delta == Integer.MAX_VALUE){
				System.out.println("所有领域操作被禁忌，无法求解");
				System.exit(1);
			}			
			makemove();
			//System.out.println(moves+" delta: "+best_delta+" best_c: "+best_conflict+" current_c: "+current_conflict);
			//testsolution(tmpsolution);
			moves++;
		}
    }
    
    /*
     * 做出动作
     */
    int m_i,m_j;
    private void makemove(){                                  
    		//更新邻接颜色表,按照邻接矩阵受影响者为move_i的所有邻居
        	for(m_i = 0 ; m_i < lor[move_i] ; m_i++){
        		m_j = matrix[move_i][m_i];//j表示move_i的i号邻居
        		adjacentColorTable[m_j][tmpsolution[move_i]]--;//旧色冲突数-1
        		adjacentColorTable[m_j][move_j]++;//新色冲突数+1
        	}
        	//更新当前冲突
        	current_conflict += best_delta;
        	
        	//更新解
        	tmpsolution[move_i] = move_j;
        	
        	//更新最优解
        	if(current_conflict < best_conflict){
        		best_conflict = current_conflict;
        	}

        	//更新禁忌表
        	tabuTable[move_i][move_j] = moves + current_conflict*(random.nextInt(10)+1);
        								//+ noimprovetimes/20;
//        	tabuTable[move_i][move_j] = moves + current_conflict + random.nextInt(10)+1;
    }
    
    /*
     * 查找可行的领域动作
     * 执行后改变全局变量（返回）best_delta , move_i , move_j
     */
    int f_i,f_j;
    private void findmove(){
    	best_delta = Integer.MAX_VALUE;//历史最优动作带来的冲突数变化，初始为极大值
    	delta = Integer.MAX_VALUE;//当前操作带来的冲突变化
    	times = 1;//遇到同等优秀解的次数，第一次n次以概率1/n接受,每次更新best_delta时重置为1  	
    	//领域搜索，尝试将节点i改为j色
    	for(f_i = 0 ; f_i < nodes ; f_i ++){
    		currentcolor = tmpsolution[f_i];
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
                					//一旦接受一个禁忌解，best_delta水平提高，之后所有接受的解必都能改善全局解
        						}
        					}
        					else{//若未被禁忌
        						move_i = f_i;
        						move_j = f_j;
            					best_delta = delta;		
            					times = 1;
        					}
        				}
        				//若该操作与最优解同等优，是第n次遇到则以1/n的概率接受这个解，使每个遇到的解都是1/n的机会被接受
        				//由于delta未变，更新解时不更新delta，times也不重置
        				else if(delta == best_delta){
        					if(random.nextInt(times++) == 0){
        						if(tabuTable[f_i][f_j] > moves){//若该操作被禁忌
            						//但该禁忌操作能改善全局最优解，该解可候选，保存禁忌解
            						if(current_conflict + delta < best_conflict){    							
            							move_i = f_i;
            							move_j = f_j;  
            						}
            					}
            					else{//若未被禁忌,保存该非禁忌解
            						move_i = f_i;
            						move_j = f_j;	
            					}
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
    	
    	//初始化两个父类解，为随机数
    	tmpsolution = new int[nodes];
    	solution_a = new int[nodes];
    	solution_b = new int[nodes];
    	solution = new int[nodes];
    	int i,j;
    	for(i = 0 ; i < nodes ; i++){
    		solution_a[i] = random.nextInt(colors);
    	}
    	for(i = 0 ; i < nodes ; i++){
    		solution_b[i] = random.nextInt(colors);
    	}
    	//初始化解的链表
    	ll_a = new LinkedList[colors];//存放解的链表
    	ll_b = new LinkedList[colors];//存放解的链表
    	ll_son = new LinkedList[colors];//存放解的链表
    	for(i = 0 ; i < colors ; i++){
    		ll_a[i] = new LinkedList<Integer>();
    		ll_b[i] = new LinkedList<Integer>();
    		ll_son[i] = new LinkedList<Integer>();
    	}
    	
    	//初始化邻接颜色表AdjacentColorTable作为指针指向a或b
    	act_a = new int[nodes][colors];
    	act_b = new int[nodes][colors];
    	for(i = 0 ; i < nodes ; i++){//全置零
    		for(j = 0 ; j < colors ; j++){
    			act_a[i][j] = 0;
    			act_b[i][j] = 0;
    		}
    	}
    	//计算邻接颜色表
    	for(i = 0 ; i < nodes ; i++){
    		for(j = 0 ; j < lor[i] ; j++){
    			act_a[i][solution_a[matrix[i][j]]]++;
    			act_b[i][solution_b[matrix[i][j]]]++;
    			//matrix访问i的邻居，solution访问邻居颜色，在i邻接颜色表中的对应颜色+1
    		}
    	}
    	
    	//计算总冲突数并初始化最佳冲突数,邻接表中与i染色相同的邻居个数，求和
    	for(i = 0 ; i < nodes ; i++){
    		best_conflict_a += act_a[i][solution_a[i]];   		
    		best_conflict_b += act_b[i][solution_b[i]];
    	}
    	best_conflict_a /= 2;
    	best_conflict_b /= 2;
    	init_conflict_a = best_conflict_a;
    	init_conflict_b = best_conflict_b;
    	current_conflict_a = best_conflict_a;
    	current_conflict_b = best_conflict_b;
    	current_conflict = Integer.MAX_VALUE;
    	best_conflict = Integer.MAX_VALUE;

    	//初始化禁忌表
    	tabuTable_a = new long[nodes][colors];
    	tabuTable_b = new long[nodes][colors];
    	for(i = 0 ; i < nodes ; i++){//全置零
    		for(j = 0 ; j < colors ; j++){
    			tabuTable_a[i][j] = 0;
    			tabuTable_b[i][j] = 0;
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
    private void testsolution(int[] solution){
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
    
    private void printsolution(){
    	//输出结果
    	System.out.println("结果为");
    	for(int i = 0 ; i < nodes ; i++){
    		System.out.print("("+i+","+solution[i]+") ");
    	}
    	System.out.println();
    	System.out.println("初始化耗时："+inittime+"ms");
    	System.out.println("初始冲突数："+init_conflict_a+","+init_conflict_b);
    	System.out.println("总耗时："+total_time+"ms");
    	System.out.println("进化代数："+gen);
    	double sectime = total_time/1000;
    	double timespers = gen/sectime;
    	System.out.println("平均每秒迭代次数："+timespers);
    	testsolution(solution);
    	
    }
    
    /*
     * 将数组形式的解转换成按颜色分类的多链表形式（独立集表示），同种颜色的节点构成一个链表，用于交叉运算
     * 完成转换同时记录链表长度
     */
    int atl_i;
    private void arraytolinked(int[] s,LinkedList[] l){	
    	for(atl_i = 0 ; atl_i < nodes ; atl_i++){
    		l[s[atl_i]].add(atl_i);
    	}
    }
    
    /*
     * 将多链表形式的解转换成数组形式表示，用于禁忌搜索和结果输出
     * 完成转换的同时清空链表，及其长度
     */
    int lta_i;
    private void linkedtoarray(LinkedList[] l,int[] s){
    	for(lta_i = 0 ; lta_i < colors ; lta_i++){
    		while(!l[lta_i].isEmpty()){
    			s[(int)l[lta_i].removeFirst()]=lta_i;
    		}
    	}
    }
	
    public void test_atllta(int c){
    	colors = c;
    	init();
    	for(int i = 0 ; i < nodes ; i++){
    		solution[i] = random.nextInt(colors);
    	}
    	LinkedList<Integer>[] l = new LinkedList[colors];
    	for(int i = 0 ; i < colors ; i++){
    		l[i] = new LinkedList<Integer>();
    	}
    	arraytolinked(solution,l);
    	Iterator<Integer> iterator;
    	for(int i = 0 ; i < colors ; i++){
    		iterator = l[i].iterator();
    		while(iterator.hasNext()){
                System.out.print((int)iterator.next()+" ");
            }
    		System.out.println();
    	}
    	linkedtoarray(l,tmpsolution);
    	for(int i = 0 ; i < nodes ; i++){
    		System.out.print(tmpsolution[i]+" ");
    	}
    	System.out.println();
    	for(int i = 0 ; i < nodes ; i++){
    		System.out.print(solution[i]+" ");
    	}
    }
    
    public void test_crossoperation(int c){
    	colors = c;
    	init();
    	crossoperation();
    	arraytolinked(tmpsolution,ll_a);
    	for(i = 0 ; i < nodes ; i++){
    		System.out.print(tmpsolution[i]+" ");
    		System.out.print(solution_a[i]+" ");
    		System.out.print(solution_b[i]+" ");
    		System.out.println();
    	}
    	for(i = 0 ; i < colors ; i++){
    		System.out.println("color ="+i+" count = "+ll_a[i].size());
    	}
    }
    
}
