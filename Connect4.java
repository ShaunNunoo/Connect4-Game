
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ArrayList;
import java.security.InvalidParameterException;
import java.util.Scanner;
import java.lang.Exception;
import java.awt.*;
import java.util.Arrays;
import java.lang.CloneNotSupportedException;

public class Connect4{
  
  /*Height of the connect4 game*/
  private int height = 6;
  
  /*Represents the number of cells that have been
   * filled in the game*/
  private int moveCount = 0;
  
  /*Length of the connect4 game*/
  private int length = 7;
  
  /*Boolean that stores if the game is over*/
  private boolean gameOver = false;
  
  /*A list of possible moves left to play in the game*/
  private ArrayList<Integer> possibleMoves = new ArrayList<>();
  
  /*String return of the winner of the game*/
  private String winner = "\nIts a draw";
  
  /*Data of connected nodes for player 1*/
  private ConnectionData player1 = new ConnectionData(1);
  
  /*Data of connected nodes for player 2*/
  private ConnectionData player2 = new ConnectionData(2);
  
  /*Represents the height of cells playerd in a specific
   * column of the game*/
  public int[] columnHeight;
  
  /*Represents the displayed image of the game state*/
  private String[] gameImage;
  
  /*Exception thrown when a column of the connect4 game is full*/
  private class FullCollumnException extends Exception{};
    
  /*Class that represents a state of connected node
   * used to determin the number of connections in 
     a row for a specific player*/ 
  private class ConnectionData implements Cloneable{
  
  /*Number that represents the player this Connection data
   * belongs to*/
  private int player;
  
  /*Stores the level of the player (if an AI) used to
   *determin the recursion depth for performing 
    minmax Algorithm operations*/
  private int level = 0;
  
  /*A reference to all the nodes in the game
  * for this player*/
  private Hashtable<Integer, Node> nodes = new Hashtable<>();
  
   
  /*Node that represents a connect4 coin
   * with references to adjacents nodes*/
  private class Node{
      
      /*Number of nodes to the top, bottm, left, right, and diagonal
       * to this specific node. The ordering goes as follows with index
       * number in brackets and each bracket is a cell:
         [0][1][2]
         [3]Nod[4]
         [5][6][7]*/
      private int[] adjacentNodes = {0,0,0,0,0,0,0,0};
      
      /*Updeates the node count of a specfic adjacent node represented by
       * the input index.*/
      public void updateNodeCount(int incremeant, int index){     
        adjacentNodes[index] += incremeant;
      }
      
      /*Returns the value of the adjacent node count at a specific inded*/
      public int adjacentNodeCount(int index){
        return adjacentNodes[index];
      }
      
      /*Returns the adjacentNodes array*/
      public int[] adjacentNodeData(){
        return adjacentNodes;
      }
   }
   
   /*Formula to convert a number a coordinateNumber to an index in the adjacentNodes array*/
   private int toIndex(int x){
    
    double formula = 0;//Variable used for formual calculations
    
    //Constants to iterate through for the formula
    double[] constants = {0.000868056, -0.0272817, 0.348958, -2.33472, 8.73698, -18.0868, 18.9132, -6.55119};
    
    //Iterating through each constant and adding it to formula
    for(int i = 0; i < 8; i++)
      formula += constants[7-i] * Math.pow(x,i + 1);

    return (int)formula;
   }
   
