package tabusearch0;

import java.awt.Point;
import java.util.Random;

/*
 * ������������ѡ��ѵĽ��ɻ��߷ǽ����ƶ���
 * ����ѽ����ƶ����㡰����׼��ʱѡ����ѽ����ƶ�������ѡ��ǽ����ƶ���
 * ͬʱ�������ƶ���¼�ڽ��ɱ���
 * �㷨��ֹ����������⣬���ߴﵽ�趨�ĵ���������
 * 
 * ����׼����ѽ����ƶ���Ŀ��ֵС����ʷ��ѳ�ͻbest_conflict��С�ڵ�ǰ�ǽ����ƶ���ֵ,
 * 			��ɽ��ܽ����ƶ���ֵ��������ܷǽ����ƶ���ֵ��
 */
public class TabuSearch {
	private int[][] matrix;//��������Ϣ���ڽӾ���
	
	private int[] lor;//�����ڽӾ���ÿ�г��ȵ�����;
	
	private int nodes;//�ڵ����
	
	private long moves;//��ǰ��������
	
	private long maxmoves;//����������������ΪΪ��ֹ����֮һ
	
	private int colors;//Ŀ�����ɫ�����п��н�ʱ��ֹ����-1�ټ�����Ŀ��
	
	private int[] solution;//�⣬����Ϊ�ڵ���
	
	private int init_conflict;//��ʼ��ͻ��
	
	private int current_conflict;//��ǰ��ͻ��
	
	private int best_conflict;//��ʷ���Ž�ĳ�ͻ����
	
	/* adjacentColorTable���ڽ���ɫ��
	 * ��СΪ�ڵ���*Ŀ�����ɫ��
	 * Ԫ��ֵΪ�ڽڵ�i���ھ���ȾɫΪj�Ľڵ�ĸ�������jɫ�ĳ�ͻ��
	 * */
	private int[][] adjacentColorTable;
	
	/* tabuTable�����ɱ�
	 * ��СΪ�ڵ���*Ŀ�����ɫ��
	 * Ԫ��ֵΪ�������������iȾ����ɫj���ĵ�������
	 * ��ȡ���Ը���
	 * */
	private long[][] tabuTable;
	
	/* tabutenture�����ɱ�
	 * ��̬�������ͻ���йأ����ж����������
	 * ֵ=moves + f + random(1-10);
	 * */
	private int[][] tabutenture;
	
	/*********����findmove() makemove()�ı���**********/
	private int move_i,move_j;//��ʾ��ǰ���Ų�������i��Ⱦjɫ
	private int best_delta;//��ʾ��ǰ���Ų��������ĳ�ͻ���ı���
	private Random random = new Random();
	private boolean istabu;//�Ƿ�ִ���˽��ɲ���
	
	private long inittime;//��ʼ����ʱ
	private long tabumoves;//ִ�н��ɲ����Ĵ���
	private int noimprovetimes = 0;//�����޸��Ž�Ĵ���
	private int bestnotimprove = 0;//���Ž�δ���£��ҵ�ǰ�������������Ž���ۻ�����
	private int count = 0;
	int delta;//��ǰ���������ĳ�ͻ�仯
	int times;//����ͬ�������Ĵ�������һ��n���Ը���1/n����,ÿ�θ���best_deltaʱ����Ϊ1  	
	//�������������Խ��ڵ�i��Ϊjɫ
	int currentcolor;
	/*
	 * ������������ڽӾ��󣬽ڵ���
	 */
    public TabuSearch(int[][] mat ,int[] llor,int nn){
    	matrix = mat;
    	nodes = nn;
    	lor = llor;
   	}
    
