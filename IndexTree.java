/* This class has the implementation of BplusTree and the corresponding 
 * operations like create, insert, find, list on the index.
 * The index contain information about about the data file( where the original 
 * records are present)
 * 
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class IndexTree<Key extends Comparable<Key>, Value> {
	private static final int N = 4;
	private static BufferedWriter insertbw;
	private static BufferedWriter databw;
	Node root;
	int m,height;
/*
 * Constructor for the class which declares the number of node as zero
 */
	public IndexTree() {
		root = new Node(0);
	}
/*
 * To iterate over the array entries we have a helper.
 * The left node has key and value
 * other non leaf nodes have key and pointer.
 * Hence we define the EntryClass which hass all these there variable, so we can use whichever we want.
 */
	private static class EntryClass {
		private Comparable key;
		private final Object value;
		private Node nextNode;     
		public EntryClass(Comparable key, Object value, Node nextNode) {
			this.key  = key;
			this.value  = value;
			this.nextNode = nextNode;
		}
	}
/*
 * This method return the height of the B+ tree
 */
	public int height() {
		return height;
	}

	/*
	 * This method splits the given node in the tree into two separate nodes
	 */
	private Node splitTree(Node givenNode) {
		givenNode.k = N/2;   
		Node nd = new Node(N/2);
		for (int x = 0; x < N/2; x++){
			nd.child[x] = givenNode.child[N/2+x]; 
		}
		return nd;    
	}
/*
 * This method given key and value, will insert the key into the tree. It check if the node
 * is full if not it adds the key value pair to leaf node if not it split the node.
 */
	private Node insert(Node head, Key key, Value value, int heigt) {
		EntryClass e = new EntryClass(key, value, null);
		int i=0;
		if (heigt == 0) {
			for (i = 0;i<head.k;i++) {
				if (isless(key,head.child[i].key)){
					break;
				}
			}
		} else {
			for (i=0;i<head.k;i++) {
				if ((i+1==head.k)||(isless(key,head.child[i+1].key))) {
					Node newNode=insert(head.child[i++].nextNode, key, value, heigt-1);
					if (newNode == null) 
					{
						return null;
					}
					e.key = newNode.child[0].key;
					e.nextNode = newNode;
					break;
				}
			}
		}

		for (int j = head.k; j > i; j--){
			head.child[j] = head.child[j-1];
		}
		head.k++;
		head.child[i] = e;
		if (head.k < N){
			return null;
		} else {
			return splitTree(head);
		}
	}

/*
 * This method calls the above insert method to insert the key value pair to right node
 */

	public void put(Key key, Value value) throws Exception {
		if (key == null)
		{
			throw new Exception("key is null");
		}
		Node newnode = insert(root,key,value,height); 
		m++;
		if (newnode == null)
		{
			return;
		}
		Node tree = new Node(2);
		tree.child[0] = new EntryClass(root.child[0].key,null,root);
		tree.child[1] = new EntryClass(newnode.child[0].key,null,newnode);
		root = tree;
		height++;
	}
/*
 * Given two values this method compare key1 and key2 and return true if key1 is lesser than key2
 * 
 */
	private boolean isless(Comparable key1, Comparable key2) {
		return (key1.compareTo(key2)<0);
	}
	
	/*
	 * This method performs a search operation to find the value for the given key
	 */

	public Value get(Key key) throws Exception {
		if (key==null){
			throw new Exception("Not a valid Key");
		}
		return search(key,root,height);
	}

/*
 * Search through the tree until it reaches the leaf nodes using the next pointer
 * return the Value after finding the value of the given key. 
 * 
 * 
 */
	private Value search(Key key,Node x,int heigt) {
		EntryClass[] children = x.child;
		if (heigt == 0) {
			for (int j = 0; j < x.k; j++) {
				if (equals(key, children[j].key))
				{
					@SuppressWarnings("unchecked")
					Value value = (Value) children[j].value;
					return value;
				}
			}
		} else {
			for (int j = 0; j < x.k; j++) {
				if ((j+1==x.k)||isless(key, children[j+1].key))
					return search( key,children[j].nextNode,heigt-1);
			}
		}
		return null;
	}


/*
 * This method checks if both the key1 and key2 are equal
 */

	private boolean equals(Comparable key1, Comparable key2) {
		return (key1.compareTo(key2)==0);
	}
/*
 * This method return the size of the bplustree
 */
	public int size() {
		return m;
	}

	/*
	 * This method checks if the Bplus tree is empty
	 * 
	 */
	public boolean isEmpty() {
		boolean isEmpty=false;
		if(size()==0){
			isEmpty=true;
		}
		return isEmpty;	 
	}