   /*Adds a node to the data class and returns a number
    *indicating the maxium connection adjacent connection
     count of making this addition*/
   private int addNode(int x, int y){
     Node node = new Node();//The node to be added
     nodes.put(x + y*length,node);//Adding a node to the hashtable of nodes
     int maxAdjacentNodeCount = 0;//Stores the maximum number of adjacent nodes;
     ArrayList<Integer> adjacentX = new ArrayList<>(); //List of nodes x values adjacent to this node
     ArrayList<Integer> adjacentY = new ArrayList<>(); //List of nodes y values adjacent to this node
  
     /*Itereting through all cells surronding the node at (x,y) to
      *find existing nodes and update adjacent node counts for 
       the new node*/
     for(int i = y - 1; i <= y + 1; i++)
       for(int j = x - 1; j <= x + 1; j++)
         if((i != y || j != x )&& i >= 0 && i < height && j >= 0 && j < length)/*excluding the position of the node itself*/{
           
           int adjNodeValue = j + i*length;//Converting a 2D coordinate (j, i) to a single counting value;    
           int coordinateToNum = j-x + 3*(i-y) + 4;//Formula to convert (j - x, i - y) coordinate to a unique number form 0 - 7
           int toIndex = toIndex(coordinateToNum);//Converting coordinate count to an array index form 0 - 7
           
           if(nodes.get(adjNodeValue) != null)/*If the adjacent node exists*/{
             node.updateNodeCount(1 + nodes.get(adjNodeValue).adjacentNodeCount(toIndex), toIndex);
             adjacentX.add(j-x);
             adjacentY.add(i-y);
           
           int adjacentNodeCount = node.adjacentNodeCount(toIndex) + node.adjacentNodeCount(7 - toIndex) + 1;
           
           if( maxAdjacentNodeCount < adjacentNodeCount )/*if there is a larger node count*/
             maxAdjacentNodeCount = adjacentNodeCount;
           
           
           }
         }
       
     
     
       /*Itereting through all cells surronding the node at (x,y) and
        * updating those adjacent nodes data for this added node*/
       for(int i = 0; i < adjacentX.size(); i++){

             int xCursor = x + adjacentX.get(i);//Reference  to a x coordinate in the grid of data
             int yCursor = y + adjacentY.get(i);//Reference to a y coordinate in the grid of data
             int coordinateToNum =  adjacentX.get(i) + 3*adjacentY.get(i) + 4;//Formula to convert (j - x, i - y) coordinate to a unique number form 0 - 7
             int toIndex = toIndex(coordinateToNum);//Converting coordinate count to an array index form 0 - 7
                         
             Node nodeCursor = nodes.get(xCursor + yCursor * length); //Reference to the node at (xCursor,yCursor) in the data grid
             /*Iterating and updating value while there is a continuous adjacent node*/
             while(nodeCursor != null){
               nodeCursor.updateNodeCount(1 + node.adjacentNodeCount(7 - toIndex), 7 - toIndex);
               xCursor +=  adjacentX.get(i);//Updating xCursor
               yCursor +=  adjacentY.get(i);//Updating yCursor
               nodeCursor = nodes.get(xCursor + yCursor * length);//Updating nodeCursor
             }
           
       }
     
     
     return maxAdjacentNodeCount;
   }
   
   /*Removes a node in the data class and returns a true if
    * it was successfuly removed*/
   private boolean removeNode(int x, int y){
     Node node = nodes.get(x + y*length);
     if(node == null) /*If node does not exist*/
       return false;
        
     nodes.remove(x + y*length);//Removing the node form list of nodes
      
       /*Itereting through all cells surronding the node at (x,y) and
        * updating those adjacent nodes data for this added node*/
       for(int i = y - 1; i <= y + 1; i++)
         for(int j = x - 1; j <= x + 1; j++){
           if((i != y || j != x) && i >= 0 && i < height && j >= 0 && j < length)/*excluding the position of the node itself*/{
             
             int xCursor = j;//Reference to a x coordinate in the grid of data
             int yCursor = i;//Reference to a y coordinate in the grid of data
             int coordinateToNum = j-x + 3*(i-y) + 4;//Formula to convert (j - x, i - y) coordinate to a unique number form 0 - 7
             int toIndex = toIndex(coordinateToNum);//Converting coordinate count to an array index form 0 - 7
                         
             Node nodeCursor = nodes.get(xCursor + yCursor * length); //Reference to the node at (xCursor,yCursor) in the data grid
             /*Iterating and updating value while there is a continuous adjacent node*/
             while(nodeCursor != null){
               nodeCursor.updateNodeCount(-1 - node.adjacentNodeCount(7 - toIndex), 7 - toIndex);
               xCursor += j-x;//Updating xCursor
               yCursor += i-y;//Updating yCursor
               nodeCursor = nodes.get(xCursor + yCursor * length);//Updating nodeCursor
             }
           }
        
       }
     
     
     return true;
   }
   
