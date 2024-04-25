package main;
import piece.*;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import java.awt.image.*;

public class GamePanel extends JPanel implements Runnable {

    public static final int WIDTH=1100;
    public static final int HEIGHT=800;
    final int FPS=60;
    Thread gameThread;
    Board board=new Board();
    Mouse mouse= new Mouse();

    public static final int WHITE=0;
    public static final int BLACK=1;
    int currentColor=WHITE;

    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameover;
    boolean stalemate;

    public static ArrayList<Piece> Pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promoPieces = new ArrayList<>();
 Piece activeP,checkingP;
 public static Piece castlingP;
    public GamePanel(){
        setPreferredSize(new Dimension(WIDTH,HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        setPieces();
        copyPieces(Pieces,simPieces);
    }
    public void launchGame(){
        gameThread=new Thread(this);
        gameThread.start();
    }
    public void setPieces(){
   //White team
      Pieces.add(new Pawn(WHITE,0,6));
      Pieces.add(new Pawn(WHITE,1,6));
        Pieces.add(new Pawn(WHITE,2,6));
        Pieces.add(new Pawn(WHITE,3,6));
        Pieces.add(new Pawn(WHITE,4,6));
        Pieces.add(new Pawn(WHITE,5,6));
        Pieces.add(new Pawn(WHITE,6,6));
        Pieces.add(new Pawn(WHITE,7,6));
        Pieces.add(new Knight(WHITE,1,7));
        Pieces.add(new Knight(WHITE,6,7));
        Pieces.add(new Rook(WHITE,0,7));
        Pieces.add(new Rook(WHITE,7,7));
        Pieces.add(new Bishop(WHITE,2,7));
        Pieces.add(new Bishop(WHITE,5,7));
        Pieces.add(new Queen(WHITE,3,7));
        Pieces.add(new King(WHITE,4,7));


        //Black team
        Pieces.add(new Pawn(BLACK,0,1));
        Pieces.add(new Pawn(BLACK,1,1));
        Pieces.add(new Pawn(BLACK,2,1));
        Pieces.add(new Pawn(BLACK,3,1));
        Pieces.add(new Pawn(BLACK,4,1));
        Pieces.add(new Pawn(BLACK,5,1));
        Pieces.add(new Pawn(BLACK,6,1));
        Pieces.add(new Pawn(BLACK,7,1));
        Pieces.add(new Knight(BLACK,1,0));
        Pieces.add(new Knight(BLACK,6,0));
        Pieces.add(new Rook(BLACK,0,0));
        Pieces.add(new Rook(BLACK,7,0));
        Pieces.add(new Bishop(BLACK,2,0));
        Pieces.add(new Bishop(BLACK,5,0));
        Pieces.add(new Queen(BLACK,3,0));
        Pieces.add(new King(BLACK,4,0));

    }
    private void copyPieces(ArrayList<Piece> source,ArrayList<Piece> target){
        target.clear();
        for(int i=0;i<source.size();i++){
            target.add(source.get(i));
        }
    }
    private boolean isStalemate(){
        int count=0;
        //count the no of pieces
        for(Piece piece:simPieces){
            if(piece.color!=currentColor){
                count++;
            }
        }
        if(count==1){
            if(KingCanMove(getKing(true))==false){
                return true;
            }
        }
        return false;
    }
    private void promoting(){
        if(mouse.pressed){
            for(Piece piece:promoPieces){
                if(piece.col==mouse.x/Board.SQUARE_SIZE &&piece.row== mouse.y/Board.SQUARE_SIZE){
                    switch(piece.type){
                        case ROOK: simPieces.add(new Rook(currentColor, activeP.col, activeP.row));     break;
                        case KNIGHT: simPieces.add(new Knight(currentColor, activeP.col, activeP.row)); break;
                        case BISHOP: simPieces.add(new Bishop(currentColor, activeP.col, activeP.row)); break;
                        case QUEEN: simPieces.add(new Queen(currentColor, activeP.col, activeP.row));   break;
                        default: break;
                    }
                    simPieces.remove(activeP.getIndex());
                    copyPieces(simPieces,Pieces);
                    activeP=null;
                    promotion=false;
                    changePlayer();
                }
            }
        }

    }
    private boolean isIllegal(Piece king){
        if(king.type==Type.KING){
            for(Piece piece :simPieces){
                if(piece!=king && piece.color!=king.color &&piece.canMove(king.col, king.row) ){
                    return true;
                }
            }
        }


        return false;
    }

    private void update() {
        if(promotion){
            promoting();
        }
        else if(gameover==false &&stalemate==false){
            if (mouse.pressed) {
                if (activeP == null) {
                    for (Piece piece : simPieces) {
                        if (piece.color == currentColor &&
                                piece.col == mouse.x / Board.SQUARE_SIZE &&
                                piece.row == mouse.y / Board.SQUARE_SIZE) {
                            activeP = piece;

                        }
                    }
                } else {
                    if (activeP != null) {
                        simulate();

                    }
                }
            }
            if(mouse.pressed==false){
                if(activeP!=null){
                    if(validSquare) {

                        copyPieces(simPieces,Pieces);
                        activeP.updatePosition();
                        if(castlingP!=null){
                            castlingP.updatePosition();
                        }
                        if(isKinginCheck() &&isCheckMate()){
                            gameover=true;
                           //Possibly game over
                        }
                        else if(isStalemate() &&isKinginCheck()==false){
                            stalemate=true;
                        }
                        else {
                            if (canPromote()) {
                                promotion = true;
                            } else {
                                changePlayer();
                            }
                        }

                    }
                    else {
                        copyPieces(Pieces,simPieces);
                        activeP.resetPosition();
                        activeP = null;
                    }
                }
            }
        }

    }
    private boolean isKinginCheck(){
        Piece king=getKing(true);
        if(activeP.canMove(king.col,king.row)){
            checkingP=activeP;
            return true;
        }
        else{
            checkingP=null;
        }
        return false;
    }


    private Piece getKing(boolean opponent){
        Piece king=null;
        for(Piece piece:simPieces){
            if(opponent){
                if(piece.type==Type.KING && piece.color!=currentColor){
                    king=piece;
                }
              else{
                    if(piece.type==Type.KING && piece.color==currentColor){
                        king=piece;
                    }
                }
            }

        }
        return king;
    }
    private boolean canPromote(){
        if(activeP.type==Type.PAWN){
            if(currentColor==WHITE &&activeP.row==0 ||currentColor==BLACK &&activeP.row==7){
                promoPieces.clear();
                promoPieces.add(new Rook(currentColor,9,2));
                promoPieces.add(new Knight(currentColor,9,3));
                promoPieces.add(new Bishop(currentColor,9,4));
                promoPieces.add(new Queen(currentColor,9,5));
                return true;
            }
        }
        return false;
    }
    private boolean opponentCanCaptureKing(){
         Piece king=getKing(true);
        for(Piece piece:simPieces){
            if(piece.color !=king.color && piece.canMove(king.col,king.row)){
                return true;
            }
        }
        return false;
    }

    private void simulate(){
        canMove=false;
        validSquare=false;
        copyPieces(Pieces,simPieces);

        //Reset Castling Position
        if(castlingP!=null){
            castlingP.col=castlingP.preCol;
            castlingP.x=castlingP.getX(castlingP.col);
            castlingP=null;
        }

        activeP.x = mouse.x-Board.HALF_SQUARE_SIZE;
        activeP.y = mouse.y-Board.HALF_SQUARE_SIZE;
        activeP.col=activeP.getCol(activeP.x);
        activeP.row=activeP.getRow(activeP.y);

        if(activeP.canMove(activeP.col, activeP.row)){
            canMove=true;
            if(activeP.hittingP!=null){
                simPieces.remove(activeP.hittingP.getIndex());
            }
            checkCastling();
            if(isIllegal(activeP)==false && opponentCanCaptureKing()==false){
                validSquare=true;
            }
        }

    }
    private boolean isCheckMate() {
        Piece king = getKing(true);
        if (KingCanMove(king)) {
            return false;
        } else {
            //check if any of your piece can block the attack
            //check the position of the checking piece from your king in check
            int colDiff = Math.abs(checkingP.col - king.col);
            int rowDiff = Math.abs(checkingP.row - king.row);
            if (colDiff == 0) {
                //the checking piece is attacking from vertically
                if (checkingP.row < king.row) {
                    //the checking piece is above the king
                    for (int row = checkingP.row; row < king.row; row++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }
                if (checkingP.row > king.row) {
                    //the checking piece is below the king
                    for (int row = checkingP.row; row > king.row; row--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }
            } else if (rowDiff == 0) {
                //the checking piece is attacking from horizontally
                if (checkingP.col < king.col) {
                    //the checking piece is to the left
                    for (int col = checkingP.col; col < king.col; col++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }
                if (checkingP.col > king.col) {
                    //the checking piece is to the right
                    for (int col = checkingP.col; col > king.col; col--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }
            } else if (colDiff == rowDiff) {
                //the checking piece is attacking diagonally
                if (checkingP.row < king.row) {
                    //the checking piece is above the king
                    if (checkingP.col < king.col) {
                        //the checking piece is on the upper left
                        for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                    return false;
                                }
                            }

                        }
                        if (checkingP.col > king.col) {
                            //the checking piece is on the upper right
                            for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++) {
                                for (Piece piece : simPieces) {
                                    if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    if (checkingP.row > king.row) {
                        //the checking piece is below the king
                        if (checkingP.col < king.col) {
                            //the checking piece is in lower left
                            for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--) {
                                for (Piece piece : simPieces) {
                                    if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                        return false;
                                    }
                                }
                            }
                            if (checkingP.col > king.col) {
                                //the checking piece is in lower right
                                for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--) {
                                    for (Piece piece : simPieces) {
                                        if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                            return false;
                                        }
                                    }
                                }

                            }

                        }
                    }
                }
            }
        }
        return true;
    }
    private boolean KingCanMove(Piece king){
        //Check if the king can move to any square
        if(isValidMove(king,-1,-1)){return true;}
        if(isValidMove(king,0,-1)){return true;}
        if(isValidMove(king,1,-1)){return true;}
        if(isValidMove(king,-1,0)){return true;}
        if(isValidMove(king,1,0)){return true;}
        if(isValidMove(king,-1,1)){return true;}
        if(isValidMove(king,0,1)){return true;}
        if(isValidMove(king,1,1)){return true;}

        return false;
    }
    private boolean isValidMove(Piece king,int colPlus,int rowPlus){
        boolean isValidMove=false;
        //Updating king position for a second
        king.col+=colPlus;
        king.row+=rowPlus;
        if(king.canMove(king.col,king.row)){
            if(king.hittingP!=null){
                simPieces.remove(king.hittingP.getIndex());
            }
            if(isIllegal(king)==false){
                isValidMove=true;
            }
        }
        king.resetPosition();
        copyPieces(Pieces,simPieces);

        return isValidMove;
    }
    private void checkCastling(){
        if(castlingP!=null){
            if(castlingP.col==0){
                castlingP.col+=3;
            }
            else if(castlingP.col==7){
                castlingP.col -=2;
            }
            castlingP.x=castlingP.getX(castlingP.col);
        }
    }
    private void changePlayer(){
        if(currentColor==WHITE){
            currentColor=BLACK;
            //Reset black's two stepped status
            for(Piece piece:Pieces){
                if(piece.color==BLACK){
                    piece.twoStepped=false;
                }
            }
        }
        else{
            currentColor=WHITE;
            //Reset white's two stepped status
            for(Piece piece:Pieces){
                if(piece.color==WHITE){
                    piece.twoStepped=false;
                }
            }
        }
        activeP=null;
    }
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        //Board
        board.draw(g2);
        //Pieces
        for(Piece p: simPieces){
            p.draw(g2);
        }
        if(activeP!=null){
            if(canMove) {
                if(isIllegal(activeP) ||opponentCanCaptureKing()){
                    g2.setColor(Color.gray);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }else {
                    g2.setColor(Color.red);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }
            activeP.draw(g2);
        }
        //STATUS MESSAGES
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua",Font.PLAIN,40));
        g2.setColor(Color.white);
        if(promotion){
            g2.drawString("Promote to:",800,150);
            for(Piece piece:promoPieces){
                g2.drawImage(piece.image,piece.getX(piece.col),piece.getY(piece.row),Board.SQUARE_SIZE,Board.SQUARE_SIZE,null);

            }
        }
        else {

            if (currentColor == WHITE) {
                g2.drawString("White's Turn", 800, 550);
                if(checkingP!=null &&checkingP.color==BLACK){
                    g2.setColor(Color.white);
                        g2.drawString("The King",800,650);
                    g2.drawString(" Is in Check!",800,7000);
                    }
            } else {
                g2.drawString("Black's Turn", 800, 250);
                if(checkingP!=null &&checkingP.color==WHITE){
                    g2.setColor(Color.white);
                    g2.drawString("The King",800,100);
                    g2.drawString(" Is in Check!",800,150);
                }
            }
        }
        if(gameover){
            String s="";
            if(currentColor==WHITE){
                s="White Wins";
            }
            else{
                s="Black Wins";
            }
            g2.setFont(new Font("Arial",Font.PLAIN,90));
                    g2.setColor(Color.black);
                    g2.drawString(s,150,400);
        }
        if(stalemate){
            g2.setFont(new Font("Arial",Font.PLAIN,90));
            g2.setColor(Color.black);
            g2.drawString("Stalemate!",150,400);

        }
    }

    @Override
    public void run() {
        final double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                SwingUtilities.invokeLater(this::repaint); // Schedule repaint on EDT
                delta--;
            }
        }
    }
}
