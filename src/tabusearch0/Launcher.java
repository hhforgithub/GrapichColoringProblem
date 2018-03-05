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
	private static long loadtime;//文件读取耗时
	
	public static void main(String[] args) {
		//读取文件,输出为二维数组实现的邻接矩阵（使用另一一维数组记录其每行长度）
		String filepath = ".\\Graph Coloring_instances\\DSJC500.5.col";
		Info info = readFile1(filepath);
		int[][] matrix = info.matrix;
		int[] lor = info.lor;
		int nodes = info.nodes;
		int edges = info.edges;
		/*测试输出邻接表
		for(int i = 0 ; i < lor.length ; i++){
			for(int j = 0 ; j < lor[i] ; j++){
				System.out.print(matrix[i][j]+" ");
			}
			System.out.println();
		}
		*/
		
		TabuSearch ts = new TabuSearch(matrix,lor,nodes);
		ts.Start(49, 0);
		
		System.out.println("文件读取耗时："+loadtime+"ms");
	}
	 
	public static Info readFile1(String path) { 
        long start = System.currentTimeMillis();//开始时间
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
                while (line != null) { //按行读数据至p开头行
                    //System.out.println(line);  
                	if(line.charAt(0) == 'p'){
                		String[] sFragment = line.split(" ");
                		size = Integer.parseInt(sFragment[2]);
                		edges = Integer.parseInt(sFragment[3]);
                		break;
                	}
                	line = bufferedReader.readLine();  
                }
                int[][] matrix = new int[size][size];//邻接矩阵
                int[] lor = new int[size];//lengthOfRow，记录matrix每行长度，也即matrix每行末尾插入位置的下标
        		for(int i = 0 ; i < size ; i++){
        			for(int j = 0 ; j < size ; j++){
        				matrix[i][j] = 0;
        			}
        			lor[i] = 0;
        		}
                while (line != null) {//按行读数据至末尾
                	if(line.charAt(0) == 'e'){
                		try{
                			String[] sFragment = line.split(" ");
                			int x = Integer.parseInt(sFragment[1])-1;//减1为了利用数组下标从0开始
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
                //关闭文件
                try {  
                    fileReader.close();  
                    bufferedReader.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
                long end = System.currentTimeMillis();//结束时间
                loadtime = end - start;          
            }              
        }
        System.out.println("读取失败"); 
        return null;
    } 

}