   /*Constructor that initializes the player for this connection data*/
   public ConnectionData(int player){
    this.player = player;
   }
   
   /*Creates a clone of the data*/
   public Object clone() throws CloneNotSupportedException{
        return super.clone();
   }
   
   /*Returns the level of the player*/
   public int level(){
     return level;
   }
   
   /*Updates the level of the player*/
   public void changeLevel(int level){
     this.level = level;
   }
   
   /*Returns the player for this connection data*/
   public int player(){
     return player;
   }
   
   /*Adds a node at a specific x position along the length
    *of the connect 4 game.*/   
   public int play(int x, int[] columnHeight, ArrayList<Integer> possibleMoves){  
     moveCount++;//Increasing move count
     
     // System.out.println("Add at: " + x + ", " + (height - columnHeight[x] - 1));
     int returnVal = addNode(x, height - columnHeight[x] - 1); //adding a node to the data class
     
     
     if(columnHeight[x] < height)
     columnHeight[x] += 1;//Increasing column height at x
     
     if(columnHeight[x] == height)//If column at x is full, remove x form possibleMoves
       possibleMoves.remove(new Integer(x));

     return returnVal;
     
   }
   
   /*Removes a node from a specfic x position along the length
    * of the connect 4 game*/   
   public boolean undo(int x, int[] columnHeight, ArrayList<Integer> possibleMoves){  
     
     moveCount--;//Decreasing move count
     
     if(!possibleMoves.contains(new Integer(x)))/*If x is not a possible move when removing*/{
       int index = 0;//Index to insert removed element
       
       //Iterating through possible moves to insert x in order
       while( index < possibleMoves.size() && possibleMoves.get(index) < x)
         index++;
       
       possibleMoves.add(index, x);
     }
     
     boolean returnVal = removeNode(x, height - columnHeight[x]);//removing a node from the data class
    // System.out.println("Remove at: " + x + ", " + (height - columnHeight[x] ));
     
     if( columnHeight[x] > 0)
       columnHeight[x] -= 1;//Decreasing column height at x
     else return false;
     
     return returnVal;
   }
   
   /*Prints a representation of the connectionData in the form:
    *  <0,0,0 ...>, [0,0,0 ...], [0,0,0 ...], [0,0,0 ...]...
       [0,0,0 ...], <0,0,0 ...>, [0,0,0 ...], <0,0,0 ...>
       <0,0,0 ...>, <0,0,0 ...>, <0,0,0 ...>, [0,0,0 ...]
       .
       .
       .
       where <> represents a node and [] is empty space
       The numbers represent data of adjacent nodes count for 
       each node
    */
   public void displayData(){

     for(int i = 0; i < height; i++){
       for(int j = 0; j < length; j++){
         int index = j + i*length;//Converting coordinate (j,i) into an index count    
         if(nodes.get(index) != null)
           System.out.print("<");
         else
           System.out.print("[");
           
           for(int k = 0; k < 8; k++)
             if(nodes.get(index) != null)/*if the node exists at (j,i)*/
               System.out.print(nodes.get(index).adjacentNodeData()[k]+", ");
             else
               System.out.print("0, "); 
             
           if(nodes.get(index) != null)
             System.out.print(">, ");
           else
             System.out.print("], ");
       }  
       System.out.println();  
     }     
   }
  }
  
