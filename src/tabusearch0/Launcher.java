package tabusearch0;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

public class Launcher {
	private static long loadtime;//�ļ���ȡ��ʱ
	
	public static void main(String[] args) {
		//��ȡ�ļ�,���Ϊ��ά����ʵ�ֵ��ڽӾ���ʹ����һһά�����¼��ÿ�г��ȣ�
		String filepath = ".\\Graph Coloring_instances\\DSJC500.5.col";
		Info info = readFile1(filepath);
		int[][] matrix = info.matrix;
		int[] lor = info.lor;
		int nodes = info.nodes;
		int edges = info.edges;
		/*��������ڽӱ�
		for(int i = 0 ; i < lor.length ; i++){
			for(int j = 0 ; j < lor[i] ; j++){
				System.out.print(matrix[i][j]+" ");
			}
			System.out.println();
		}
		*/
		
		TabuSearch ts = new TabuSearch(matrix,lor,nodes);
		ts.Start(49, 0);
		
		System.out.println("�ļ���ȡ��ʱ��"+loadtime+"ms");
	}
	 
	public static Info readFile1(String path) { 
        long start = System.currentTimeMillis();//��ʼʱ��
        File file = new File(path);  
        if (file.isFile()) {            
            BufferedReader bufferedReader = null;  
            FileReader fileReader = null;  
            try {  
                fileReader = new FileReader(file); 
                bufferedReader = new BufferedReader(fileReader);  
                String line = bufferedReader.readLine();
                int size = 0;
                int edges = 0;
                while (line != null) { //���ж�������p��ͷ��
                    //System.out.println(line);  
                	if(line.charAt(0) == 'p'){
                		String[] sFragment = line.split(" ");
                		size = Integer.parseInt(sFragment[2]);
                		edges = Integer.parseInt(sFragment[3]);
                		break;
                	}
                	line = bufferedReader.readLine();  
                }
                int[][] matrix = new int[size][size];//�ڽӾ���
                int[] lor = new int[size];//lengthOfRow����¼matrixÿ�г��ȣ�Ҳ��matrixÿ��ĩβ����λ�õ��±�
        		for(int i = 0 ; i < size ; i++){
        			for(int j = 0 ; j < size ; j++){
        				matrix[i][j] = 0;
        			}
        			lor[i] = 0;
        		}
                while (line != null) {//���ж�������ĩβ
                	if(line.charAt(0) == 'e'){
                		try{
                			String[] sFragment = line.split(" ");
                			int x = Integer.parseInt(sFragment[1])-1;//��1Ϊ�����������±��0��ʼ
                			int y = Integer.parseInt(sFragment[2])-1;
                			matrix[x][lor[x]] = y;
                			matrix[y][lor[y]] = x;
                			lor[x]++;
                			lor[y]++;
                		}
                		catch(NullPointerException e){
                			e.printStackTrace();
                			break;
                		}
                	}
                    line = bufferedReader.readLine();  
                }
                Info info = new Info(matrix,lor,size,edges);
                return info;
            } catch (FileNotFoundException e) {  
                e.printStackTrace();  
            } catch (IOException e) {  
                e.printStackTrace();  
            } finally {  
                //�ر��ļ�
                try {  
                    fileReader.close();  
                    bufferedReader.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
                long end = System.currentTimeMillis();//����ʱ��
                loadtime = end - start;          
            }              
        }
        System.out.println("��ȡʧ��"); 
        return null;
    } 

}
