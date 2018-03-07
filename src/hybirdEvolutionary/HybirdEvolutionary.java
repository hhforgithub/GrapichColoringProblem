package hybirdEvolutionary;

import java.util.Random;
import java.util.Iterator;
import java.util.LinkedList;

public class HybirdEvolutionary {//��Ⱥ��Ϊ2

	private int[][] matrix;//��������Ϣ���ڽӾ���
	
	private int[] lor;//�����ڽӾ���ÿ�г��ȵ�����;
	
	private int nodes;//�ڵ����
	
	private long moves_a,moves_b,moves;//��ǰ��������
	
	private int colors;//Ŀ�����ɫ�����п��н�ʱ��ֹ����-1�ټ�����Ŀ��
	
	private int[] solution;//�㷨���ս⣬����Ϊ�ڵ���
	
	private int[] tmpsolution;//��ʱ�⣬���ڽ����������̺ʹ���Ӵ���
	
	private int[] solution_a,solution_b;//����������
	
	private LinkedList<Integer>[] ll_a;//��Ž������
	private LinkedList<Integer>[] ll_b;
	private LinkedList<Integer>[] ll_son;
	
	private int init_conflict_a,init_conflict_b;//��ʼ��ͻ��
	
	private int current_conflict_a,current_conflict_b,current_conflict;//��ǰ��ͻ��
	
	private int best_conflict_a,best_conflict_b,best_conflict;//��ʷ���Ž�ĳ�ͻ����

	/* adjacentColorTable���ڽ���ɫ��
	 * ��СΪ�ڵ���*Ŀ�����ɫ��
	 * Ԫ��ֵΪ�ڽڵ�i���ھ���ȾɫΪj�Ľڵ�ĸ�������jɫ�ĳ�ͻ��
	 * */
	private int[][] adjacentColorTable,act_a,act_b;
	
	/* tabuTable�����ɱ�
	 * ��СΪ�ڵ���*Ŀ�����ɫ��
	 * Ԫ��ֵΪ�������������iȾ����ɫj���ĵ�������
	 * ��ȡ���Ը���
	 * */
	private long[][] tabuTable,tabuTable_a,tabuTable_b;
	
	private long total_time = 0;//�㷨������ʱ��
	
	/*********����findmove() makemove()�ı���**********/
	private int move_i,move_j;//��ʾ��ǰ���Ų�������i��Ⱦjɫ
	private int best_delta;//��ʾ��ǰ���Ų��������ĳ�ͻ���ı���
	private Random random = new Random();
	
	private long inittime;//��ʼ����ʱ
	int delta;//��ǰ���������ĳ�ͻ�仯
	int times;//����ͬ�������Ĵ�������һ��n���Ը���1/n����,ÿ�θ���best_deltaʱ����Ϊ1  	
	//�������������Խ��ڵ�i��Ϊjɫ
	int currentcolor;
	int breakpoiont=0;
	/*
	 * ������������ڽӾ��󣬽ڵ���
	 */
    public HybirdEvolutionary(int[][] mat ,int[] llor,int nn){
    	matrix = mat;
    	nodes = nn;
    	lor = llor;
   	}
    /*
	 * ���ô˺�����ʼִ���㷨
	 * ���������Ŀ�����ɫ��cc������������mm,����������Ϊ0��Ϊ�޴�������
	 */
    private int gen;//��������
    public void Start(int cc,int mm){
    	long start = System.currentTimeMillis();//��ʼʱ��
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
    	
    	long end = System.currentTimeMillis();//����ʱ��
    	total_time = end - start;
    	printsolution();
    }
    
    /*
     * �������㣬��solution_a��solution_b,�Ӵ������tmpsolution
     * ִ����Ϻ���������������գ�tmpsolution����ֵ�Ӵ���
     */
    private int cross_count;//���������ʣ��Ԫ��,�Ӵ��̳м�����������Ϊcolorsʱ��ֹ��
    private void crossoperation(){
    	//ת��Ϊ����
    	arraytolinked(solution_a,ll_a);
    	arraytolinked(solution_b,ll_b);
    	cross_count = 0;
    	//�Ӵ��̳и����������������������Ŀ�ʼ���̳�colors�Σ�ʣ��δ�̳е�Ԫ�������ֵ���Ӵ�
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
    	//�����������е�ʣ��Ԫ�������ֵ���Ӵ�����ͬʱ������������ɾ����ЩԪ��
    	for(i = 0 ; i < colors ;i++){
    		while(!ll_a[i].isEmpty()){
        		tmp_e=ll_a[i].removeFirst();
        		ll_son[random.nextInt(colors)].add((int)tmp_e);
        		for(j = 0 ; j < colors ; j++){
        			if(ll_b[j].remove(tmp_e))break;//���ɹ�ɾ����ֹͣѭ�����������һ��������ɾ����Ԫ��
        		}
           	}
    	}   	
    	linkedtoarray(ll_son, tmpsolution);
    	
    }
    