  /*Constructor that initializes the height and ;ength of the game*/
  public Connect4(int length, int height){
    gameImage = new String[height];//Initializing game image
    this.length = length;
    this.height = height;
    columnHeight = new int[length];
    /*Creating game image*/
    for(int i = 0; i < height; i++){
      StringBuilder builder = new StringBuilder("");
      for(int j = 0; j < length; j++){
        builder.append("[ ]");
        if(i == 0)
          possibleMoves.add(j);
      }
      
      gameImage[i] = builder.toString();
    }   
  }
  
  /*Prints the winner of the game to the screen*/
  public void winner(){
    System.out.print(winner);
  }
  
  /*Prints the game image to teh screen*/
  public void print(){
    for(int i = 0; i < height; i++)
       System.out.println(gameImage[i]);
    
    for(int i = 0; i < length; i++)
       System.out.print(" " + i + " ");
    
    System.out.println();  
  }
  
  /*Updates the game image*/
   public void updateGame(int player, int x){
    StringBuilder builder = new StringBuilder("");
      
    int y = height - columnHeight[x] - 1; 
    for(int j = 0; j < 3 * length; j++)
      if( 3*x + 1 == j)
        builder.append((player == 1)?'O':'X');
      else builder.append(gameImage[y].charAt(j));
                          
    gameImage[y] = builder.toString();                  
  }
  
   /*Returns if the game is over*/
   public boolean gameOver(){
     return gameOver;
   }
   
   /*Retursn the data of a specific player in the game*/
   public ConnectionData getPlayer(int player){
     if(player == 1)
       return player1;
     else if(player == 2)
       return player2;
     else return null;
   }
   
