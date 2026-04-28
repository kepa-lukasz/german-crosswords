package com.germancrosswords.germancrosswords.Classes.CrosswordGenerator;

public class PlacedWordDTO {
    private String answer;
     private String polish;
    private int startx;
    private int starty;
    private String orientation;
    private int position;

    // Konstruktor
    public PlacedWordDTO(String answer, String polish, int startx, int starty, String orientation, int position) {
        this.answer = answer;
        this.polish = polish;
        this.startx = startx;
        this.starty = starty;
        this.orientation = orientation;
        this.position = position;
    }

    // Gettery są wymagane, żeby Spring mógł zamienić to na JSON!
    public String getAnswer() { return answer; }
     public String getPolish() { return polish; }
    public int getStartx() { return startx; }
    public int getStarty() { return starty; }
    public String getOrientation() { return orientation; }
    public int getPosition() { return position; }

    public void setStartx(int startx) { this.startx = startx; }
    public void setStarty(int starty) { this.starty = starty; }
}