    /*
     * �Ӵ���ȡ���ϲ�ĸ�����
     * ��ȡ���Ľ�Ľ�������������ݽ������³�ʼ������δ��ȡ���Ľ⽫�����ϴν���������״̬
     */
    private int s_i,s_j;
    private void succeed(){
    	if(random.nextBoolean()){//ȡ��a
    		for(s_i = 0 ; s_i < nodes ; s_i++){
    			solution_a[s_i] = tmpsolution[s_i];
    		}
    		//�����ڽ���ɫ��
    		for(i = 0 ; i < nodes ; i++){//ȫ����
        		for(j = 0 ; j < colors ; j++){
        			act_a[i][j] = 0;
        		}
        	}
        	for(s_i = 0 ; s_i < nodes ; s_i++){
        		for(s_j = 0 ; s_j < lor[s_i] ; s_j++){
        			act_a[s_i][solution_a[matrix[s_i][s_j]]]++;
        		}
        	}
        	//���ý��ɱ�
        	for(i = 0 ; i < nodes ; i++){//ȫ����
        		for(j = 0 ; j < colors ; j++){
        			tabuTable_a[i][j] = 0;
        		}
        	}
        	//������������
        	moves_a = 0;
        	
        	//�����ܳ�ͻ��
        	best_conflict_a = 0;
        	for(s_i = 0 ; s_i < nodes ; s_i++){
        		best_conflict_a += act_a[s_i][solution_a[s_i]];   		
        	}
        	best_conflict_a /= 2;
        	current_conflict_a = best_conflict_a;

        	
    	}
    	else{//ȡ��b
    		for(s_i = 0 ; s_i < nodes ; s_i++){
    			solution_b[s_i] = tmpsolution[s_i];
    		}
    		//�����ڽ���ɫ��
    		for(i = 0 ; i < nodes ; i++){//ȫ����
        		for(j = 0 ; j < colors ; j++){
        			act_b[i][j] = 0;
        		}
        	}
        	for(s_i = 0 ; s_i < nodes ; s_i++){
        		for(s_j = 0 ; s_j < lor[s_i] ; s_j++){
        			act_b[s_i][solution_b[matrix[s_i][s_j]]]++;
        		}
        	}
        	//���ý��ɱ�
        	for(i = 0 ; i < nodes ; i++){//ȫ����
        		for(j = 0 ; j < colors ; j++){
        			tabuTable_b[i][j] = 0;
        		}
        	}
        	//������������
        	moves_b = 0;
        	
        	//�����ܳ�ͻ��
        	best_conflict_b = 0;
        	for(s_i = 0 ; s_i < nodes ; s_i++){  		
        		best_conflict_b += act_b[s_i][solution_b[s_i]];
        	}
        	best_conflict_b /= 2;
        	current_conflict_b = best_conflict_b;

    	}
    }
    
    /*
     * �Ӵ�ѡ��ĳһ�����е��Ӽ��󣬴���һ�������޳��Ӽ��е���ͬԪ��
     * c�Ǹ������Ӽ���ll����һ����
     * ִ�к�c�ᱻ��գ�ll����c��ͬ��Ԫ�ػᱻ�Ƴ�
     */
    private int de_i;
    private Object tmp_e;
    private void deletaElement(LinkedList[] ll,LinkedList c){
    	while(!c.isEmpty()){
    		tmp_e=c.removeFirst();
    		for(de_i = 0 ; de_i < colors ; de_i++){
    			if(ll[de_i].remove(tmp_e)){
    				break;//���ɹ�ɾ����ֹͣѭ�����������һ��������ɾ����Ԫ��
    			}
    		}
       	}
    }
    
    //�������������ֵ,����ֻ��Ϊll_a_length �� ll_a_length ,����Ϊcolors,���ظı�maxcolor
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
     * ��a���н�����������a��״̬��ֵ�������õı���
     * ���������󣬽����������ֵ��a��״̬
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
     * ��b���н�����������b��״̬��ֵ�������õı���
     * ���������󣬽����������ֵ��b��״̬
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
     * ���tmpsolution����,ʹ�ñ�����tabuTable,AdjacentColorTable,best_conflict,current_conflict,moves
     * ����ǰ��Ҫ�����ϱ�����Ӧ�ĸ�ֵ(a��b�Ķ�Ӧֵ)
     * ��������ʱ������洢��bestsolution
     */
    private void tabusearch(int mm){//��������������,��ȫ�ֱ���tmpsolution��������,ʹ��AdjacentColorTable
    	while(best_conflict != 0 && moves <= mm){
			findmove();
			if(best_delta == Integer.MAX_VALUE){
				System.out.println("����������������ɣ��޷����");
				System.exit(1);
			}			
			makemove();
			//System.out.println(moves+" delta: "+best_delta+" best_c: "+best_conflict+" current_c: "+current_conflict);
			//testsolution(tmpsolution);
			moves++;
		}
    }
    