   public int getMove(int player){    
     double bestScore = (player == 2)?Double.NEGATIVE_INFINITY:Double.POSITIVE_INFINITY;
     ArrayList<Integer> bestMoves = new ArrayList<>(); //A list of best moves with the same score
    // System.out.println("Player: " + player);
     for(int i = 0; i < possibleMoves.size(); i++){
       int move = possibleMoves.get(i);
       double score = minmax(player, move, player1, player2, possibleMoves, columnHeight, (player == 2)?true:false, ((player == 2)?player2:player1).level(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY );
      ((player == 2 )?player2:player1).undo(move, columnHeight, possibleMoves);//Undoing adding the index to the connection data
       //System.out.println("Score: " + score +" move: " + move);
      
       if((player == 2)?(score > bestScore):(score < bestScore)){
        bestScore = score;
        bestMoves.clear();
        bestMoves.add(move);
      } else if (score == bestScore)
        bestMoves.add(move);
     }
   
  
    return bestMoves.get((int)(Math.random()*(bestMoves.size()-1)));
  
 }
    

  public double minmax(int player, int moveIndex, ConnectionData player1, ConnectionData player2, ArrayList<Integer> possibleMovesRef, int[] columnHeight, boolean maximizingPlayer, int depth, double alpha, double beta){
    //Represents the maximum number of adjacent connections in the game
    int playScore = ((maximizingPlayer)?player2:player1).play(moveIndex, columnHeight, possibleMovesRef);
    
    //If the game has ended or max recursion depth have been reached
    if(depth == 0 || possibleMovesRef.size() == 0 || playScore >= 4){ 
      if((player == 2 && !maximizingPlayer && depth == player2.level())) 
        return  Double.POSITIVE_INFINITY;
      else if (player == 1 && maximizingPlayer && depth == player1.level())
        return Double.NEGATIVE_INFINITY;
    
      int scoreValue = 0;//Evaulation value based on playScoure count
        switch(playScore){
          case 2:
            scoreValue = 50;
            break;
          case 3:
            scoreValue = 500;
            break;
          default:
            scoreValue = 50000;
            break;
        }
         return ((maximizingPlayer)?scoreValue :-1* scoreValue) * (1 + height*length - moveCount);//If there winner, return a positive or negative evaluations based1 on who is playing
     
    }
    
    //If maximizing player is playing
    if(!maximizingPlayer){
      double maxEval = Double.NEGATIVE_INFINITY;  
      for(int i = 0; i < possibleMovesRef.size(); i++){
        
        int moves = possibleMovesRef.get(i);
        double eval = minmax(player, moves, player1, player2, possibleMovesRef, columnHeight, true , depth - 1, alpha, beta); 
        player2.undo(moves, columnHeight, possibleMovesRef);//Undoing adding the index to the connection data  
        maxEval = Math.max(maxEval, eval);
        alpha = Math.max(alpha, eval);      
        if(beta <= alpha)
          break;
      }
      return maxEval - ((player1.level() < 3)?0:1)*(((moveIndex == (int)(Math.ceil((double)(length + 1)/2.0 - 1.0)) || moveIndex == (int)(Math.floor((double)(length + 1)/2.0 - 1.0)))?60:0)+((playScore == 3 && player == 2)?1000:0)+((playScore == 2 && player == 2)?50:0))* (1 + height*length - moveCount);//reward for playing in center of game;;
    } else {     
       double minEval = Double.POSITIVE_INFINITY; 
       for(int i = 0; i < possibleMovesRef.size(); i++){
         int moves = possibleMovesRef.get(i);
         double eval = minmax(player, moves, player1, player2, possibleMovesRef, columnHeight, false, depth - 1, alpha, beta); 
         player1.undo(moves, columnHeight, possibleMovesRef);//Undoing adding the index to the connection data \
          
         minEval = Math.min(minEval, eval); 
         beta = Math.min(beta, eval);        
         if(beta <= alpha)
           break;      
      } 
       
       return minEval + ((player2.level() < 3)?0:13)*(((moveIndex == (int)(Math.ceil((double)(length + 1)/2.0 - 1.0)) || moveIndex == (int)(Math.floor((double)(length + 1)/2.0 - 1.0)))?60:0)-((playScore == 3 && player == 1)?1000:0)-((playScore == 2 && player ==1)?50:0))* (1 + height*length - moveCount);//reward for playing in center of game;
    }

  }
   
   /*Plays at a specific index for a specific player. Throws FullCollumnException if
     the column being played at is full*/
   public void play(int player, int index)throws FullCollumnException{
     if(height - columnHeight[index] - 1 < 0 )//Throwing exception if column is full
       throw new FullCollumnException();  
     
     updateGame(player, index);//Updating the game image
     
     if(player == 1)
       if(player1.play(index, columnHeight, possibleMoves) >= 4 ){
         print();
         //player1.displayData();
         gameOver = true;//Setting gameOver to true if the play made 4 connections
         winner = "\nPlayer 1 won!";//Setting winner to player 1
         
       }
     
     if(player == 2)
       if(player2.play(index, columnHeight, possibleMoves) >= 4 ){
         print();
         //player2.displayData();
         gameOver = true;//Setting gameOver to true if the play made 4 connections
         winner = "\nPlayer 2 won!";//Setting winner to player 2              
       }
     
     if(moveCount == length*height){
       print();
       gameOver = true;//Setting gameOver to true if there are no more moves left
     }
     
   }
   
  /*Returns the length of the game*/
   public int length(){
    return length;
  }
   
   public static void main(String[] args){
     Connect4 game = new Connect4(7,6);
    int player = 0;
    int gameMode = 0;
    boolean gameStart = false; 
    boolean AILevelsInitiated = false;
    boolean firstAIlevelDetermined = false;
    
    System.out.print("************CONNECT41!************\n\nChose what game mode you want by\ntyping in the number next to it\n\n1. Player VS Player\n2. Player VS AI \n3. AI VS AI\n\n");
     
    while(!gameStart){  
      try{
        Scanner input = new Scanner(System.in);      
        int playCommand = Integer.parseInt(input.nextLine()); 
        
        if(playCommand < 0 || playCommand > 3)
          System.out.println("\nINVALID INPUT! Enter 1, 2 or 3 for one of the above play options.\n");
        else{
          gameMode = playCommand;
          gameStart = true;
        }  
        
      }catch(NumberFormatException e){
        System.out.println("\nINVALID INPUT! Enter 1, 2 or 3 for one of the above play options.\n");
      }
    }
    

    while(!game.gameOver() && gameStart){  
      if(gameMode == 1 )
        AILevelsInitiated = true;
      else if (gameMode == 2 && !AILevelsInitiated){
        System.out.print("Input AI Level (0 - 10): ");
        try{
          Scanner input = new Scanner(System.in);      
          int level = Integer.parseInt(input.nextLine());         
          game.getPlayer(2).changeLevel(level);
          AILevelsInitiated = true;

          
        }catch(NumberFormatException e){
          System.out.println("\nINVALID INPUT! Enter a level between  0 and 100\n");
        }
      } else if (gameMode == 3 && !AILevelsInitiated){
        
        if(!firstAIlevelDetermined)
          System.out.print("Input First AI Level (0 - 10): ");
        else
          System.out.print("Input Second AI Level (0 - 10):");
        try{
          Scanner input = new Scanner(System.in);      
          int level = Integer.parseInt(input.nextLine());         
          game.getPlayer((!firstAIlevelDetermined)?1:2).changeLevel(level);
          if(firstAIlevelDetermined)
            AILevelsInitiated = true;       
            firstAIlevelDetermined = true;   
        }catch(NumberFormatException e){
          System.out.println("\nINVALID INPUT! Enter a level from  0 to 10\n");
        }
        
      }
      
      if(AILevelsInitiated){
        game.print();
        if(gameMode == 1)
          System.out.println("\nPlayer " + (1+player) + " is playing!");
        else if(gameMode == 2 && player == 0)
          System.out.println("\nPlayer 1 is playing!");
         
        
        try{
          int playIndex = 0; 
          
          if(gameMode == 1){
            Scanner input = new Scanner(System.in);  
            playIndex = Integer.parseInt(input.nextLine());
          } else if (gameMode == 2){
              Scanner input;
              if(player == 0){
                input = new Scanner(System.in);
                playIndex = Integer.parseInt(input.nextLine());
              } else{
               System.out.println("\nAI (Level " + game.getPlayer(2).level() + ") is thinking ...");
               long prevTime = System.currentTimeMillis();
               playIndex = game.getMove(2);
               long deltaTime = System.currentTimeMillis() - prevTime;
               try{
                Thread.sleep((deltaTime < 4000 )?4000-deltaTime:0);
              }catch(InterruptedException ex){}
               System.out.println("\nAI has played at column: ["+ playIndex +"]\n");
              }
          } else if (gameMode == 3){
            
            System.out.println("\nPlayer " + (player + 1) + " AI (Level " + game.getPlayer(player + 1).level() + ") is thinking ...");           
            long prevTime = System.currentTimeMillis();
            playIndex = game.getMove(player + 1);
            long deltaTime = System.currentTimeMillis() - prevTime;
            try{
              Thread.sleep((deltaTime < 4000 )?4000-deltaTime:0);
            }catch(InterruptedException ex){}
            
            System.out.println("\nPlayer " + (player + 1) + " AI has played at column: ["+ playIndex +"]\n");
          }
          game.play(player + 1, playIndex);   
          player++;
          player = player%2;
        }catch(NumberFormatException e){
          System.out.println("\nINVALIDINPUT! Enter a number between 0 and "+(game.length()-1) + " to play!\n");
        }catch(ArrayIndexOutOfBoundsException e){
          System.out.println("\nINVALID INPUT! Enter a number between 0 and "+(game.length()-1) + " to play!\n");
        }catch(FullCollumnException e){
          System.out.println("Cannot play at this column because it is full! Chose another column.\n");
        }
      }
      
    }
    
    game.winner();
  }
  

}