package tabusearch0;
/*
 * 作为文件读取返回的数据类型
 * 包含邻接矩阵，记载邻接矩阵每行长度的数组，节点数（矩阵大小），边数
 */
public class Info {
	public int[][] matrix;
	public int[] lor;
	public int nodes;
	public int edges;
	
	public Info(int[][] matrixinput , int[] lengthofrow , int n ,int e){
		matrix = matrixinput;
		lor = lengthofrow;
		nodes = n;
		edges = e;
	}
}