/*
 * Main method handle all the operations like create,list,find,insert.
 * 
 */
	public static void main(String args[]) throws Exception{
		String operation=args[0];
		//String operation="-list";
		String dataFilename="";
		switch(operation){
		case "-create":
			String dataFileName=args[1];
					String indexFileName=args[2];
					int indexSize=Integer.parseInt(args[3]);
			 /*

			String dataFileName="data.txt";
			String indexFileName="indexfile.txt";*/
			//int indexSize=15;
			File indexFile=new File(indexFileName);
			if(!indexFile.createNewFile()){
				indexFile.delete();
				indexFile.createNewFile();
			}
			IndexTree<String, String> tree = new IndexTree<String, String>();
			String line = null;
			RandomAccessFile fileStore=null;
			BufferedWriter bw=null;
			FileOutputStream fos=null;
			try {
				fileStore = new RandomAccessFile(dataFileName, "rw");
				long filePtr=fileStore.getFilePointer();
				while((line = fileStore.readLine()) != null) {

					String key=line.substring(0, indexSize);
					if(key.length()!=0){
						if(tree.get(key)==null){
						tree.put(key,filePtr+"");
						filePtr=fileStore.getFilePointer();
						}
					}
				}
				fos = new FileOutputStream(indexFileName);
				bw = new BufferedWriter(new OutputStreamWriter(fos));
		//		bw.write("TextfileName "+dataFileName);
		//		bw.newLine();
				tree.printTree(bw);
                 bw.write("TextfileName "+dataFileName);		
		bw.newLine();

				System.out.print("Successfully created index file "+indexFileName);
			}
			catch(FileNotFoundException ex) {
				System.out.println("Unable to open file '" + dataFileName + "'");                
			}
			catch(IOException ex) {
				System.out.println("Error reading file '"+ dataFileName + "'");                  
				ex.printStackTrace();
			} finally{
				bw.close();
				fos.close();

			}

			break;
		case "-find":
			String indexFName=args[1];
			String key=args[2];
			//String indexFName="indexfile.txt";
			//String key="17917647482952Z";
			FileInputStream inputStream = null;
			Scanner sc = null;
			long position=0;

			try {
				inputStream = new FileInputStream(indexFName);
				sc = new Scanner(inputStream, "UTF-8");
				while (sc.hasNextLine()) {
					String indexline = sc.nextLine();
					int indexspace=indexline.indexOf(" ");
					//System.out.println(indexline);
					String keyFromFile=indexline.substring(0,indexspace);
					String valueFromFile=indexline.substring(indexspace+1);
					if(key.equals(keyFromFile)){
						position=Long.parseLong(valueFromFile);
						//break;
					}
					if("TextfileName".equals(keyFromFile)){
						dataFilename=valueFromFile;
					//	System.out.println(dataFilename);
						}
				}
				if(position==0)
				{
					System.out.println("Unable to find the given key in the index file "+indexFName);
				}else{
					try {
						//System.out.println(dataFilename);
						RandomAccessFile fStore = new RandomAccessFile(dataFilename, "rw");
						fStore.seek(position);
						System.out.println("At "+position+", record: "+fStore.readLine());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// note that Scanner suppresses exceptions
				if (sc.ioException() != null) {
					throw sc.ioException();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			finally {
				if (inputStream != null) {
					inputStream.close();
				}
				if (sc != null) {
					sc.close();
				}
			}

			break;

		case "-insert":
			try {
				String indexfileName=args[1];
String inputrecord=args[2];
//System.out.println(" Given Input "+inputrecord);
			String addkey=inputrecord.substring(0,inputrecord.indexOf(" "));
//System.out.println(" key "+addkey);			
String addrecord=inputrecord.substring(inputrecord.indexOf(" ")+1);
				//String indexfileName="indexfile.txt";
				long recordpos=0;
		//		for(int i=3;i<args.length;i++)
		//	addrecord+=args[i]+" ";
				/*String addkey="17917647482952Z";
				String addrecord="MY New second REcord";*/
				long pos=0;
				IndexTree<String, String> btree = new IndexTree<String, String>();
				FileInputStream iStream = new FileInputStream(indexfileName);
				sc = new Scanner(iStream, "UTF-8");
	int count=1;			
				while (sc.hasNextLine() ) {
					String indexline = sc.nextLine();
					
					int indexspace=indexline.indexOf(" ");
					
					String keyFromFile=indexline.substring(0,indexspace);
//System.out.println(keyFromFile.length()+" "+addkey.length()+" ");       
if(!(keyFromFile.length() >=addkey.length()) && (count==2) ){
pos=1;
System.out.println("Invalid key !");
break;
}
count++;
					String valueFromFile=indexline.substring(indexspace+1);
					btree.put(keyFromFile, valueFromFile);
					if(addkey.equals(keyFromFile)){
						pos=Long.parseLong(valueFromFile);	 
						System.out.println("Key already exists, so we cannot add "+addkey+" "+addrecord);
						break;
					}
					if("TextfileName".equals(keyFromFile)){
						dataFilename=valueFromFile;
					}
				
					} 

				if(pos==0){

					fileStore = new RandomAccessFile(dataFilename, "rw");
					long filelength=fileStore.length()-1;
					fileStore.seek(filelength);
					while((line = fileStore.readLine()) != null) {
						recordpos=fileStore.getFilePointer();
					}
					btree.put(addkey,recordpos+"");
					FileWriter datafos = new FileWriter(dataFilename,true);

					databw = new BufferedWriter(datafos);
					databw.write(addkey+" "+addrecord);
					databw.newLine();

					FileWriter insertfos = new FileWriter(indexfileName);

					insertbw = new BufferedWriter(insertfos);
					btree.printTree(insertbw);
					//insertbw.newLine();
					System.out.println("Record "+addkey+" "+addrecord+"     added successfully");
				}
			}catch(Exception e){
				e.printStackTrace();
			} finally{
				if(databw!=null){
					databw.close();
				}
				if(insertbw!=null){
					insertbw.close();
				}

			}
			break;
		case "-list":
			try {
			String indexfile=args[1];
			String startingKey=args[2];
			int count=Integer.parseInt(args[3]);
			/*	String indexfile="indexfile.txt";
				String startingKey="99273424429835A";
				int count=5;
				*/
			FileInputStream iStream = new FileInputStream(indexfile);
			sc = new Scanner(iStream, "UTF-8");
			long pos=0;
			IndexTree<String, String> btree = new IndexTree<String, String>();
			FileInputStream iStream1 = new FileInputStream(indexfile);
			sc = new Scanner(iStream, "UTF-8");
			while (sc.hasNextLine() ) {
				String indexline = sc.nextLine();
				int indexspace=indexline.indexOf(" ");
				String keyFromFile=indexline.substring(0,indexspace);
				String valueFromFile=indexline.substring(indexspace+1);
				btree.put(keyFromFile, valueFromFile);
			}
			//System.out.println("text file"+btree.get("TextfileName"));
			FileInputStream iStream2 = new FileInputStream(indexfile);
			Scanner sc1 = new Scanner(iStream2, "UTF-8");
			fileStore = new RandomAccessFile(btree.get("TextfileName"), "rw");
			while (sc1.hasNextLine() ) {
				
				String indexline = sc1.nextLine();
				int indexspace=indexline.indexOf(" ");
				String keyFromFile=indexline.substring(0,indexspace);
				String valueFromFile=indexline.substring(indexspace+1);
				//System.out.println(startingKey.equals(keyFromFile)+" "+(startingKey.compareTo(keyFromFile)<0));
				if((startingKey.equals(keyFromFile) || startingKey.compareTo(keyFromFile)<0)&& count>0){
					pos=Long.parseLong(valueFromFile);	
				//	System.out.println("pos "+pos);
					fileStore.seek(pos);
					System.out.println(fileStore.readLine());
					count--;
				}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			finally{
				//fileStore.close();
			}
			break;
                  default:
System.out.println(" Enter a valid operation");
		}
	
}
	public void printTree(BufferedWriter bw) throws IOException{
		printTree(root, height,bw);
	}
/*
 * Each node is represented by this class. Each node has as EntryClass array of children
 * 
 */
	private static final class Node {
		int k;                            
		private EntryClass[] child = new EntryClass[N];   
		private Node(int m) {
			k = m;
		}
	}
/*
 * This method prints the entire bplus tree to the bufferedWriter
 * 
 */
	private void printTree(Node head, int height,BufferedWriter bw) throws IOException {
		EntryClass[] children = head.child;
		if (height == 0) {
			for (int j = 0; j < head.k; j++) {
				bw.write(children[j].key+" "+children[j].value);
				bw.newLine();

			}
		}
		else {
			for (int j = 0; j < head.k; j++) {
				printTree(children[j].nextNode, height-1,bw);
			}
		}
	}

}