    /*
	 * ���ô˺�����ʼִ���㷨
	 * ���������Ŀ�����ɫ��������������,����������Ϊ0��Ϊ�޴�������
	 */
    public void Start(int cc,long mm){
    	long start = System.currentTimeMillis();//��ʼʱ��
    	colors = cc;
    	maxmoves = mm;
    	tabumoves = 0;//ִ�н��ɲ����Ĵ������޹��㷨����ͳ��
    	init();
    	if(mm <= 0){//�޵����������ƣ���ֹ����Ϊ��ͻ��Ϊ0
    		while(best_conflict != 0){
    			findmove();
    			if(best_delta == Integer.MAX_VALUE){
    				System.out.println("����������������ɣ��޷����");
    				break;
    			}
    			makemove();
    			if(moves%200000 == 0)//ÿ20��ε������һ��״̬
    				System.out.println(moves+" "+best_conflict+","+current_conflict+","+init_conflict);
    			if(istabu)tabumoves++;
    			moves++;
    		}
    	}
    	else{//��ֹ����Ϊ��ͻ��Ϊ0 �� ����������������
    		while(best_conflict != 0 && moves <= mm){
    			findmove();
    			if(best_delta == Integer.MAX_VALUE){
    				System.out.println("����������������ɣ��޷����");
    				break;
    			}
    			makemove();
    			if(moves%100000 == 0)
        			System.out.println(moves+" "+best_conflict+","+current_conflict+","+init_conflict);
    			if(istabu)tabumoves++;
    			moves++;
    		}
    	}
    	long end = System.currentTimeMillis();//����ʱ��
    	//������
    	System.out.println("���Ϊ");
    	for(int i = 0 ; i < nodes ; i++){
    		System.out.print("("+i+","+solution[i]+") ");
    	}
    	System.out.println();
    	System.out.println("��ʼ����ʱ��"+inittime+"ms");
    	System.out.println("��ʼ��ͻ����"+init_conflict);
    	System.out.println("�ܺ�ʱ��"+(end - start)+"ms");
    	System.out.println("����������"+(moves-1));
    	double sectime = (end - start)/1000;
    	double timespers = moves/sectime;
    	System.out.println("ƽ��ÿ�����������"+timespers);
    	System.out.println("ִ�н��ɲ���������"+tabumoves);
    	testsolution();
    	
    }
    
    /*
     * ��������
     */
    int m_i,m_j;//Ϊ�˲���findmove��makemove�д����ֲ�����������Ч��
    private void makemove(){                                  
    		//�����ڽ���ɫ��,�����ڽӾ�����Ӱ����Ϊmove_i�������ھ�
        	for(m_i = 0 ; m_i < lor[move_i] ; m_i++){
        		m_j = matrix[move_i][m_i];//j��ʾmove_i��i���ھ�
        		adjacentColorTable[m_j][solution[move_i]]--;//��ɫ��ͻ��-1
        		adjacentColorTable[m_j][move_j]++;//��ɫ��ͻ��+1
        	}
        	//���µ�ǰ��ͻ
        	current_conflict += best_delta;
        	//�������Ž�
        	if(current_conflict < best_conflict){
        		best_conflict = current_conflict;
        	}
        	//���½�
        	solution[move_i] = move_j;
        	
        	if(best_delta >= 0)noimprovetimes++;
        	else noimprovetimes = 0;
        	//���½��ɱ�
        	tabuTable[move_i][move_j] = moves + current_conflict*random.nextInt(10)+1;
        								//+ noimprovetimes/20;
//        	tabuTable[move_i][move_j] = moves + current_conflict + random.nextInt(10)+1;
    }
    