    /*
     * ��������
     */
    int m_i,m_j;
    private void makemove(){                                  
    		//�����ڽ���ɫ��,�����ڽӾ�����Ӱ����Ϊmove_i�������ھ�
        	for(m_i = 0 ; m_i < lor[move_i] ; m_i++){
        		m_j = matrix[move_i][m_i];//j��ʾmove_i��i���ھ�
        		adjacentColorTable[m_j][tmpsolution[move_i]]--;//��ɫ��ͻ��-1
        		adjacentColorTable[m_j][move_j]++;//��ɫ��ͻ��+1
        	}
        	//���µ�ǰ��ͻ
        	current_conflict += best_delta;
        	
        	//���½�
        	tmpsolution[move_i] = move_j;
        	
        	//�������Ž�
        	if(current_conflict < best_conflict){
        		best_conflict = current_conflict;
        	}

        	//���½��ɱ�
        	tabuTable[move_i][move_j] = moves + current_conflict*(random.nextInt(10)+1);
        								//+ noimprovetimes/20;
//        	tabuTable[move_i][move_j] = moves + current_conflict + random.nextInt(10)+1;
    }
    
    /*
     * ���ҿ��е�������
     * ִ�к�ı�ȫ�ֱ��������أ�best_delta , move_i , move_j
     */
    int f_i,f_j;
    private void findmove(){
    	best_delta = Integer.MAX_VALUE;//��ʷ���Ŷ��������ĳ�ͻ���仯����ʼΪ����ֵ
    	delta = Integer.MAX_VALUE;//��ǰ���������ĳ�ͻ�仯
    	times = 1;//����ͬ�������Ĵ�������һ��n���Ը���1/n����,ÿ�θ���best_deltaʱ����Ϊ1  	
    	//�������������Խ��ڵ�i��Ϊjɫ
    	for(f_i = 0 ; f_i < nodes ; f_i ++){
    		currentcolor = tmpsolution[f_i];
    		if(adjacentColorTable[f_i][currentcolor]>0){
    			for(f_j = 0 ; f_j < colors ; f_j++){
        			if(f_j != currentcolor){//����Ҫ�ı����ɫ���Ǳ�������ɫ
        				//����deltaֵ=��ɫ�ھ���-��ɫ�ھ���
        				delta = adjacentColorTable[f_i][f_j] - adjacentColorTable[f_i][currentcolor];   				
        				if(delta < best_delta){//���ò�����������Ϊ��ʷ����
        					//���ò���������
        					if(tabuTable[f_i][f_j] > moves){
        						//���ý��ɲ����ܸ���ȫ�����Ž⣬�ý�ɺ�ѡ
        						if(current_conflict + delta < best_conflict){    							
        							move_i = f_i;
        							move_j = f_j;        							
                					times = 1;
                					best_delta = delta;
                					//һ������һ�����ɽ⣬best_deltaˮƽ��ߣ�֮�����н��ܵĽ�ض��ܸ���ȫ�ֽ�
        						}
        					}
        					else{//��δ������
        						move_i = f_i;
        						move_j = f_j;
            					best_delta = delta;		
            					times = 1;
        					}
        				}
        				//���ò��������Ž�ͬ���ţ��ǵ�n����������1/n�ĸ��ʽ�������⣬ʹÿ�������Ľⶼ��1/n�Ļ��ᱻ����
        				//����deltaδ�䣬���½�ʱ������delta��timesҲ������
        				else if(delta == best_delta){
        					if(random.nextInt(times++) == 0){
        						if(tabuTable[f_i][f_j] > moves){//���ò���������
            						//���ý��ɲ����ܸ���ȫ�����Ž⣬�ý�ɺ�ѡ��������ɽ�
            						if(current_conflict + delta < best_conflict){    							
            							move_i = f_i;
            							move_j = f_j;  
            						}
            					}
            					else{//��δ������,����÷ǽ��ɽ�
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
     * ��ʼ��
     */
    private void init(){
    	long start = System.currentTimeMillis();//��ʼʱ��
    	
    	//��ʼ����������⣬Ϊ�����
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
    	//��ʼ���������
    	ll_a = new LinkedList[colors];//��Ž������
    	ll_b = new LinkedList[colors];//��Ž������
    	ll_son = new LinkedList[colors];//��Ž������
    	for(i = 0 ; i < colors ; i++){
    		ll_a[i] = new LinkedList<Integer>();
    		ll_b[i] = new LinkedList<Integer>();
    		ll_son[i] = new LinkedList<Integer>();
    	}
    	
    	//��ʼ���ڽ���ɫ��AdjacentColorTable��Ϊָ��ָ��a��b
    	act_a = new int[nodes][colors];
    	act_b = new int[nodes][colors];
    	for(i = 0 ; i < nodes ; i++){//ȫ����
    		for(j = 0 ; j < colors ; j++){
    			act_a[i][j] = 0;
    			act_b[i][j] = 0;
    		}
    	}
    	//�����ڽ���ɫ��
    	for(i = 0 ; i < nodes ; i++){
    		for(j = 0 ; j < lor[i] ; j++){
    			act_a[i][solution_a[matrix[i][j]]]++;
    			act_b[i][solution_b[matrix[i][j]]]++;
    			//matrix����i���ھӣ�solution�����ھ���ɫ����i�ڽ���ɫ���еĶ�Ӧ��ɫ+1
    		}
    	}
    	
    	//�����ܳ�ͻ������ʼ����ѳ�ͻ��,�ڽӱ�����iȾɫ��ͬ���ھӸ��������
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

    	//��ʼ�����ɱ�
    	tabuTable_a = new long[nodes][colors];
    	tabuTable_b = new long[nodes][colors];
    	for(i = 0 ; i < nodes ; i++){//ȫ����
    		for(j = 0 ; j < colors ; j++){
    			tabuTable_a[i][j] = 0;
    			tabuTable_b[i][j] = 0;
    		}
    	}
    	    	
    	long end = System.currentTimeMillis();//����ʱ��
    	inittime = end - start;
    	
    	//printACT();
    	
    }
    
    /*
     * ��������ڽ���ɫ��
     */
    private void printACT(){
    	for(int i = 0 ; i < nodes ; i++){//ȫ����
    		System.out.print("Node"+i+"   ");
    		for(int j = 0 ; j < colors ; j++){
    			System.out.print(adjacentColorTable[i][j]+" ");
    		}
    		System.out.println();
    	}
    }
    
    /*
     * ������
     */
    private void testsolution(int[] solution){
    	int[][] act = new int[nodes][colors];
    	for(int i = 0 ; i < nodes ; i++){//ȫ����
    		for(int j = 0 ; j < colors ; j++){
    			act[i][j] = 0;
    		}
    	}
    	for(int i = 0 ; i < nodes ; i++){
    		for(int j = 0 ; j < lor[i] ; j++){
    			act[i][solution[matrix[i][j]]]++;
    			//matrix����i���ھӣ�solution�����ھ���ɫ����i�ڽ���ɫ���еĶ�Ӧ��ɫ+1
    		}
    	}
    	
    	int cf = 0;
    	for(int i = 0 ; i < nodes ; i++){
    		cf += act[i][solution[i]];
    	}
    	cf /= 2;
    	
    	System.out.println("�𰸳�ͻ����Ϊ��"+cf);
    	
    }
    
    private void printsolution(){
    	//������
    	System.out.println("���Ϊ");
    	for(int i = 0 ; i < nodes ; i++){
    		System.out.print("("+i+","+solution[i]+") ");
    	}
    	System.out.println();
    	System.out.println("��ʼ����ʱ��"+inittime+"ms");
    	System.out.println("��ʼ��ͻ����"+init_conflict_a+","+init_conflict_b);
    	System.out.println("�ܺ�ʱ��"+total_time+"ms");
    	System.out.println("����������"+gen);
    	double sectime = total_time/1000;
    	double timespers = gen/sectime;
    	System.out.println("ƽ��ÿ�����������"+timespers);
    	testsolution(solution);
    	
    }
    
    /*
     * ��������ʽ�Ľ�ת���ɰ���ɫ����Ķ�������ʽ����������ʾ����ͬ����ɫ�Ľڵ㹹��һ���������ڽ�������
     * ���ת��ͬʱ��¼������
     */
    int atl_i;
    private void arraytolinked(int[] s,LinkedList[] l){	
    	for(atl_i = 0 ; atl_i < nodes ; atl_i++){
    		l[s[atl_i]].add(atl_i);
    	}
    }
    
    /*
     * ����������ʽ�Ľ�ת����������ʽ��ʾ�����ڽ��������ͽ�����
     * ���ת����ͬʱ����������䳤��
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
