package tabusearch0;
/*
 * ��Ϊ�ļ���ȡ���ص���������
 * �����ڽӾ��󣬼����ڽӾ���ÿ�г��ȵ����飬�ڵ����������С��������
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