    /*
     * ���ҿ��е�������
     * ִ�к�ı�ȫ�ֱ��������أ�best_delta , move_i , move_j
     */
    int f_i,f_j;//Ϊ�˲���findmove��makemove�д����ֲ�����������Ч��
    private void findmove(){
    	best_delta = Integer.MAX_VALUE;//��ʷ���Ŷ��������ĳ�ͻ���仯����ʼΪ����ֵ
    	delta = 0;//��ǰ���������ĳ�ͻ�仯
    	times = 1;//����ͬ�������Ĵ�������һ��n���Ը���1/n����,ÿ�θ���best_deltaʱ����Ϊ1  	
    	//�������������Խ��ڵ�i��Ϊjɫ
    	for(f_i = 0 ; f_i < nodes ; f_i ++){
    		currentcolor = solution[f_i];
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
                					istabu = true;
                					//һ������һ�����ɽ⣬best_deltaˮƽ��ߣ�֮�����н��ܵĽ�ض��ܸ���ȫ�ֽ�
        						}
        					}
        					else{//��δ������
        						move_i = f_i;
        						move_j = f_j;
            					best_delta = delta;		
            					times = 1;
            					istabu = false;
        					}
        				}
        				//���ò��������Ž�ͬ���ţ��ǵ�n����������1/n�ĸ��ʽ�������⣬ʹÿ�������Ľⶼ��1/n�Ļ��ᱻ����
        				//����deltaδ�䣬���½�ʱ������delta��timesҲ������
        				else if(delta == best_delta && random.nextInt(times++) == 0){    					
        					if(tabuTable[f_i][f_j] > moves){//���ò���������
        						//���ý��ɲ����ܸ���ȫ�����Ž⣬�ý�ɺ�ѡ��������ɽ�
        						if(current_conflict + delta < best_conflict){    							
        							move_i = f_i;
        							move_j = f_j;  
        							istabu = true;
        						}
        					}
        					else{//��δ������,����÷ǽ��ɽ�
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
     * ��ʼ��
     */
    private void init(){
    	long start = System.currentTimeMillis();//��ʼʱ��
    	//��ʼ����������
    	moves = 0;
    	
    	//��ʼ���⣬Ϊ�����
    	solution = new int[nodes];
    	for(int i = 0 ; i < nodes ; i++){
    		solution[i] = random.nextInt(colors);
    	}
    	
    	//��ʼ���ڽ���ɫ��
    	adjacentColorTable = new int[nodes][colors];
    	for(int i = 0 ; i < nodes ; i++){//ȫ����
    		for(int j = 0 ; j < colors ; j++){
    			adjacentColorTable[i][j] = 0;
    		}
    	}
    	for(int i = 0 ; i < nodes ; i++){
    		for(int j = 0 ; j < lor[i] ; j++){
    			adjacentColorTable[i][solution[matrix[i][j]]]++;
    			//matrix����i���ھӣ�solution�����ھ���ɫ����i�ڽ���ɫ���еĶ�Ӧ��ɫ+1
    		}
    	}
    	
    	//�����ܳ�ͻ������ʼ����ѳ�ͻ��,�ڽӱ�����iȾɫ��ͬ���ھӸ��������
    	best_conflict = 0;
    	for(int i = 0 ; i < nodes ; i++){
    		best_conflict += adjacentColorTable[i][solution[i]];
    	}
    	best_conflict /= 2;
    	init_conflict = best_conflict;
    	current_conflict = best_conflict;

    	//��ʼ�����ɱ�
    	tabuTable = new long[nodes][colors];
    	for(int i = 0 ; i < nodes ; i++){//ȫ����
    		for(int j = 0 ; j < colors ; j++){
    			tabuTable[i][j] = 0;
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
    private void testsolution(){
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
    
    //������ý�
    private void diversification(){
    	for(int i = 0 ; i < nodes ; i++){
    		solution[i] = random.nextInt(colors);
    	}
    	for(int i = 0 ; i < nodes ; i++){//ȫ����
    		for(int j = 0 ; j < colors ; j++){
    			adjacentColorTable[i][j] = 0;
    		}
    	}
    	for(int i = 0 ; i < nodes ; i++){
    		for(int j = 0 ; j < lor[i] ; j++){
    			adjacentColorTable[i][solution[matrix[i][j]]]++;
    			//matrix����i���ھӣ�solution�����ھ���ɫ����i�ڽ���ɫ���еĶ�Ӧ��ɫ+1
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
 * �Ľ�:��������ֻ���ǳ�ͻ����0�ĵ�
 */
